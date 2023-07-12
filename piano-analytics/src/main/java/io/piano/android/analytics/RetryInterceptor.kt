package io.piano.android.analytics

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import java.io.IOException

internal class RetryInterceptor(
    private val maxAttempts: Int = DEFAULT_MAX_ATTEMPTS,
    private val sleepBetweenAttempts: Long = DEFAULT_SLEEP_MS,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        for (attempt in 1..maxAttempts) {
            try {
                val response = chain.proceed(request)
                if (response.isSuccessful) {
                    return response
                }
                Timber.d("Unsuccessful request, retrying attempt $attempt")
            } catch (e: Exception) {
                Timber.d("Exception at request, retrying attempt $attempt")
                if (attempt == maxAttempts) {
                    throw e
                }
            }
            try {
                Thread.sleep(sleepBetweenAttempts)
            } catch (e: InterruptedException) {
                // ignore it
            }
        }
        throw IOException("Reached max retry count")
    }

    companion object {
        internal const val DEFAULT_MAX_ATTEMPTS = 3
        internal const val DEFAULT_SLEEP_MS = 400L
    }
}
