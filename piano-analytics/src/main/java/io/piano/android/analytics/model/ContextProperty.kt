package io.piano.android.analytics.model

/**
 * Customer context property
 *
 * @param properties properties, which will be added to events
 * @param persistent specifies will [properties] be persistent or not.
 * @param eventNames specifies with which events the [properties] will be sent
 * @constructor Creates a customer context property
 */
public data class ContextProperty @JvmOverloads constructor(
    val properties: Set<Property>,
    val persistent: Boolean = false,
    val eventNames: Collection<String> = emptyList(),
)
