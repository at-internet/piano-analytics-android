package io.piano.android.analytics.model

import java.util.Date

/**
 * Event property
 */
public class Property {
    public val name: PropertyName
    public val value: Any
    public val forceType: Type?

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return other is Property && name.key.equals(other.name.key, ignoreCase = true)
    }

    override fun hashCode(): Int = name.key.lowercase().hashCode()

    internal constructor(name: PropertyName, value: Any, forceType: Type?) {
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
    public constructor(name: PropertyName, value: String, forceType: Type? = null) {
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
    public constructor(name: PropertyName, value: Int, forceType: Type? = null) {
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
    public constructor(name: PropertyName, value: Long, forceType: Type? = null) {
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
    public constructor(name: PropertyName, value: Double, forceType: Type? = null) {
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
    public constructor(name: PropertyName, value: Date) : this(name, value.time / 1000, Type.DATE)

    /**
     * Creates a new property
     *
     * @param name property name
     * @param value property value
     */
    public constructor(name: PropertyName, value: Boolean, forceType: Type? = null) {
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
    public constructor(name: PropertyName, value: Array<String>, forceType: Type? = null) {
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
    public constructor(name: PropertyName, value: Array<Int>, forceType: Type? = null) {
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
    public constructor(name: PropertyName, value: Array<Double>, forceType: Type? = null) {
        require(name != PropertyName.ANY_PROPERTY)
        this.name = name
        this.value = value
        this.forceType = forceType
    }

    public enum class Type(
        public val prefix: String,
    ) {
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
