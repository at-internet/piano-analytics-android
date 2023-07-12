package io.piano.android.analytics

import com.squareup.moshi.JsonAdapter
import io.piano.android.analytics.model.ContextProperty
import io.piano.android.analytics.model.Property
import io.piano.android.analytics.model.PropertyName

internal class CrashReporter(
    private val configuration: Configuration,
    private val prefsStorage: PrefsStorage,
    private val packageName: String,
    private val screenNameProvider: ScreenNameProvider,
    private val contextPropertiesStorage: ContextPropertiesStorage,
    private val propertiesJsonAdapter: JsonAdapter<Set<Property>>,
) {
    internal fun initialize() {
        val crashInfo = prefsStorage.crashInfo ?: return
        val data = propertiesJsonAdapter.fromJson(crashInfo) ?: return
        contextPropertiesStorage.add(ContextProperty(data))
    }

    internal fun processUncaughtException(@Suppress("UNUSED_PARAMETER") t: Thread, e: Throwable) {
        if (!configuration.detectCrashes) {
            return
        }
        val exc = e.cause ?: e
        val data = setOf(
            Property(PropertyName.APP_CRASH, exc.javaClass.name),
            Property(PropertyName.APP_CRASH_SCREEN, screenNameProvider.screenName),
            Property(
                PropertyName.APP_CRASH_CLASS,
                exc.stackTrace.firstOrNull { it.className.startsWith(packageName) }?.className ?: ""
            )
        )
        prefsStorage.crashInfo = propertiesJsonAdapter.toJson(data)
        contextPropertiesStorage.add(ContextProperty(data))
    }
}
