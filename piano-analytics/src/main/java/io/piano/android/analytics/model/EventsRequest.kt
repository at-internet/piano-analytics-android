package io.piano.android.analytics.model

import com.squareup.moshi.JsonClass
import io.piano.android.analytics.RawJson

@JsonClass(generateAdapter = true)
internal class EventsRequest(
    @RawJson val events: List<String>,
)
