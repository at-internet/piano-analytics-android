package io.piano.android.analytics.eventprocessors

import io.piano.android.analytics.model.Event

internal class GroupEventProcessor internal constructor(
    private val processors: MutableList<EventProcessor> = mutableListOf(),
) : EventProcessor, MutableList<EventProcessor> by processors {
    override fun process(events: List<Event>): List<Event> = processors.fold(events) { acc, eventProcessor ->
        eventProcessor.process(acc)
    }
}
