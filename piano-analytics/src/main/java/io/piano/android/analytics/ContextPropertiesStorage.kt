package io.piano.android.analytics

import io.piano.android.analytics.model.ContextProperty
import io.piano.android.analytics.model.Event
import io.piano.android.analytics.model.Property
import io.piano.android.analytics.model.PropertyName

/**
 * Stores all customer context properties. Automatically removes non-persistent properties at adding them to event
 */
class ContextPropertiesStorage internal constructor(
    private val contextProperties: MutableList<ContextProperty> = mutableListOf(),
) {

    /**
     * Clears storage
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun clear() = contextProperties.clear()

    /**
     * Adds a [ContextProperty] into storage
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun add(property: ContextProperty) {
        val p = if (property.eventNames.isEmpty()) property.copy(eventNames = listOf(Event.ANY)) else property
        contextProperties.add(p)
    }

    /**
     * Removes property by its key
     * @param key property key
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    fun deleteByKey(key: PropertyName) {
        contextProperties.forEachIndexed { index, contextProperty ->
            contextProperty.properties.filterNotTo(mutableSetOf()) {
                it.name == key
            }.takeIf {
                it.size != contextProperty.properties.size
            }?.let {
                contextProperties[index] = contextProperty.copy(properties = it)
            }
        }
        contextProperties.removeAll { it.properties.isEmpty() }
    }

    internal fun getByEventName(eventName: String): List<Property> {
        val filtered = if (eventName == Event.ANY) {
            contextProperties.toList()
        } else {
            contextProperties.filter { cp ->
                cp.eventNames.any { it.wildcardMatches(eventName) }
            }
        }
        contextProperties.removeAll {
            it in filtered && !it.persistent
        }
        return filtered.flatMap { it.properties }
    }
}
