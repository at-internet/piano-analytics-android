package io.piano.android.analytics

import com.squareup.moshi.JsonAdapter
import io.piano.android.analytics.model.Event
import io.piano.android.analytics.model.EventRecord
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class EventRepositoryTest {
    private val databaseHelper = mock<DatabaseHelper>()
    private val eventAdapter = mock<JsonAdapter<Event>> {
        on { toJson(any()) } doReturn DUMMY
    }
    private val eventRepository = EventRepository(databaseHelper, eventAdapter)

    @Test
    fun putEvents() {
        val events = listOf(
            Event.Builder(DUMMY).build(),
            Event.Builder(DUMMY).build(),
        )
        eventRepository.putEvents(events)
        verify(eventAdapter, times(events.size)).toJson(any())
        verify(databaseHelper, times(events.size)).save(any())
    }

    @Test
    fun deleteOldEvents() {
        eventRepository.deleteOldEvents(1)
        verify(databaseHelper).delete(eq("${EventRecord.TIME} < ?"), anyString())
    }

    @Test
    fun getNotSentEvents() {
        eventRepository.getNotSentEvents()
        verify(databaseHelper).query(
            selection = "${EventRecord.IS_SENT} = 0",
            orderBy = "${EventRecord.TIME} ASC",
        )
    }

    @Test
    fun markEventsAsSent() {
        val events = listOf(
            EventRecord(DUMMY),
            EventRecord(DUMMY),
        )
        eventRepository.markEventsAsSent(events)
        verify(databaseHelper, times(events.size)).save(any())
    }

    companion object {
        private const val DUMMY = "dummy"
    }
}
