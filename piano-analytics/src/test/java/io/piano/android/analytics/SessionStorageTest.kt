package io.piano.android.analytics

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import io.piano.android.analytics.model.PrivacyStorageFeature
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class SessionStorageTest {
    private val sharedPreferences = InMemorySharedPreferences()
    private val context = mock<Context> {
        on { getSharedPreferences(any(), any()) } doReturn sharedPreferences
    }
    private val prefsStorage = PrefsStorage(context)
    private lateinit var utilsMock: MockedStatic<*>

    @Before
    fun setUp() {
        // Turns `runOnMainThread` into a no-op: registering the lifecycle observer needs the Android main looper
        utilsMock = Mockito.mockStatic(Class.forName("io.piano.android.analytics.UtilsKt"))
    }

    @After
    fun tearDown() {
        utilsMock.close()
    }

    @Test
    fun firstSession() {
        val sessionStorage = createSessionStorage()
        assertFirstSession(sessionStorage)
    }

    @Test
    fun newSession() {
        val sessionStorage = createSessionStorage()
        sessionStorage.initNewSession()
        assertEquals(2, sessionStorage.sessionCount)
        assertFalse(sessionStorage.isFirstSession)
        assertFalse(sessionStorage.isFirstSessionAfterUpdate)
    }

    @Test
    fun newSessionAfterAppUpdate() {
        createSessionStorage(VERSION_CODE)
        val sessionStorage = createSessionStorage(VERSION_CODE + 1)
        assertEquals(2, sessionStorage.sessionCount)
        assertFalse(sessionStorage.isFirstSession)
        assertTrue(sessionStorage.isFirstSessionAfterUpdate)
        assertEquals(1, sessionStorage.sessionCountAfterUpdate)
        assertEquals(0, sessionStorage.daysSinceUpdate)
    }

    @Test
    fun lifecycleStorageClearedDuringSession() {
        val sessionStorage = createSessionStorage()
        sessionStorage.initNewSession()
        prefsStorage.cleanStorageFeature(PrivacyStorageFeature.LIFECYCLE)

        // events sent right after the privacy mode switch
        assertFirstSession(sessionStorage)

        // next session is not the first one after an app update: the app version did not change
        sessionStorage.initNewSession()
        assertEquals(2, sessionStorage.sessionCount)
        assertFalse(sessionStorage.isFirstSession)
        assertFalse(sessionStorage.isFirstSessionAfterUpdate)
    }

    @Test
    fun newSessionAfterLifecycleStorageCleared() {
        val sessionStorage = createSessionStorage()
        val sessionIdBeforeClear = sessionStorage.sessionId
        prefsStorage.cleanStorageFeature(PrivacyStorageFeature.LIFECYCLE)

        // app returns from background before any event was sent
        sessionStorage.initNewSession()
        assertFirstSession(sessionStorage)
        assertNotEquals(sessionIdBeforeClear, sessionStorage.sessionId)
    }

    @Test
    fun relaunchAfterLifecycleStorageCleared() {
        createSessionStorage()
        prefsStorage.cleanStorageFeature(PrivacyStorageFeature.LIFECYCLE)

        val sessionStorage = createSessionStorage()
        assertFirstSession(sessionStorage)

        sessionStorage.initNewSession()
        assertEquals(2, sessionStorage.sessionCount)
        assertFalse(sessionStorage.isFirstSessionAfterUpdate)
    }

    @Test
    fun lifecycleStorageForbidden() {
        prefsStorage.privacyStorageFilter = { false }
        val sessionStorage = createSessionStorage()
        assertFirstSession(sessionStorage)

        val firstSessionId = sessionStorage.sessionId
        sessionStorage.initNewSession()
        assertFirstSession(sessionStorage)
        assertNotEquals(firstSessionId, sessionStorage.sessionId)
    }

    private fun createSessionStorage(versionCode: Int = VERSION_CODE): SessionStorage {
        val info = mock<PackageInfo> {
            on { longVersionCode } doReturn versionCode.toLong()
        }
        // read instead of `longVersionCode` on API < 28
        @Suppress("DEPRECATION")
        info.versionCode = versionCode
        val deviceInfoProvider = mock<DeviceInfoProvider> {
            on { packageInfo } doReturn info
        }
        return SessionStorage(prefsStorage, deviceInfoProvider, SessionLifecycleListener(BACKGROUND_DURATION))
    }

    private fun assertFirstSession(sessionStorage: SessionStorage) {
        assertTrue(sessionStorage.isFirstSession)
        assertFalse(sessionStorage.isFirstSessionAfterUpdate)
        assertEquals(1, sessionStorage.sessionCount)
        assertEquals(0, sessionStorage.daysSinceFirstSession)
        assertEquals(0, sessionStorage.daysSinceLastSession)
    }

    companion object {
        private const val VERSION_CODE = 10
        private const val BACKGROUND_DURATION = 30L
    }
}

private class InMemorySharedPreferences : SharedPreferences {
    private val values = mutableMapOf<String?, Any?>()

    override fun getAll(): MutableMap<String?, *> = values.toMutableMap()

    override fun getString(key: String?, defValue: String?): String? = values[key] as String? ?: defValue

    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String?, defValues: MutableSet<String?>?): MutableSet<String?>? =
        values[key] as MutableSet<String?>? ?: defValues

    override fun getInt(key: String?, defValue: Int): Int = values[key] as Int? ?: defValue

    override fun getLong(key: String?, defValue: Long): Long = values[key] as Long? ?: defValue

    override fun getFloat(key: String?, defValue: Float): Float = values[key] as Float? ?: defValue

    override fun getBoolean(key: String?, defValue: Boolean): Boolean = values[key] as Boolean? ?: defValue

    override fun contains(key: String?): Boolean = key in values

    override fun edit(): SharedPreferences.Editor = Editor()

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?,
    ) = Unit

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?,
    ) = Unit

    private inner class Editor : SharedPreferences.Editor {
        // null value means removal
        private val changes = mutableMapOf<String?, Any?>()
        private var clearAll = false

        override fun putString(key: String?, value: String?): SharedPreferences.Editor = put(key, value)

        override fun putStringSet(key: String?, values: MutableSet<String?>?): SharedPreferences.Editor =
            put(key, values?.toMutableSet())

        override fun putInt(key: String?, value: Int): SharedPreferences.Editor = put(key, value)

        override fun putLong(key: String?, value: Long): SharedPreferences.Editor = put(key, value)

        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = put(key, value)

        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = put(key, value)

        override fun remove(key: String?): SharedPreferences.Editor = put(key, null)

        override fun clear(): SharedPreferences.Editor = also { clearAll = true }

        override fun commit(): Boolean {
            apply()
            return true
        }

        override fun apply() {
            if (clearAll) {
                values.clear()
            }
            changes.forEach { (key, value) ->
                if (value == null) values.remove(key) else values[key] = value
            }
            changes.clear()
            clearAll = false
        }

        private fun put(key: String?, value: Any?): SharedPreferences.Editor = also { changes[key] = value }
    }
}
