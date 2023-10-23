package io.piano.android.analytics

import android.content.SharedPreferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

internal inline fun <T> delegatedPropertyWithDefaultValue(
    delegateProperty: KMutableProperty0<T>,
    crossinline defaultValue: () -> T,
    crossinline valueFilter: (T) -> Boolean,
) = object : ReadWriteProperty<Any, T> {
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return delegateProperty.get().takeIf(valueFilter) ?: defaultValue().also { setValue(thisRef, property, it) }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        delegateProperty.set(value)
    }
}

internal fun <T> resettableProperty(resetValue: T, initializer: () -> T) = ResettableProperty(resetValue, initializer)

internal class ResettableProperty<T>(
    private val resetValue: T,
    private val initializer: () -> T,
) : ReadWriteProperty<Any, T> {
    private var value: T = resetValue
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        if (value == resetValue) {
            value = initializer()
        }
        return value
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        this.value = value
    }
}

internal val SharedPreferences.delegates get() = SharedPreferenceDelegates(this)

internal class SharedPreferenceDelegates(private val prefs: SharedPreferences) {
    fun boolean(
        default: Boolean = false,
        key: String? = null,
        canBeSaved: (key: String) -> Boolean = DEFAULT_SAVE_FILTER,
    ): ReadWriteProperty<Any, Boolean> = create(default, key, canBeSaved, prefs::getBoolean, prefs.edit()::putBoolean)

    fun int(
        default: Int = 0,
        key: String? = null,
        canBeSaved: (key: String) -> Boolean = DEFAULT_SAVE_FILTER,
    ): ReadWriteProperty<Any, Int> = create(default, key, canBeSaved, prefs::getInt, prefs.edit()::putInt)

    fun float(
        default: Float = 0f,
        key: String? = null,
        canBeSaved: (key: String) -> Boolean = DEFAULT_SAVE_FILTER,
    ): ReadWriteProperty<Any, Float> = create(default, key, canBeSaved, prefs::getFloat, prefs.edit()::putFloat)

    fun long(
        default: Long = 0L,
        key: String? = null,
        canBeSaved: (key: String) -> Boolean = DEFAULT_SAVE_FILTER,
    ): ReadWriteProperty<Any, Long> = create(default, key, canBeSaved, prefs::getLong, prefs.edit()::putLong)

    fun string(
        default: String = "",
        key: String? = null,
        canBeSaved: (key: String) -> Boolean = DEFAULT_SAVE_FILTER,
    ): ReadWriteProperty<Any, String> =
        create(default, key, canBeSaved, { k, d -> prefs.getString(k, d) as String }, prefs.edit()::putString)

    fun nullableString(
        default: String? = null,
        key: String? = null,
        canBeSaved: (key: String) -> Boolean = DEFAULT_SAVE_FILTER,
    ): ReadWriteProperty<Any, String?> =
        create(default, key, canBeSaved, { k, d -> prefs.getString(k, d) }, prefs.edit()::putString)

    private fun <T> create(
        default: T,
        key: String? = null,
        canBeSaved: (key: String) -> Boolean,
        getter: (key: String, default: T) -> T,
        setter: (key: String, value: T) -> SharedPreferences.Editor,
    ) = object : ReadWriteProperty<Any, T> {
        private fun key(property: KProperty<*>) = key ?: property.name

        override fun getValue(thisRef: Any, property: KProperty<*>): T = getter(key(property), default)

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            val propertyKey = key(property)
            if (canBeSaved(propertyKey)) {
                setter(propertyKey, value).apply()
            }
        }
    }

    companion object {
        @JvmStatic
        private val DEFAULT_SAVE_FILTER: (String) -> Boolean = { _ -> true }
    }
}
