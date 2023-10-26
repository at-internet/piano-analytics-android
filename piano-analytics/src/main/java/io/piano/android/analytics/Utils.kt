package io.piano.android.analytics

import android.annotation.SuppressLint
import timber.log.Timber

internal fun String.wildcardMatches(string: String): Boolean {
    val index = indexOf("*")
    return index == 0 || (index == -1 && this == string) || (index > 0 && string.startsWith(substring(0, index)))
}

internal fun isLogHttpSet(): Boolean = getProperty(LOG_HTTP_KEY) == "true"

@SuppressLint("PrivateApi")
@Suppress("SameParameterValue")
private fun getProperty(key: String): String? = runCatching {
    Class.forName("android.os.SystemProperties")
        .getMethod("get", String::class.java, String::class.java)
        .invoke(null, key, null) as? String
}.onFailure {
    Timber.w(it, "can't get value %s from SystemProperties", key)
}.getOrNull()

private const val LOG_HTTP_KEY = "debug.piano.sdk"
