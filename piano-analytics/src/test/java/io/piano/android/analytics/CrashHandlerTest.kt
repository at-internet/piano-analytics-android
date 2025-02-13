package io.piano.android.analytics

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import java.lang.Thread.UncaughtExceptionHandler

class CrashHandlerTest {
    private val crashListener: (Thread, Throwable) -> Unit = spy(
        object : CrashListener {
            override fun invoke(thread: Thread, throwable: Throwable) {
            }
        },
    )

    @Test
    fun uncaughtException() {
        CrashHandler(null, crashListener).uncaughtException(mock(), mock())
        verify(crashListener).invoke(any(), any())
    }

    @Test
    fun uncaughtExceptionWithDefaultHandler() {
        val defaultHandler = mock<UncaughtExceptionHandler>()
        CrashHandler(defaultHandler, crashListener).uncaughtException(mock(), mock())
        verify(crashListener).invoke(any(), any())
        verify(defaultHandler).uncaughtException(any(), any())
    }
}
