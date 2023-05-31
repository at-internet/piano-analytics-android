package io.piano.android.analytics.model

import java.util.Date

/**
 * Event property
 */
class Property {
    val name: PropertyName
    val value: Any

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return other is Property && name.key.equals(other.name.key, ignoreCase = true)
    }

    override fun hashCode(): Int = name.key.lowercase().hashCode()

    internal constructor(name: PropertyName, value: Any) {
        require(name != PropertyName.ANY_PROPERTY)
        this.name = name
        this.value = value
    }

    /**
     * Creates a new property
     *
     * @param name property name
     * @param value property value
     */
    constructor(name: PropertyName, value: String) {
        require(name != PropertyName.ANY_PROPERTY)
        this.name = name
        this.value = value
    }

    /**
     * Creates a new property
     *
     * @param name property name
     * @param value property value
     */
    constructor(name: PropertyName, value: Int) {
        require(name != PropertyName.ANY_PROPERTY)
        this.name = name
        this.value = value
    }

    /**
     * Creates a new property
     *
     * @param name property name
     * @param value property value
     */
    constructor(name: PropertyName, value: Long) {
        require(name != PropertyName.ANY_PROPERTY)
        this.name = name
        this.value = value
    }

    /**
     * Creates a new property
     *
     * @param name property name
     * @param value property value
     */
    constructor(name: PropertyName, value: Double) {
        require(name != PropertyName.ANY_PROPERTY)
        this.name = name
        this.value = value
    }

    /**
     * Creates a new property
     *
     * @param name property name
     * @param value property value
     */
    constructor(name: PropertyName, value: Date) : this(name, value.time / 1000)

    /**
     * Creates a new property
     *
     * @param name property name
     * @param value property value
     */
    constructor(name: PropertyName, value: Boolean) {
        require(name != PropertyName.ANY_PROPERTY)
        this.name = name
        this.value = value
    }

    /**
     * Creates a new property
     *
     * @param name property name
     * @param value property value
     */
    constructor(name: PropertyName, value: Array<String>) {
        require(name != PropertyName.ANY_PROPERTY)
        this.name = name
        this.value = value
    }

    /**
     * Creates a new property
     *
     * @param name property name
     * @param value property value
     */
    constructor(name: PropertyName, value: Array<Int>) {
        require(name != PropertyName.ANY_PROPERTY)
        this.name = name
        this.value = value
    }

    /**
     * Creates a new property
     *
     * @param name property name
     * @param value property value
     */
    constructor(name: PropertyName, value: Array<Double>) {
        require(name != PropertyName.ANY_PROPERTY)
        this.name = name
        this.value = value
    }
}
