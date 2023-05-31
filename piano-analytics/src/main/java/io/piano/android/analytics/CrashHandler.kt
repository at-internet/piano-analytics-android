package io.piano.android.analytics

import java.lang.Thread.UncaughtExceptionHandler

internal class CrashHandler(
    private val defaultHandler: UncaughtExceptionHandler?,
    private val crashListener: (Thread, Throwable) -> Unit
) : UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        crashListener(t, e)
        defaultHandler?.uncaughtException(t, e)
    }
}
