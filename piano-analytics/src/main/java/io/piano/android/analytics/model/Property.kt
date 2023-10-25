package io.piano.android.analytics.model

import java.util.Date

/**
 * Event property
 */
class Property {
    val name: PropertyName
    val value: Any
    val forceType: Type?

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return other is Property && name.key.equals(other.name.key, ignoreCase = true)
    }

    override fun hashCode(): Int = name.key.lowercase().hashCode()

    internal constructor(name: PropertyName, value: Any, forceType: Type? = null) {
        require(name != PropertyName.ANY_PROPERTY)
        this.name = name
        this.value = value
        this.forceType = forceType
    }

    /**
     * Creates a new property
     *
     * @param name property name
     * @param value property value
     */
    constructor(name: PropertyName, value: String, forceType: Type? = null) {
        require(name != PropertyName.ANY_PROPERTY)
        this.name = name
        this.value = value
        this.forceType = forceType
    }

    /**
     * Creates a new property
     *
     * @param name property name
     * @param value property value
     */
    constructor(name: PropertyName, value: Int, forceType: Type? = null) {
        require(name != PropertyName.ANY_PROPERTY)
        this.name = name
        this.value = value
        this.forceType = forceType
    }

    /**
     * Creates a new property
     *
     * @param name property name
     * @param value property value
     */
    constructor(name: PropertyName, value: Long, forceType: Type? = null) {
        require(name != PropertyName.ANY_PROPERTY)
        this.name = name
        this.value = value
        this.forceType = forceType
    }

    /**
     * Creates a new property
     *
     * @param name property name
     * @param value property value
     */
    constructor(name: PropertyName, value: Double, forceType: Type? = null) {
        require(name != PropertyName.ANY_PROPERTY)
        this.name = name
        this.value = value
        this.forceType = forceType
    }

    /**
     * Creates a new property
     *
     * @param name property name
     * @param value property value
     */
    constructor(name: PropertyName, value: Date) : this(name, value.time / 1000, Type.DATE)

    /**
     * Creates a new property
     *
     * @param name property name
     * @param value property value
     */
    constructor(name: PropertyName, value: Boolean, forceType: Type? = null) {
        require(name != PropertyName.ANY_PROPERTY)
        this.name = name
        this.value = value
        this.forceType = forceType
    }

    /**
     * Creates a new property
     *
     * @param name property name
     * @param value property value
     */
    constructor(name: PropertyName, value: Array<String>, forceType: Type? = null) {
        require(name != PropertyName.ANY_PROPERTY)
        this.name = name
        this.value = value
        this.forceType = forceType
    }

    /**
     * Creates a new property
     *
     * @param name property name
     * @param value property value
     */
    constructor(name: PropertyName, value: Array<Int>, forceType: Type? = null) {
        require(name != PropertyName.ANY_PROPERTY)
        this.name = name
        this.value = value
        this.forceType = forceType
    }

    /**
     * Creates a new property
     *
     * @param name property name
     * @param value property value
     */
    constructor(name: PropertyName, value: Array<Double>, forceType: Type? = null) {
        require(name != PropertyName.ANY_PROPERTY)
        this.name = name
        this.value = value
        this.forceType = forceType
    }

    enum class Type(val prefix: String) {
        STRING("s"),
        INTEGER("n"),
        FLOAT("f"),
        DATE("d"),
        BOOLEAN("b"),
        STRING_ARRAY("a:s"),
        INTEGER_ARRAY("a:n"),
        FLOAT_ARRAY("a:f"),
    }
}
