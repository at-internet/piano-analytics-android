package io.piano.android.analytics.model

import java.util.Collections

public class Event private constructor(
    public val name: String,
    public val properties: Set<Property>,
) {
    public fun newBuilder(): Builder = Builder(name, properties.toMutableSet())

    public data class Builder constructor(
        var name: String,
        val properties: MutableSet<Property> = mutableSetOf(),
    ) {
        public fun name(name: String): Builder = apply { this.name = name }
        public fun properties(vararg properties: Property): Builder = apply { this.properties.addAll(properties) }
        public fun properties(properties: Collection<Property>): Builder = apply { this.properties.addAll(properties) }

        public fun build(): Event {
            check(name.isNotBlank()) {
                "Event name should be filled"
            }
            return Event(
                name,
                Collections.unmodifiableSet(properties)
            )
        }
    }

    public companion object {
        public const val ANY: String = "*"
        public const val PAGE_DISPLAY: String = "page.display"
        public const val CLICK_ACTION: String = "click.action"
        public const val CLICK_NAVIGATION: String = "click.navigation"
        public const val CLICK_DOWNLOAD: String = "click.download"
        public const val CLICK_EXIT: String = "click.exit"
        public const val PUBLISHER_IMPRESSION: String = "publisher.impression"
        public const val PUBLISHER_CLICK: String = "publisher.click"
        public const val SELF_PROMOTION_IMPRESSION: String = "self_promotion.impression"
        public const val SELF_PROMOTION_CLICK: String = "self_promotion.click"
        public const val INTERNAL_SEARCH_RESULT_DISPLAY: String = "internal_search_result.display"
        public const val INTERNAL_SEARCH_RESULT_CLICK: String = "internal_search_result.click"
        public const val MV_TEST_DISPLAY: String = "mv_test.display"
    }
}
