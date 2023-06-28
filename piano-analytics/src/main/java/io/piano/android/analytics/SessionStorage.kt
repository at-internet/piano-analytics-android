package io.piano.android.analytics

import android.os.Build
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.abs

internal class SessionStorage(
    private val prefsStorage: PrefsStorage,
    private val deviceInfoProvider: DeviceInfoProvider,
    sessionLifecycleListener: SessionLifecycleListener,
) {
    private val appVersionCode: Long by lazy {
        deviceInfoProvider.packageInfo?.run {
            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) longVersionCode else versionCode.toLong()
        } ?: 0L
    }
    private var savedVersionCode: Long by prefsStorage::versionCode
    private var lastSessionTimestamp: Long by prefsStorage::lastSessionDate

    var sessionCount: Int by prefsStorage::sessionCount
        private set
    var sessionCountAfterUpdate: Int by prefsStorage::sessionCountAfterUpdate
        private set
    var firstSessionTimestamp: Long by prefsStorage::firstSessionDate
        private set
    var firstSessionTimestampAfterUpdate: Long by prefsStorage::firstSessionDateAfterUpdate
        private set

    var sessionId: String = ""
        private set
    val isFirstSession
        get() = sessionCount == 1
    val isFirstSessionAfterUpdate
        get() = sessionCount != 1 && sessionCountAfterUpdate == 1
    val daysSinceFirstSession
        get() = getCurrentTimestamp().daysSince(firstSessionTimestamp)
    val daysSinceUpdate
        get() = getCurrentTimestamp().daysSince(firstSessionTimestampAfterUpdate)
    val daysSinceLastSession
        get() = getCurrentTimestamp().daysSince(lastSessionTimestamp)

    init {
        sessionLifecycleListener.sessionExpiredCallback = this::initNewSession
        addLifecycleObserver(sessionLifecycleListener)
        if (sessionCount == 0) {
            // first session
            sessionCount = 1
            sessionCountAfterUpdate = 1
            firstSessionTimestamp = getCurrentTimestamp()
            lastSessionTimestamp = firstSessionTimestamp
            savedVersionCode = appVersionCode
            sessionId = UUID.randomUUID().toString()
        } else {
            initNewSession()
        }
    }

    internal fun initNewSession() {
        lastSessionTimestamp = getCurrentTimestamp()
        sessionCount++
        if (savedVersionCode != appVersionCode) {
            savedVersionCode = appVersionCode
            firstSessionTimestampAfterUpdate = lastSessionTimestamp
            sessionCountAfterUpdate = 1
        } else {
            sessionCountAfterUpdate++
        }
        sessionId = UUID.randomUUID().toString()
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Long.daysSince(timestamp: Long) = TimeUnit.MILLISECONDS.toDays(abs(this - timestamp))

    // for mocking in tests
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun getCurrentTimestamp() = System.currentTimeMillis()

    // for mocking in tests
    internal fun addLifecycleObserver(observer: DefaultLifecycleObserver) =
        ProcessLifecycleOwner.get().lifecycle.addObserver(observer)
}
