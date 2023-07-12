package io.piano.android.analytics.model

import java.util.Collections

class Event private constructor(
    val name: String,
    val properties: Set<Property>,
) {
    fun newBuilder(): Builder = Builder(name, properties.toMutableSet())

    data class Builder constructor(
        var name: String,
        val properties: MutableSet<Property> = mutableSetOf(),
    ) {
        fun name(name: String) = apply { this.name = name }
        fun properties(vararg properties: Property) = apply { this.properties.addAll(properties) }
        fun properties(properties: Collection<Property>) = apply { this.properties.addAll(properties) }

        fun build(): Event {
            check(name.isNotBlank()) {
                "Event name should be filled"
            }
            return Event(
                name,
                Collections.unmodifiableSet(properties)
            )
        }
    }

    companion object {
        const val ANY = "*"
        const val PAGE_DISPLAY = "page.display"
        const val CLICK_ACTION = "click.action"
        const val CLICK_NAVIGATION = "click.navigation"
        const val CLICK_DOWNLOAD = "click.download"
        const val CLICK_EXIT = "click.exit"
        const val PUBLISHER_IMPRESSION = "publisher.impression"
        const val PUBLISHER_CLICK = "publisher.click"
        const val SELF_PROMOTION_IMPRESSION = "self_promotion.impression"
        const val SELF_PROMOTION_CLICK = "self_promotion.click"
        const val INTERNAL_SEARCH_RESULT_DISPLAY = "internal_search_result.display"
        const val INTERNAL_SEARCH_RESULT_CLICK = "internal_search_result.click"
        const val MV_TEST_DISPLAY = "mv_test.display"
    }
}
