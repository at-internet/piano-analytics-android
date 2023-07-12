package io.piano.android.analytics.eventprocessors

import io.piano.android.analytics.ContextPropertiesStorage
import io.piano.android.analytics.model.Event

internal class ContextPropertiesEventProcessor(
    private val contextPropertiesStorage: ContextPropertiesStorage,
) : EventProcessor {

    override fun process(events: List<Event>): List<Event> = events.map { event ->
        val eventPropertiesKeys = event.properties.map { it.name }
        contextPropertiesStorage.getByEventName(event.name)
            .filter { it.name !in eventPropertiesKeys }
            .takeUnless { it.isEmpty() }
            ?.let {
                event.newBuilder()
                    .properties(it)
                    .build()
            } ?: event
    }
}
