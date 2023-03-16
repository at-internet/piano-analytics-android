package io.piano.android.analytics.eventprocessors

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.piano.android.analytics.Configuration
import io.piano.android.analytics.PrivacyModesStorage
import io.piano.android.analytics.model.Event
import io.piano.android.analytics.model.PrivacyMode
import org.junit.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class PrivacyEventProcessorTest {
    private val privacyModesStorage = mock<PrivacyModesStorage> {
        on { currentMode } doReturn PrivacyMode.EXEMPT
    }
    private val configuration = mock<Configuration>()

    @Test
    fun process() {
        PrivacyMode.EXEMPT.allowedEventNames.apply {
            clear()
            add(DUMMY)
        }
        PrivacyMode.EXEMPT.forbiddenEventNames.apply {
            clear()
            add(DUMMY2)
        }
        val processor = PrivacyEventProcessor(configuration, privacyModesStorage)
        val allowedEvent = Event.Builder(DUMMY).build()
        val forbiddenEvent = Event.Builder(DUMMY2).build()
        val events = processor.process(
            listOf(
                allowedEvent,
                forbiddenEvent
            )
        )
        assertEquals(1, events.size)
        val eventNames = events.map { it.name }
        assertContains(eventNames, allowedEvent.name)
        assertFalse { forbiddenEvent.name in eventNames }
    }

    @Test
    fun processWithoutAllowedEvents() {
        PrivacyMode.EXEMPT.allowedEventNames.clear()
        val processor = PrivacyEventProcessor(configuration, privacyModesStorage)
        assertEquals(emptyList<Event>(), processor.process(listOf(mock())))
    }

    @Test
    fun processOptOutNotSend() {
        val storage = mock<PrivacyModesStorage> {
            on { currentMode } doReturn PrivacyMode.OPTOUT
        }
        val customConfiguration = mock<Configuration> {
            on { sendEventWhenOptOut } doReturn false
        }
        val processor = PrivacyEventProcessor(customConfiguration, storage)
        assertEquals(emptyList<Event>(), processor.process(listOf(mock())))
    }

    companion object {
        private const val DUMMY = "dummy"
        private const val DUMMY2 = "dummy2"
    }
}
