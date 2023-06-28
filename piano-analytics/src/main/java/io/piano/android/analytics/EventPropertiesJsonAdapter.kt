package io.piano.android.analytics

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import io.piano.android.analytics.model.Property
import io.piano.android.analytics.model.PropertyName

internal class EventPropertiesJsonAdapter(
    private val mapAdapter: JsonAdapter<Map<String, Any>>,
) : JsonAdapter<Set<Property>>() {
    override fun fromJson(reader: JsonReader): Set<Property>? {
        val value = mapAdapter.fromJson(reader)
        return value?.entries?.mapNotNullTo(mutableSetOf()) { entry ->
            when (entry.value) {
                // just filter allowed types
                is String,
                is Int,
                is Long,
                is Double,
                is Boolean,
                is Array<*>,
                -> Property(PropertyName(entry.key), entry.value)

                else -> null
            }
        }
    }

    override fun toJson(writer: JsonWriter, value: Set<Property>?) {
        requireNotNull(value)
        mapAdapter.toJson(writer, value.associate { it.name.key.lowercase() to it.value })
    }
}
