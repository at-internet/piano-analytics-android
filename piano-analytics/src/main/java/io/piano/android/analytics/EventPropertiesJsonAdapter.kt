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
                -> {
                    val delimiterIndex = entry.key.lastIndexOf(DELIMITER)
                    val key = entry.key.substring(delimiterIndex + 1)
                    val type = if (delimiterIndex != -1) {
                        val prefix = entry.key.substring(0, delimiterIndex)
                        Property.Type.values().firstOrNull { it.prefix == prefix }
                    } else {
                        null
                    }
                    Property(PropertyName(key), entry.value, type)
                }

                else -> null
            }
        }
    }

    override fun toJson(writer: JsonWriter, value: Set<Property>?) {
        requireNotNull(value)
        mapAdapter.toJson(
            writer,
            value.associate {
                it.forceType?.prefix?.plus(DELIMITER).orEmpty() + it.name.key.lowercase() to it.value
            }
        )
    }

    companion object {
        private const val DELIMITER = ":"
    }
}
