package io.piano.android.analytics.eventprocessors

import io.piano.android.analytics.UserStorage
import io.piano.android.analytics.model.Event
import io.piano.android.analytics.model.User
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class UserEventProcessorTest {

    @Test
    fun process() {
        val events = listOf(
            Event.Builder(DUMMY).build(),
        )
        val user = User(DUMMY, DUMMY2)
        val userStorage = mock<UserStorage> {
            on { currentUser } doReturn user
            on { userRecognized } doReturn true
        }
        val processor = UserEventProcessor(userStorage)
        val processedEvents = processor.process(events)
        assertEquals(1, processedEvents.size)
        assertEquals(3, processedEvents.first().properties.size)
    }

    @Test
    fun processWithoutUser() {
        val events = listOf(mock<Event>())
        val userStorage = mock<UserStorage>()
        val processor = UserEventProcessor(userStorage)
        assertEquals(events, processor.process(events))
    }

    companion object {
        private const val DUMMY = "dummy"
        private const val DUMMY2 = "dummy2"
    }
}
