package io.piano.android.analytics

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import okio.Buffer

internal object RawJsonAdapter {
    @FromJson
    @RawJson
    fun fromJson(reader: JsonReader): List<String> = buildList {
        reader.beginArray()
        while (reader.hasNext()) {
            add(reader.nextSource().readUtf8())
        }
        reader.endArray()
    }

    @ToJson
    fun toJson(writer: JsonWriter, @RawJson value: List<String>) {
        writer.beginArray().apply {
            value.forEach {
                value(Buffer().writeUtf8(it))
            }
        }.endArray()
    }
}
