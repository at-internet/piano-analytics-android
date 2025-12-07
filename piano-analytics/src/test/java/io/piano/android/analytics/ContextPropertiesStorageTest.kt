package io.piano.android.analytics

import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import io.piano.android.analytics.model.ContextProperty
import io.piano.android.analytics.model.Event
import io.piano.android.analytics.model.Property
import io.piano.android.analytics.model.PropertyName
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ContextPropertiesStorageTest {
    private val properties = spy(mutableListOf<ContextProperty>())
    private val propertiesStorage = ContextPropertiesStorage(properties)

    @Test
    fun clear() {
        propertiesStorage.clear()
        verify(properties).clear()
    }

    @Test
    fun add() {
        val contextProperty = ContextProperty(
            setOf(Property(PropertyName.CLICK, DUMMY)),
        )
        propertiesStorage.add(contextProperty)
        verify(properties).add(any())
        assertTrue { properties.size == 1 }
        assertEquals(properties.first().eventNames, listOf(Event.ANY))
    }

    @Test
    fun addWithEventNames() {
        val contextProperty = ContextProperty(
            setOf(Property(PropertyName.CLICK, DUMMY)),
            eventNames = listOf(Event.PAGE_DISPLAY),
        )
        propertiesStorage.add(contextProperty)
        verify(properties).add(any())
        assertTrue { properties.size == 1 }
        assertContains(properties, contextProperty)
    }

    @Test
    fun deleteByKey() {
        val contextProperty = ContextProperty(
            setOf(Property(PropertyName.CLICK, DUMMY)),
            eventNames = listOf(Event.PAGE_DISPLAY),
        )
        properties.add(contextProperty)
        propertiesStorage.deleteByKey(PropertyName.PAGE)
        verify(properties, never()).remove(any())
        assertContains(properties, contextProperty)
        propertiesStorage.deleteByKey(PropertyName.CLICK)
        verify(properties).removeAt(anyInt())
        assertTrue { properties.isEmpty() }
    }

    @Test
    fun getByEventName() {
        val property = Property(PropertyName.CLICK, DUMMY)
        val contextProperty = ContextProperty(
            setOf(property),
            eventNames = listOf(Event.PAGE_DISPLAY),
        )
        properties.add(contextProperty)
        assertEquals(
            emptyList(),
            propertiesStorage.getByEventName("*test"),
        )
        assertTrue { properties.size == 1 }
        assertEquals(
            emptyList(),
            propertiesStorage.getByEventName(Event.CLICK_ACTION),
        )
        assertTrue { properties.size == 1 }
        assertEquals(
            listOf(property),
            propertiesStorage.getByEventName(Event.PAGE_DISPLAY),
        )
        assertTrue { properties.isEmpty() }
    }

    @Test
    fun getByEventNameAny() {
        val property = Property(PropertyName.CLICK, DUMMY)
        val contextProperty = ContextProperty(
            setOf(property),
            eventNames = listOf(Event.PAGE_DISPLAY),
        )
        properties.add(contextProperty)
        assertEquals(
            listOf(property),
            propertiesStorage.getByEventName(Event.ANY),
        )
        assertTrue { properties.isEmpty() }
    }

    @Test
    fun getByEventNamePersistent() {
        val property = Property(PropertyName.CLICK, DUMMY)
        val contextProperty = ContextProperty(
            setOf(property),
            persistent = true,
            eventNames = listOf("page.*"),
        )
        properties.add(contextProperty)
        assertEquals(
            listOf(property),
            propertiesStorage.getByEventName(Event.PAGE_DISPLAY),
        )
        assertTrue { properties.size == 1 }
    }

    companion object {
        private const val DUMMY = "dummy"
    }
}
