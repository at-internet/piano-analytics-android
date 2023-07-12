package io.piano.android.analytics.eventprocessors

import io.piano.android.analytics.Configuration
import io.piano.android.analytics.PrivacyModesStorage
import io.piano.android.analytics.model.Event
import io.piano.android.analytics.model.PrivacyMode
import io.piano.android.analytics.model.Property
import io.piano.android.analytics.model.PropertyName
import io.piano.android.analytics.wildcardMatches
import timber.log.Timber

internal class PrivacyEventProcessor(
    private val configuration: Configuration,
    private val privacyModesStorage: PrivacyModesStorage,
) : EventProcessor {

    override fun process(events: List<Event>): List<Event> {
        with(privacyModesStorage.currentMode) {
            if (this == PrivacyMode.OPTOUT && !configuration.sendEventWhenOptOut) {
                Timber.w("Privacy: user opted out and send when opt-out disabled")
                return emptyList()
            }

            val allowedEvents = allowedEventNames.simplify()
            val forbiddenEvents = forbiddenEventNames.simplify()
            if (allowedEvents.isEmpty()) {
                return emptyList()
            }

            val allowedPropertiesKeys = allowedPropertyKeys.simplify()
            val forbiddenPropertiesKeys = forbiddenPropertyKeys.simplify()
            return events.filter { event ->
                allowedEvents.any { it.wildcardMatches(event.name) } &&
                    !forbiddenEvents.any { it.wildcardMatches(event.name) }
            }.map { event ->
                val allowedProperties = allowedPropertiesKeys.propertyNamesForEvent(event.name)
                val forbiddenProperties = forbiddenPropertiesKeys.propertyNamesForEvent(event.name)
                Event.Builder(event.name)
                    .properties(event.properties.applyFilter(allowedProperties, forbiddenProperties))
                    .properties(
                        Property(PropertyName.VISITOR_PRIVACY_MODE, visitorMode),
                        Property(PropertyName.VISITOR_PRIVACY_CONSENT, visitorConsent)
                    )
                    .build()
            }
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Set<String>.simplify(): Set<String> =
        if (contains(WILDCARD)) {
            setOf(WILDCARD)
        } else {
            toSet()
        }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Map<String, Set<PropertyName>>.simplify(): Map<String, Set<PropertyName>> = mapValues {
        if (it.value.contains(PropertyName.ANY_PROPERTY)) {
            setOf(PropertyName.ANY_PROPERTY)
        } else {
            it.value
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Map<String, Set<PropertyName>>.propertyNamesForEvent(eventName: String): Set<PropertyName> =
        filterKeys {
            it.wildcardMatches(eventName)
        }.values.fold(setOf()) { result, item ->
            result + item
        }

    private fun Set<Property>.applyFilter(allowed: Set<PropertyName>, forbidden: Set<PropertyName>): Set<Property> {
        if (forbidden.contains(PropertyName.ANY_PROPERTY)) {
            return emptySet()
        }
        val sequence = asSequence()
        val filtered = if (allowed.contains(PropertyName.ANY_PROPERTY)) {
            sequence
        } else {
            sequence.filter { p ->
                p.name in allowed || allowed.any { it.key.wildcardMatches(p.name.key) }
            }
        }
        return filtered.filterNot { p ->
            p.name in forbidden || forbidden.any { it.key.wildcardMatches(p.name.key) }
        }.toSet()
    }

    companion object {
        internal const val WILDCARD = "*"
    }
}
