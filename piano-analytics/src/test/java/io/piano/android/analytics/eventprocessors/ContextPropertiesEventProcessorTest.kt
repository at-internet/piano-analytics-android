package io.piano.android.analytics.eventprocessors

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.piano.android.analytics.ContextPropertiesStorage
import io.piano.android.analytics.model.Event
import io.piano.android.analytics.model.Property
import io.piano.android.analytics.model.PropertyName
import org.junit.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ContextPropertiesEventProcessorTest {
    @Test
    fun process() {
        val existProperty = Property(PropertyName.CLICK, DUMMY)
        val newProperty = Property(PropertyName.EVENT_NAME, DUMMY)
        val contextPropertiesStorage = mock<ContextPropertiesStorage> {
            on { getByEventName(DUMMY) } doReturn listOf(newProperty, existProperty)
        }
        val processor = ContextPropertiesEventProcessor(contextPropertiesStorage)
        val events = processor.process(
            listOf(
                Event.Builder(DUMMY)
                    .properties(
                        Property(PropertyName.PAGE, DUMMY),
                        existProperty,
                    )
                    .build(),
            ),
        )
        assertEquals(1, events.size)
        events.first().apply {
            assertEquals(3, properties.size)
            assertContains(properties, newProperty)
            assertContains(properties, existProperty)
        }
        val event = Event.Builder(DUMMY)
            .properties(
                newProperty,
                existProperty,
            )
            .build()
        val events2 = processor.process(listOf(event))
        assertEquals(1, events2.size)
        assertContains(events2, event)
    }

    companion object {
        private const val DUMMY = "dummy"
    }
}
