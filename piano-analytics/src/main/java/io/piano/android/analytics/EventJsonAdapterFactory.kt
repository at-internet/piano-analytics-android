package io.piano.android.analytics

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.piano.android.analytics.model.Event
import io.piano.android.analytics.model.Property
import java.lang.reflect.Type

internal class EventJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? =
        when (type) {
            EVENT_PROPERTIES_TYPE -> EventPropertiesJsonAdapter(
                moshi.adapter(
                    Types.newParameterizedType(
                        Map::class.java,
                        String::class.java,
                        Any::class.java
                    )
                )
            )

            Event::class.java -> EventJsonAdapter(
                moshi.adapter(EVENT_PROPERTIES_TYPE)
            )

            else -> null
        }

    companion object {
        @JvmStatic
        internal val EVENT_PROPERTIES_TYPE = Types.newParameterizedType(
            Set::class.java,
            Property::class.java
        )
    }
}
