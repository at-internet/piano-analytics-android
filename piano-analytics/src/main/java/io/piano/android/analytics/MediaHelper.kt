package io.piano.android.analytics

import android.util.SparseLongArray
import io.piano.android.analytics.model.Event
import io.piano.android.analytics.model.Property
import io.piano.android.analytics.model.PropertyName
import java.util.UUID
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Helper for AV Insights
 */
class MediaHelper internal constructor(
    sessionId: String,
    private val contentId: String,
    private val pianoAnalytics: PianoAnalytics,
    private val executorProvider: () -> ScheduledExecutorService,
) {
    private var executor: ScheduledExecutorService = executorProvider()
    private val heartbeatDurations = SparseLongArray()
    private val bufferHeartbeatDurations = SparseLongArray()

    private var autoHeartbeat = false
    private var autoBufferHeartbeat = false
    private var isPlaying = false
    private var isPlaybackActivated = false

    private var extraProps: Array<out Property> = emptyArray()

    private var previousHeartbeatDelay = 0L
    private var previousBufferHeartbeatDelay = 0L
    private var previousEventName = ""
    private var previousCursorPositionMillis = 0
    private var currentCursorPositionMillis = 0
    private var sessionDurationMillis = 0L
    private var eventDurationMillis = 0L
    private var startSessionTimeMillis by resettableProperty(0L) {
        getCurrentTimestamp()
    }
    private var bufferTimeMillis by resettableProperty(0L) {
        getCurrentTimestamp()
    }
    private val heartbeatRunnable: Runnable = Runnable {
        processHeartbeat(-1, true, *extraProps)
    }
    private val bufferHeartbeatRunnable: Runnable = Runnable {
        processBufferHeartbeat(true, *extraProps)
    }
    private val rebufferHeartbeatRunnable: Runnable = Runnable {
        processRebufferHeartbeat(true, *extraProps)
    }

    /**
     * Current session id
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    var sessionId: String = sessionId
        private set

    /**
     * Current playback speed
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    var playbackSpeed: Double = 1.0
        set(value) {
            require(value > 0) {
                "Speed can be only greater than 0"
            }

            restartHeartbeatExecutor()

            if (isPlaying) {
                heartbeat(-1)
                if (autoHeartbeat) {
                    previousHeartbeatDelay = rescheduleRunnable(
                        previousHeartbeatDelay,
                        startSessionTimeMillis,
                        MIN_HEARTBEAT_DURATION,
                        heartbeatDurations,
                        heartbeatRunnable
                    )
                }
            }
            field = value
        }

    /**
     * Sets heartbeat value
     *
     * @param values new values for heartbeat
     * @return this [MediaHelper] instance
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun setHeartbeat(values: SparseLongArray) = apply {
        require(values.size() > 0) {
            "Can't set empty heartbeat values"
        }
        autoHeartbeat = true
        heartbeatDurations.replaceValues(values, MIN_HEARTBEAT_DURATION)
    }

    /**
     * Sets buffer heartbeat value
     *
     * @param values new values for buffer heartbeat
     * @return this [MediaHelper] instance
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun setBufferHeartbeat(values: SparseLongArray) = apply {
        require(values.size() > 0) {
            "Can't set empty buffer heartbeat values"
        }
        autoBufferHeartbeat = true
        bufferHeartbeatDurations.replaceValues(values, MIN_BUFFER_HEARTBEAT_DURATION)
    }

    /**
     * Sets a map of extraProps on the media object and returns it.
     * @param extraProps The extra props (e.g. av_content properties)
     * @return this [MediaHelper] instance
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun setExtraProps(extraProps: Array<out Property>) = apply {
        this.extraProps = extraProps
    }

    /**
     * Generate heartbeat event.
     *
     * @param cursorPosition Cursor position in milliseconds
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun heartbeat(cursorPosition: Int, vararg properties: Property) =
        processHeartbeat(cursorPosition, false, *properties)

    /**
     * Generate heartbeat during buffering.
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun bufferHeartbeat(vararg properties: Property) =
        processBufferHeartbeat(false, *properties)

    /**
     * Generate heartbeat during rebuffering.
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun rebufferHeartbeat(vararg properties: Property) =
        processRebufferHeartbeat(false, *properties)

    /**
     * Generate play event (play attempt).
     *
     * @param cursorPosition Cursor position in milliseconds
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun play(cursorPosition: Int, vararg properties: Property) {
        eventDurationMillis = 0
        previousCursorPositionMillis = cursorPosition.coerceAtLeast(0)
        currentCursorPositionMillis = previousCursorPositionMillis
        bufferTimeMillis = 0
        isPlaying = false
        isPlaybackActivated = false

        restartHeartbeatExecutor()
        pianoAnalytics.sendEvents(buildEvent("av.play", true, *properties))
    }

    /**
     * Player buffering start to initiate the launch of the media.
     *
     * @param cursorPosition Cursor position in milliseconds
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun bufferStart(cursorPosition: Int, vararg properties: Property) {
        val (eventName, runnable) = if (isPlaybackActivated) {
            "av.rebuffer.start" to rebufferHeartbeatRunnable
        } else "av.buffer.start" to bufferHeartbeatRunnable

        processEvent(eventName, properties) {
            previousCursorPositionMillis = currentCursorPositionMillis
            currentCursorPositionMillis = cursorPosition.coerceAtLeast(0)
            restartHeartbeatExecutor()

            if (autoBufferHeartbeat) {
                previousBufferHeartbeatDelay = rescheduleRunnable(
                    previousBufferHeartbeatDelay,
                    bufferTimeMillis,
                    MIN_BUFFER_HEARTBEAT_DURATION,
                    bufferHeartbeatDurations,
                    runnable
                )
            }
        }
    }

    /**
     * Media playback start (first frame of the media).
     *
     * @param cursorPosition Cursor position in milliseconds
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun playbackStart(cursorPosition: Int, vararg properties: Property) =
        processEvent("av.start", properties) {
            previousCursorPositionMillis = cursorPosition.coerceAtLeast(0)
            currentCursorPositionMillis = previousCursorPositionMillis
            bufferTimeMillis = 0
            isPlaying = true
            isPlaybackActivated = true

            restartHeartbeatExecutor()
            if (autoHeartbeat) {
                previousHeartbeatDelay = rescheduleRunnable(
                    previousHeartbeatDelay,
                    startSessionTimeMillis,
                    MIN_HEARTBEAT_DURATION,
                    heartbeatDurations,
                    heartbeatRunnable
                )
            }
        }

    /**
     * Media playback paused.
     *
     * @param cursorPosition Cursor position in milliseconds
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun playbackPaused(cursorPosition: Int, vararg properties: Property) =
        processEvent("av.pause", properties) {
            previousCursorPositionMillis = currentCursorPositionMillis
            currentCursorPositionMillis = cursorPosition.coerceAtLeast(0)
            bufferTimeMillis = 0
            isPlaying = false
            isPlaybackActivated = true
            restartHeartbeatExecutor()
        }

    /**
     * Media playback restarted manually after a pause.
     *
     * @param cursorPosition Cursor position in milliseconds
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun playbackResumed(cursorPosition: Int, vararg properties: Property) =
        processEvent("av.resume", properties) {
            previousCursorPositionMillis = currentCursorPositionMillis
            currentCursorPositionMillis = cursorPosition.coerceAtLeast(0)
            bufferTimeMillis = 0
            isPlaying = true
            isPlaybackActivated = true

            restartHeartbeatExecutor()
            if (autoHeartbeat) {
                previousHeartbeatDelay = rescheduleRunnable(
                    previousHeartbeatDelay,
                    startSessionTimeMillis,
                    MIN_HEARTBEAT_DURATION,
                    heartbeatDurations,
                    heartbeatRunnable
                )
            }
        }

    /**
     * Media playback stopped.
     *
     * @param cursorPosition Cursor position in milliseconds
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun playbackStopped(cursorPosition: Int, vararg properties: Property) {
        processEvent("av.stop", properties) {
            previousCursorPositionMillis = currentCursorPositionMillis
            currentCursorPositionMillis = cursorPosition.coerceAtLeast(0)
            bufferTimeMillis = 0
            isPlaying = false
            isPlaybackActivated = false

            restartHeartbeatExecutor()
            startSessionTimeMillis = 0
            sessionDurationMillis = 0
            bufferTimeMillis = 0
            previousHeartbeatDelay = 0
            previousBufferHeartbeatDelay = 0
        }
        sessionId = UUID.randomUUID().toString()
        previousEventName = ""
        previousCursorPositionMillis = 0
        currentCursorPositionMillis = 0
        eventDurationMillis = 0
    }

    /**
     * Measuring seek event.
     *
     * @param oldCursorPosition Starting position in milliseconds
     * @param newCursorPosition Ending position in milliseconds
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun seek(oldCursorPosition: Int, newCursorPosition: Int, vararg properties: Property) =
        if (oldCursorPosition > newCursorPosition) {
            seekBackward(oldCursorPosition, newCursorPosition, *properties)
        } else seekForward(oldCursorPosition, newCursorPosition, *properties)

    /**
     * Measuring seek backward.
     *
     * @param oldCursorPosition Starting position in milliseconds
     * @param newCursorPosition Ending position in milliseconds
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun seekBackward(oldCursorPosition: Int, newCursorPosition: Int, vararg properties: Property) =
        processSeek("av.backward", oldCursorPosition, newCursorPosition, properties)

    /**
     * Measuring seek forward.
     *
     * @param oldCursorPosition Starting position in milliseconds
     * @param newCursorPosition Ending position in milliseconds
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun seekForward(oldCursorPosition: Int, newCursorPosition: Int, vararg properties: Property) =
        processSeek("av.forward", oldCursorPosition, newCursorPosition, properties)

    /**
     * Measuring seek start.
     *
     * @param oldCursorPosition Starting position in milliseconds
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun seekStart(oldCursorPosition: Int, vararg properties: Property) =
        pianoAnalytics.sendEvents(buildSeekStartEvent(oldCursorPosition, properties))

    /**
     * Measuring media click (especially for ads).
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun adClick(vararg properties: Property) =
        pianoAnalytics.sendEvents(buildEvent("av.ad.click", false, *properties))

    /**
     * Measuring media skip (especially for ads).
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun adSkip(vararg properties: Property) =
        pianoAnalytics.sendEvents(buildEvent("av.ad.skip", false, *properties))

    /**
     * Measuring reco or Ad display.
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun display(vararg properties: Property) =
        pianoAnalytics.sendEvents(buildEvent("av.display", false, *properties))

    /**
     * Measuring close action.
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun close(vararg properties: Property) =
        pianoAnalytics.sendEvents(buildEvent("av.close", false, *properties))

    /**
     * Measurement of a volume change action.
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun volume(vararg properties: Property) =
        pianoAnalytics.sendEvents(buildEvent("av.volume", false, *properties))

    /**
     * Measurement of activated subtitles.
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun subtitleOn(vararg properties: Property) =
        pianoAnalytics.sendEvents(buildEvent("av.subtitle.on", false, *properties))

    /**
     * Measurement of deactivated subtitles.
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun subtitleOff(vararg properties: Property) =
        pianoAnalytics.sendEvents(buildEvent("av.subtitle.off", false, *properties))

    /**
     * Measuring a full-screen display.
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun fullscreenOn(vararg properties: Property) =
        pianoAnalytics.sendEvents(buildEvent("av.fullscreen.on", false, *properties))

    /**
     * Measuring a full screen deactivation.
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun fullscreenOff(vararg properties: Property) =
        pianoAnalytics.sendEvents(buildEvent("av.fullscreen.off", false, *properties))

    /**
     * Measurement of a quality change action.
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun quality(vararg properties: Property) =
        pianoAnalytics.sendEvents(buildEvent("av.quality", false, *properties))

    /**
     * Measurement of a speed change action.
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun speed(vararg properties: Property) =
        pianoAnalytics.sendEvents(buildEvent("av.speed", false, *properties))

    /**
     * Measurement of a sharing action.
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun share(vararg properties: Property) =
        pianoAnalytics.sendEvents(buildEvent("av.share", false, *properties))

    /**
     * Error measurement preventing reading from continuing.
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun error(message: String, vararg properties: Property) =
        pianoAnalytics.sendEvents(
            buildEvent(
                "av.error",
                false,
                Property(PropertyName("av_player_error"), message),
                *properties
            )
        )

    internal fun restartHeartbeatExecutor() {
        if (!executor.isShutdown) {
            executor.shutdownNow()
        }
        executor = executorProvider()
    }

    internal fun processSeek(
        eventName: String,
        oldCursorPosition: Int,
        newCursorPosition: Int,
        properties: Array<out Property>
    ) {
        val startSeekEvent = buildSeekStartEvent(oldCursorPosition, properties)
        eventDurationMillis = 0
        previousCursorPositionMillis = oldCursorPosition.coerceAtLeast(0)
        currentCursorPositionMillis = newCursorPosition.coerceAtLeast(0)

        pianoAnalytics.sendEvents(
            startSeekEvent,
            buildEvent(eventName, true, *properties)
        )
    }

    internal fun buildSeekStartEvent(oldCursorPosition: Int, properties: Array<out Property>): Event {
        previousCursorPositionMillis = currentCursorPositionMillis
        currentCursorPositionMillis = oldCursorPosition.coerceAtLeast(0)

        if (isPlaying) {
            eventDurationMillis = getCurrentTimestamp() - startSessionTimeMillis - sessionDurationMillis
            sessionDurationMillis += eventDurationMillis
        } else {
            eventDurationMillis = 0
        }
        return buildEvent("av.seek.start", true, *properties)
    }

    internal fun buildEvent(eventName: String, addOptions: Boolean = false, vararg properties: Property): Event {
        val avProperties = if (addOptions) {
            setOf(
                Property(PropertyName("av_previous_position"), 0),
                Property(PropertyName("av_position"), 0),
                Property(PropertyName("av_duration"), 0),
                Property(PropertyName("av_previous_event"), previousEventName),
            ).also {
                previousEventName = eventName
            }
        } else emptySet()
        return Event.Builder(eventName)
            .properties(
                *properties,
                Property(PropertyName("av_session_id"), sessionId),
                Property(PropertyName("av_content_id"), contentId)
            )
            .properties(avProperties)
            .build()
    }

    internal fun processEvent(eventName: String, properties: Array<out Property>, customProcessing: () -> Unit) {
        eventDurationMillis = getCurrentTimestamp() - startSessionTimeMillis - sessionDurationMillis
        sessionDurationMillis += eventDurationMillis

        customProcessing()

        pianoAnalytics.sendEvents(buildEvent(eventName, true, *properties))
    }

    internal fun processHeartbeat(cursorPosition: Int = -1, isAutomatic: Boolean, vararg properties: Property) =
        processEvent("av.heartbeat", properties) {
            previousCursorPositionMillis = currentCursorPositionMillis
            currentCursorPositionMillis = if (cursorPosition < 0)
                currentCursorPositionMillis + (eventDurationMillis * playbackSpeed).toInt()
            else cursorPosition

            if (isAutomatic) {
                previousHeartbeatDelay = rescheduleRunnable(
                    previousHeartbeatDelay,
                    startSessionTimeMillis,
                    MIN_HEARTBEAT_DURATION,
                    heartbeatDurations,
                    heartbeatRunnable
                )
            }
        }

    internal fun processBufferHeartbeat(isAutomatic: Boolean, vararg properties: Property) =
        processEvent("av.buffer.heartbeat", properties) {
            if (isAutomatic) {
                previousBufferHeartbeatDelay = rescheduleRunnable(
                    previousBufferHeartbeatDelay,
                    bufferTimeMillis,
                    MIN_BUFFER_HEARTBEAT_DURATION,
                    bufferHeartbeatDurations,
                    bufferHeartbeatRunnable
                )
            }
        }

    internal fun processRebufferHeartbeat(isAutomatic: Boolean, vararg properties: Property) =
        processEvent("av.rebuffer.heartbeat", properties) {
            previousCursorPositionMillis = currentCursorPositionMillis
            if (isAutomatic) {
                previousBufferHeartbeatDelay = rescheduleRunnable(
                    previousBufferHeartbeatDelay,
                    bufferTimeMillis,
                    MIN_BUFFER_HEARTBEAT_DURATION,
                    bufferHeartbeatDurations,
                    rebufferHeartbeatRunnable
                )
            }
        }

    @Suppress("NOTHING_TO_INLINE")
    internal inline fun rescheduleRunnable(
        previousDelay: Long,
        startTimerMillis: Long,
        minDelay: Long,
        durations: SparseLongArray,
        runnable: Runnable
    ): Long {
        val minutesDelay = TimeUnit.MILLISECONDS.toMinutes(getCurrentTimestamp() - startTimerMillis).toInt()
        return durations.get(minutesDelay, previousDelay).coerceAtLeast(minDelay).also {
            executor.schedule(runnable, it, TimeUnit.SECONDS)
        }
    }

    private fun SparseLongArray.replaceValues(values: SparseLongArray, minValue: Long) {
        clear()
        for (i in 0 until values.size()) {
            put(values.keyAt(i), values.valueAt(i).coerceAtLeast(minValue))
        }
        if (indexOfKey(0) < 0) {
            put(0, minValue)
        }
    }

    // for mocking in tests
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun getCurrentTimestamp() = System.currentTimeMillis()

    companion object {
        internal const val MIN_HEARTBEAT_DURATION = 5L
        internal const val MIN_BUFFER_HEARTBEAT_DURATION = 1L
    }
}
