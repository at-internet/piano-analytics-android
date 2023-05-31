package io.piano.android.analytics.eventprocessors

import io.piano.android.analytics.model.Event

/**
 * An event processor. Can change any data in events
 */
fun interface EventProcessor {
    /**
     * You can add, remove or change events here.
     * Return list (can be empty) of events, which will be pushed to next [EventProcessor].
     *
     * @param events immutable list of events, which should be processed
     * @return list of events, which should be sent.
     */
    fun process(events: List<Event>): List<Event>
}
