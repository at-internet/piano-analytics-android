package io.piano.android.analytics

import android.content.Context
import io.piano.android.analytics.eventprocessors.EventProcessor
import io.piano.android.analytics.eventprocessors.GroupEventProcessor
import io.piano.android.analytics.idproviders.CustomIdProvider
import io.piano.android.analytics.idproviders.VisitorIdProvider
import io.piano.android.analytics.model.ContextProperty
import io.piano.android.analytics.model.Event
import io.piano.android.analytics.model.OfflineStorageMode
import io.piano.android.analytics.model.Property
import io.piano.android.analytics.model.PropertyName
import io.piano.android.consents.PianoConsents
import java.util.UUID
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

/**
 * Singleton class used as a facade to the Piano Analytics
 *
 * @property privacyModesStorage Storage for privacy modes
 * @property contextPropertiesStorage Storage for context properties
 * @property userStorage Storage for user data
 * @property pianoConsents [PianoConsents] instance for managing user consent.
 */
public class PianoAnalytics internal constructor(
    private val executorProvider: () -> ScheduledExecutorService,
    private val configuration: Configuration,
    private val screenNameProvider: ScreenNameProvider,
    private val eventProcessorsGroup: GroupEventProcessor,
    private val eventRepository: EventRepository,
    private val sendTask: SendTask,
    private val visitorIdProvider: VisitorIdProvider,
    private val customIdProvider: CustomIdProvider,
    customEventProcessorsGroup: GroupEventProcessor,
    // Public API.
    @Suppress("unused", "MemberVisibilityCanBePrivate")
    @Deprecated("Use `pianoConsents` for managing consents instead")
    public val privacyModesStorage: PrivacyModesStorage,
    // Public API.
    @Suppress("unused", "MemberVisibilityCanBePrivate")
    public val contextPropertiesStorage: ContextPropertiesStorage,
    // Public API.
    @Suppress("unused", "MemberVisibilityCanBePrivate")
    public val userStorage: UserStorage,
    // Public API.
    @Suppress("unused", "MemberVisibilityCanBePrivate")
    public val pianoConsents: PianoConsents?,
) {
    private val executor: ScheduledExecutorService = executorProvider()

    /**
     * Current visitor id, depends on [Configuration.visitorIDType]
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    public val visitorId: String? by visitorIdProvider::visitorId

    /**
     * Custom visitor id, will be used if [Configuration.visitorIDType] is set to [VisitorIDType.CUSTOM]
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    public var customVisitorId: String? by customIdProvider::visitorId

    /**
     * List of custom event processors, which can change sending events.
     * Privacy rules will be applied after these event processors
     *
     * @see [EventProcessor]
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    public val customEventProcessors: MutableList<EventProcessor> = customEventProcessorsGroup

    /**
     * Callback, which will be called after all events' processors and before sending event.
     */
    public var eventProcessorCallback: EventProcessorCallback = EventProcessorCallback { _ -> }

    /**
     * Sets current screen name.
     * It will be automatically added as property for event [Event.PAGE_DISPLAY]
     *
     * @param name screen name
     */
    @Suppress("unused") // Public API.
    public fun screenName(name: String) {
        screenNameProvider.customScreenName = name
        contextPropertiesStorage.add(
            ContextProperty(
                properties = setOf(Property(PropertyName.PAGE, name)),
                eventNames = listOf(Event.PAGE_DISPLAY),
            ),
        )
    }

    /**
     * Returns a [MediaHelper] for sending AV Insights event
     *
     * @param contentId Content id for AV Insights events.
     * @param mediaSessionId Session id for AV Insights events.
     * Use when you need to transfer sessions between different device, for example
     */
    @Suppress("unused") // Public API.
    @JvmOverloads
    public fun mediaHelper(
        contentId: String,
        mediaSessionId: String = UUID.randomUUID().toString(),
    ): MediaHelper {
        require(contentId.isNotEmpty()) {
            "Content id can't be empty"
        }
        require(mediaSessionId.isNotEmpty()) {
            "Media session id can't be empty"
        }
        return MediaHelper(
            mediaSessionId,
            contentId,
            this,
            executorProvider,
        )
    }

    /**
     * Send events data
     *
     * @param events a custom event list
     */
    @Suppress("unused") // Public API.
    public fun sendEvents(vararg events: Event) {
        // delay is required, see androidx.lifecycle.ProcessLifecycleOwner.TIMEOUT_MS
        executor.schedule(
            {
                val processedEvents = eventProcessorsGroup.process(events.toList())
                eventRepository.putEvents(processedEvents)
                eventProcessorCallback.onProcess(processedEvents)
                if (configuration.offlineStorageMode != OfflineStorageMode.ALWAYS) {
                    sendTask.run()
                }
            },
            1,
            TimeUnit.SECONDS,
        )
    }

    /**
     * Send offline data stored on device
     */
    @Suppress("unused") // Public API.
    public fun sendOfflineData() {
        executor.submit(sendTask)
    }

    /**
     * Delete offline data stored on device and keep only remaining days
     *
     * @param remaining age of data which have to be kept (in days)
     */
    @Suppress("unused") // Public API.
    public fun deleteOfflineStorage(remaining: Int = 0) {
        executor.submit {
            eventRepository.deleteOldEvents(remaining)
        }
    }

    public fun interface EventProcessorCallback {
        public fun onProcess(events: List<Event>)
    }

    public companion object {
        /**
         * Initializes Piano Analytics SDK
         *
         * @param context Activity or Application context
         * @param configuration [Configuration] object
         * @param pianoConsents [PianoConsents] instance for managing user consent. Default is null.
         * @param dataEncoder custom [DataEncoder] for encrypting/decrypting events' data, default is [PlainDataEncoder]
         */
        @Suppress("unused") // Public API.
        @JvmStatic
        @JvmOverloads
        public fun init(
            context: Context,
            configuration: Configuration,
            pianoConsents: PianoConsents? = null,
            dataEncoder: DataEncoder = PlainDataEncoder,
            customHttpDataProvider: CustomHttpDataProvider = EmptyCustomHttpDataProvider,
        ): PianoAnalytics {
            DependenciesProvider.init(
                context,
                configuration,
                pianoConsents ?: runCatching { PianoConsents.getInstance() }.getOrNull(),
                dataEncoder,
                customHttpDataProvider,
            )
            return getInstance()
        }

        @Suppress("unused") // Public API.
        @JvmStatic
        public fun getInstance(): PianoAnalytics = DependenciesProvider.getInstance().pianoAnalytics
    }
}
