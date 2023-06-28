package io.piano.android.analytics

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.TimeUnit
import kotlin.math.abs

internal class SessionLifecycleListener(
    backgroundDuration: Long,
) : DefaultLifecycleObserver {
    private val sessionBackgroundDuration = TimeUnit.SECONDS.toMillis(backgroundDuration)
    private var timestamp = -1L
    internal var sessionExpiredCallback: () -> Unit = {}

    override fun onResume(owner: LifecycleOwner) {
        if (timestamp > -1 && sessionBackgroundDuration < abs(getCurrentTimestamp() - timestamp)) {
            sessionExpiredCallback()
        }
        timestamp = -1
    }

    override fun onPause(owner: LifecycleOwner) {
        timestamp = getCurrentTimestamp()
    }

    // for mocking in tests
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun getCurrentTimestamp() = System.currentTimeMillis()
}
