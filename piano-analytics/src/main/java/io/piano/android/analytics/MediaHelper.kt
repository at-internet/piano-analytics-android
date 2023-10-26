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
        processHeartbeat(-1, true)
    }
    private val bufferHeartbeatRunnable: Runnable = Runnable {
        processBufferHeartbeat(true)
    }
    private val rebufferHeartbeatRunnable: Runnable = Runnable {
        processRebufferHeartbeat(true)
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
    fun setExtraProps(vararg extraProps: Property): MediaHelper = apply {
        this.extraProps = extraProps.copyOf()
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
    fun bufferHeartbeat(vararg properties: Property) = processBufferHeartbeat(false, *properties)

    /**
     * Generate heartbeat during rebuffering.
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun rebufferHeartbeat(vararg properties: Property) = processRebufferHeartbeat(false, *properties)

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
        pianoAnalytics.sendEvents(buildEvent(AV_PLAY, true, *properties))
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
            AV_REBUFFER_START to rebufferHeartbeatRunnable
        } else {
            AV_BUFFER_START to bufferHeartbeatRunnable
        }

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
    fun playbackStart(cursorPosition: Int, vararg properties: Property) = processEvent(AV_START, properties) {
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
    fun playbackPaused(cursorPosition: Int, vararg properties: Property) = processEvent(AV_PAUSE, properties) {
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
    fun playbackResumed(cursorPosition: Int, vararg properties: Property) = processEvent(AV_RESUME, properties) {
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
        processEvent(AV_STOP, properties) {
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
        } else {
            seekForward(oldCursorPosition, newCursorPosition, *properties)
        }

    /**
     * Measuring seek backward.
     *
     * @param oldCursorPosition Starting position in milliseconds
     * @param newCursorPosition Ending position in milliseconds
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun seekBackward(oldCursorPosition: Int, newCursorPosition: Int, vararg properties: Property) =
        processSeek(AV_BACKWARD, oldCursorPosition, newCursorPosition, properties)

    /**
     * Measuring seek forward.
     *
     * @param oldCursorPosition Starting position in milliseconds
     * @param newCursorPosition Ending position in milliseconds
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun seekForward(oldCursorPosition: Int, newCursorPosition: Int, vararg properties: Property) =
        processSeek(AV_FORWARD, oldCursorPosition, newCursorPosition, properties)

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
    fun adClick(vararg properties: Property) = pianoAnalytics.sendEvents(buildEvent(AV_AD_CLICK, false, *properties))

    /**
     * Measuring media skip (especially for ads).
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun adSkip(vararg properties: Property) = pianoAnalytics.sendEvents(buildEvent(AV_AD_SKIP, false, *properties))

    /**
     * Measuring reco or Ad display.
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun display(vararg properties: Property) = pianoAnalytics.sendEvents(buildEvent(AV_DISPLAY, false, *properties))

    /**
     * Measuring close action.
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun close(vararg properties: Property) = pianoAnalytics.sendEvents(buildEvent(AV_CLOSE, false, *properties))

    /**
     * Measurement of a volume change action.
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun volume(vararg properties: Property) = pianoAnalytics.sendEvents(buildEvent(AV_VOLUME, false, *properties))

    /**
     * Measurement of activated subtitles.
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun subtitleOn(vararg properties: Property) =
        pianoAnalytics.sendEvents(buildEvent(AV_SUBTITLE_ON, false, *properties))

    /**
     * Measurement of deactivated subtitles.
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun subtitleOff(vararg properties: Property) =
        pianoAnalytics.sendEvents(buildEvent(AV_SUBTITLE_OFF, false, *properties))

    /**
     * Measuring a full-screen display.
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun fullscreenOn(vararg properties: Property) =
        pianoAnalytics.sendEvents(buildEvent(AV_FULLSCREEN_ON, false, *properties))

    /**
     * Measuring a full screen deactivation.
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun fullscreenOff(vararg properties: Property) =
        pianoAnalytics.sendEvents(buildEvent(AV_FULLSCREEN_OFF, false, *properties))

    /**
     * Measurement of a quality change action.
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun quality(vararg properties: Property) = pianoAnalytics.sendEvents(buildEvent(AV_QUALITY, false, *properties))

    /**
     * Measurement of a speed change action.
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun speed(vararg properties: Property) = pianoAnalytics.sendEvents(buildEvent(AV_SPEED, false, *properties))

    /**
     * Measurement of a sharing action.
     *
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun share(vararg properties: Property) = pianoAnalytics.sendEvents(buildEvent(AV_SHARE, false, *properties))

    /**
     * Error measurement preventing reading from continuing.
     *
     * @param message error message
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun error(message: String, vararg properties: Property) = pianoAnalytics.sendEvents(
        buildEvent(
            AV_ERROR,
            false,
            Property(PropertyName("av_player_error"), message),
            *properties
        )
    )

    /**
     * Track custom event, don't use it for built-in events.
     *
     * @param eventName name of custom event
     * @param properties extra properties for event
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun track(eventName: String, vararg properties: Property) {
        require(eventName !in BUILTIN_EVENTS) {
            "customEvent should be used only for custom events, not for built-in"
        }
        pianoAnalytics.sendEvents(buildEvent(eventName, false, *properties))
    }

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
        properties: Array<out Property>,
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
        return buildEvent(AV_SEEK_START, true, *properties)
    }

    internal fun buildEvent(eventName: String, addOptions: Boolean = false, vararg properties: Property): Event {
        val avProperties = if (addOptions) {
            setOf(
                Property(PropertyName("av_previous_position"), previousCursorPositionMillis),
                Property(PropertyName("av_position"), currentCursorPositionMillis),
                Property(PropertyName("av_duration"), eventDurationMillis),
                Property(PropertyName("av_previous_event"), previousEventName)
            ).also {
                previousEventName = eventName
            }
        } else {
            emptySet()
        }
        return Event.Builder(eventName)
            .properties(
                *extraProps,
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
        processEvent(AV_HEARTBEAT, properties) {
            previousCursorPositionMillis = currentCursorPositionMillis
            currentCursorPositionMillis = if (cursorPosition < 0) {
                currentCursorPositionMillis + (eventDurationMillis * playbackSpeed).toInt()
            } else {
                cursorPosition
            }

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
        processEvent(AV_BUFFER_HEARTBEAT, properties) {
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
        processEvent(AV_REBUFFER_HEARTBEAT, properties) {
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
        runnable: Runnable,
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
        private const val AV_PLAY = "av.play"
        private const val AV_REBUFFER_START = "av.rebuffer.start"
        private const val AV_BUFFER_START = "av.buffer.start"
        private const val AV_START = "av.start"
        private const val AV_PAUSE = "av.pause"
        private const val AV_RESUME = "av.resume"
        private const val AV_STOP = "av.stop"
        private const val AV_BACKWARD = "av.backward"
        private const val AV_FORWARD = "av.forward"
        private const val AV_AD_CLICK = "av.ad.click"
        private const val AV_AD_SKIP = "av.ad.skip"
        private const val AV_DISPLAY = "av.display"
        private const val AV_CLOSE = "av.close"
        private const val AV_VOLUME = "av.volume"
        private const val AV_SUBTITLE_ON = "av.subtitle.on"
        private const val AV_SUBTITLE_OFF = "av.subtitle.off"
        private const val AV_FULLSCREEN_ON = "av.fullscreen.on"
        private const val AV_FULLSCREEN_OFF = "av.fullscreen.off"
        private const val AV_QUALITY = "av.quality"
        private const val AV_SPEED = "av.speed"
        private const val AV_SHARE = "av.share"
        private const val AV_ERROR = "av.error"
        private const val AV_SEEK_START = "av.seek.start"
        private const val AV_HEARTBEAT = "av.heartbeat"
        private const val AV_BUFFER_HEARTBEAT = "av.buffer.heartbeat"
        private const val AV_REBUFFER_HEARTBEAT = "av.rebuffer.heartbeat"
        private val BUILTIN_EVENTS = arrayOf(
            AV_PLAY,
            AV_REBUFFER_START,
            AV_BUFFER_START,
            AV_START,
            AV_PAUSE,
            AV_RESUME,
            AV_STOP,
            AV_BACKWARD,
            AV_FORWARD,
            AV_AD_CLICK,
            AV_AD_SKIP,
            AV_DISPLAY,
            AV_CLOSE,
            AV_VOLUME,
            AV_SUBTITLE_ON,
            AV_SUBTITLE_OFF,
            AV_FULLSCREEN_ON,
            AV_FULLSCREEN_OFF,
            AV_QUALITY,
            AV_SPEED,
            AV_SHARE,
            AV_ERROR,
            AV_SEEK_START,
            AV_HEARTBEAT,
            AV_BUFFER_HEARTBEAT,
            AV_REBUFFER_HEARTBEAT
        )
    }
}
