package io.piano.android.analytics.eventprocessors

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.piano.android.analytics.SessionStorage
import io.piano.android.analytics.model.Event
import org.junit.Assert.assertEquals
import org.junit.Test

class SessionEventProcessorTest {
    private val eventsSource = listOf(
        Event.Builder(DUMMY).build(),
    )

    @Test
    fun process() {
        val sessionStorage = mock<SessionStorage> {
            on { isFirstSessionAfterUpdate } doReturn false
            on { sessionId } doReturn DUMMY
        }
        val processor = SessionEventProcessor(sessionStorage)
        val events = processor.process(eventsSource)
        assertEquals(1, events.size)
        assertEquals(COMMON_SIZE, events.first().properties.size)
    }

    @Test
    fun processAfterUpdate() {
        val sessionStorage = mock<SessionStorage> {
            on { isFirstSessionAfterUpdate } doReturn true
            on { sessionId } doReturn DUMMY
        }
        val processor = SessionEventProcessor(sessionStorage)
        val events = processor.process(eventsSource)
        assertEquals(1, events.size)
        assertEquals(COMMON_SIZE + AFTER_UPDATE_SIZE, events.first().properties.size)
    }

    companion object {
        private const val DUMMY = "dummy"
        private const val COMMON_SIZE = 7
        private const val AFTER_UPDATE_SIZE = 3
    }
}
