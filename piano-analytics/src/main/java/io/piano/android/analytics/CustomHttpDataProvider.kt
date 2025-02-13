package io.piano.android.analytics

public interface CustomHttpDataProvider {
    public fun headers(): Map<String, String> = emptyMap()
    public fun parameters(): Map<String, String> = emptyMap()
}

internal object EmptyCustomHttpDataProvider : CustomHttpDataProvider
