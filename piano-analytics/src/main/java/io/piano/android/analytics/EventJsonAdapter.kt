package io.piano.android.analytics

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import io.piano.android.analytics.model.Event
import io.piano.android.analytics.model.Property

internal class EventJsonAdapter(
    private val propertiesAdapter: JsonAdapter<Set<Property>>
) : JsonAdapter<Event>() {
    override fun fromJson(reader: JsonReader): Event? {
        TODO("Not supported")
    }

    override fun toJson(writer: JsonWriter, value: Event?) {
        requireNotNull(value)
        writer.beginObject()
            .name("name")
            .value(value.name)
            .name("data")
            .apply {
                propertiesAdapter.toJson(this, value.properties)
            }.endObject()
    }
}
