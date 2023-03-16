package io.piano.android.analytics

import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before

open class BaseInterceptorTest {
    protected lateinit var mockWebServer: MockWebServer

    @Before
    open fun setUp() {
        mockWebServer = MockWebServer().apply {
            start()
        }
    }

    @After
    open fun tearDown() {
        mockWebServer.shutdown()
    }
}
