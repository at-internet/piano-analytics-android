package io.piano.android.analytics.eventprocessors

import android.os.Build
import io.piano.android.analytics.BuildConfig
import io.piano.android.analytics.Configuration
import io.piano.android.analytics.DeviceInfoProvider
import io.piano.android.analytics.model.ConnectionType
import io.piano.android.analytics.model.Event
import io.piano.android.analytics.model.OfflineStorageMode
import io.piano.android.analytics.model.Property
import io.piano.android.analytics.model.PropertyName
import java.util.Locale

internal class InternalPropertiesEventProcessor(
    private val configuration: Configuration,
    private val deviceInfoProvider: DeviceInfoProvider,
) : EventProcessor {
    override fun process(events: List<Event>): List<Event> {
        val (width, height) = deviceInfoProvider.displayMetrics.run { widthPixels to heightPixels }
        val appProperties = deviceInfoProvider.packageInfo?.run {
            setOf(
                Property(PropertyName.APP_ID, packageName),
                Property(PropertyName.APP_VERSION, versionName)
            )
        } ?: emptySet()
        val (language, country) = Locale.getDefault().run { language to country }
        val connectionType = if (configuration.offlineStorageMode == OfflineStorageMode.ALWAYS) {
            ConnectionType.OFFLINE
        } else {
            deviceInfoProvider.connectionType
        }

        return events.map { event ->
            event.newBuilder()
                .properties(
                    Property(PropertyName.DEVICE_SCREEN_WIDTH, width),
                    Property(PropertyName.DEVICE_SCREEN_HEIGHT, height),
//                    Property(PropertyName.DEVICE_SCREEN_DIAGONAL, 0), // fixme: do we really need device display diagonal?
                    Property(PropertyName.OS_GROUP, PLATFORM),
                    Property(PropertyName.OS_VERSION, Build.VERSION.RELEASE),
                    Property(PropertyName.OS, "$PLATFORM ${Build.VERSION.RELEASE}"),
                    Property(PropertyName.DEVICE_MANUFACTURER, Build.MANUFACTURER),
                    Property(PropertyName.DEVICE_MODEL, Build.MODEL),
                    Property(PropertyName.DEVICE_TIMESTAMP_UTC, System.currentTimeMillis() / 1000),
                    Property(PropertyName.BROWSER_LANGUAGE, language),
                    Property(PropertyName.BROWSER_LANGUAGE_LOCAL, country),
                    Property(PropertyName.CONNECTION_TYPE, connectionType.key),
                    Property(PropertyName.EVENT_COLLECTION_PLATFORM, PLATFORM),
                    Property(PropertyName.EVENT_COLLECTION_VERSION, BuildConfig.SDK_VERSION)
                )
                .properties(appProperties)
                .build()
        }
    }

    companion object {
        internal const val PLATFORM = "android"
    }
}
