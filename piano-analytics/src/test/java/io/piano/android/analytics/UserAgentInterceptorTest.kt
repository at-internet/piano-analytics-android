package io.piano.android.analytics

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class UserAgentInterceptorTest : BaseInterceptorTest() {
    private val userAgentInterceptor = UserAgentInterceptor(DUMMY)

    @Test
    fun intercept() {
        mockWebServer.enqueue(MockResponse())
        OkHttpClient.Builder()
            .addInterceptor(userAgentInterceptor)
            .build()
            .newCall(
                Request.Builder()
                    .url(mockWebServer.url("/"))
                    .build(),
            ).execute()
        assertEquals(DUMMY, mockWebServer.takeRequest().getHeader("User-Agent"))
    }

    companion object {
        private const val DUMMY = "dummy"
    }
}
