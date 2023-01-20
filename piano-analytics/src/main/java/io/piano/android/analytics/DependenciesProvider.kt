package io.piano.android.analytics

import android.content.Context
import androidx.annotation.RestrictTo
import com.squareup.moshi.Moshi
import io.piano.analytics.BuildConfig
import io.piano.android.analytics.eventprocessors.ContextPropertiesEventProcessor
import io.piano.android.analytics.eventprocessors.GroupEventProcessor
import io.piano.android.analytics.eventprocessors.InternalPropertiesEventProcessor
import io.piano.android.analytics.eventprocessors.PrivacyEventProcessor
import io.piano.android.analytics.eventprocessors.SessionEventProcessor
import io.piano.android.analytics.eventprocessors.UserEventProcessor
import io.piano.android.analytics.idproviders.CustomIdProvider
import io.piano.android.analytics.idproviders.GoogleAdvertisingIdProvider
import io.piano.android.analytics.idproviders.HuaweiAdvertisingIDIdProvider
import io.piano.android.analytics.idproviders.IdProvider
import io.piano.android.analytics.idproviders.UuidIdProvider
import io.piano.android.analytics.idproviders.VisitorIdProvider
import io.piano.android.analytics.model.Event
import io.piano.android.analytics.model.EventsRequest
import io.piano.android.analytics.model.User
import io.piano.android.analytics.model.VisitorIDType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class DependenciesProvider private constructor(
    context: Context,
    configuration: Configuration,
    dataEncoder: DataEncoder
) {
    private val userAgent = "Piano Analytics SDK ${BuildConfig.SDK_VERSION}"
    private val executorProvider: () -> ScheduledExecutorService = { Executors.newSingleThreadScheduledExecutor() }
    private val prefsStorage = PrefsStorage(context)
    private val privacyModesStorage = PrivacyModesStorage(configuration, prefsStorage)
    private val screenNameProvider = ScreenNameProvider()
    private val contextPropertiesStorage = ContextPropertiesStorage()

    private val moshi = Moshi.Builder()
        .add(RawJsonAdapter)
        .add(EventJsonAdapterFactory())
        .build()

    private val crashReporter = CrashReporter(
        configuration,
        prefsStorage,
        context.packageName,
        screenNameProvider,
        contextPropertiesStorage,
        moshi.adapter(EventJsonAdapterFactory.EVENT_PROPERTIES_TYPE)
    )
    private val crashHandler = CrashHandler(
        Thread.getDefaultUncaughtExceptionHandler(),
        crashReporter::processUncaughtException
    )

    private val deviceInfoProvider = DeviceInfoProvider(context)
    private val sessionLifecycleListener = SessionLifecycleListener(configuration.sessionBackgroundDuration.toLong())
    private val sessionStorage = SessionStorage(prefsStorage, deviceInfoProvider, sessionLifecycleListener)

    private val userStorage = UserStorage(configuration, prefsStorage, moshi.adapter(User::class.java))

    private val customIdProvider = CustomIdProvider()
    private val googleAdvertisingIdProvider = GoogleAdvertisingIdProvider(context)
    private val huaweiAdvertisingIdProvider = HuaweiAdvertisingIDIdProvider(context)
    private val uuidIdProvider = UuidIdProvider(configuration, prefsStorage)
    private val advertisingIdProvider = object : IdProvider {
        override val visitorId: String?
            get() = googleAdvertisingIdProvider.visitorId ?: huaweiAdvertisingIdProvider.visitorId
        override val isLimitAdTrackingEnabled: Boolean
            get() = googleAdvertisingIdProvider.isLimitAdTrackingEnabled ||
                huaweiAdvertisingIdProvider.isLimitAdTrackingEnabled
    }

    private val visitorIdProvider = VisitorIdProvider(
        configuration,
        privacyModesStorage,
        uuidIdProvider
    ) {
        when (it) {
            VisitorIDType.ADVERTISING_ID -> advertisingIdProvider
            VisitorIDType.CUSTOM -> customIdProvider
            VisitorIDType.GOOGLE_ADVERTISING_ID -> googleAdvertisingIdProvider
            VisitorIDType.HUAWEI_OPEN_ADVERTISING_ID -> huaweiAdvertisingIdProvider
            VisitorIDType.UUID -> uuidIdProvider
        }
    }

    private val databaseHelper = DatabaseHelper(context, dataEncoder)
    private val eventRepository = EventRepository(
        databaseHelper,
        moshi.adapter(Event::class.java)
    )
    private val eventsAdapter = moshi.adapter(EventsRequest::class.java)

    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(UserAgentInterceptor(userAgent))
        .addInterceptor(RetryInterceptor())
        .addInterceptor(
            HttpLoggingInterceptor().setLevel(
                if (BuildConfig.DEBUG || isLogHttpSet())
                    HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
            )
        )
        .build()

    private val sendTask = SendTask(
        configuration,
        eventRepository,
        deviceInfoProvider,
        visitorIdProvider,
        okHttpClient,
        eventsAdapter
    )

/*
    configurationStep,
    VisitorIDStep.getInstance(ps),                                              visitorIdProvider -> required for url
    CrashHandlingStep.getInstance(context, ps),                                 CrashReporter (extract saved crash and add as properties, maybe via contextPropertiesStorage)
    LifecycleStep.getInstance(context, ps),                                     SessionEventProcessor
    InternalContextPropertiesStep.getInstance(),                                InternalPropertiesEventProcessor
    CustomerContextPropertiesStep.getInstance(),                                ContextPropertiesEventProcessor
    UsersStep.getInstance(context, ps, configurationStep.getConfiguration()),   UserEventProcessor
    OnBeforeBuildCallStep.getInstance(),                                        customEventProcessors
    ps,                                                                         PrivacyEventProcessor
    BuildStep.getInstance(),                                                    useless, url will be built at sending
    StorageStep.getInstance(context),
    OnBeforeSendCallStep.getInstance(),                                         useless, maybe EventProcessor will be allowed
    SendStep.getInstance()                                                      SendTask
*/
    private val customEventProcessors = GroupEventProcessor()
    private val eventProcessors = GroupEventProcessor(
        mutableListOf(
            SessionEventProcessor(sessionStorage),
            InternalPropertiesEventProcessor(configuration, deviceInfoProvider),
            ContextPropertiesEventProcessor(contextPropertiesStorage),
            UserEventProcessor(userStorage),
            customEventProcessors,
            PrivacyEventProcessor(configuration, privacyModesStorage)
        )
    )

    internal val pianoAnalytics = PianoAnalytics(
        executorProvider,
        configuration,
        screenNameProvider,
        eventProcessors,
        eventRepository,
        sendTask,
        visitorIdProvider,
        customIdProvider,
        customEventProcessors,
        privacyModesStorage,
        contextPropertiesStorage,
        userStorage
    )

    companion object {
        @JvmStatic
        @Volatile
        private var instance: DependenciesProvider? = null

        @JvmStatic
        internal fun init(context: Context, configuration: Configuration, dataEncoder: DataEncoder) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null)
                        instance = DependenciesProvider(context.applicationContext, configuration, dataEncoder).also {
                            Thread.setDefaultUncaughtExceptionHandler(it.crashHandler)
                        }
                }
            }
        }

        @JvmStatic
        internal fun getInstance(): DependenciesProvider {
            checkNotNull(instance) {
                "Piano Analytics SDK is not initialized! Make sure that you initialize it"
            }
            return instance!!
        }
    }
}
