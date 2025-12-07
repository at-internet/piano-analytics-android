package io.piano.android.analytics

import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import com.squareup.moshi.JsonAdapter
import io.piano.android.analytics.model.ContextProperty
import io.piano.android.analytics.model.Property
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CrashReporterTest {

    @Test
    fun initialize() {
        val prefsStorage = mock<PrefsStorage> {
            on { crashInfo } doReturn DUMMY
        }
        val jsonAdapter = mock<JsonAdapter<Set<Property>>> {
            on { fromJson(anyString()) } doReturn setOf()
        }
        val storage = mock<ContextPropertiesStorage>()
        val crashReporter = CrashReporter(
            mock(),
            prefsStorage,
            DUMMY,
            mock(),
            storage,
            jsonAdapter,
        )
        crashReporter.initialize()
        verify(prefsStorage).crashInfo
        verify(jsonAdapter).fromJson(anyString())
        verify(storage).add(any())
    }

    @Test
    fun initializeNotParseable() {
        val e = IOException()
        val prefsStorage = mock<PrefsStorage> {
            on { crashInfo } doReturn DUMMY
        }
        val jsonAdapter = mock<JsonAdapter<Set<Property>>> {
            on { fromJson(anyString()) } doThrow e
        }
        val storage = mock<ContextPropertiesStorage>()
        val crashReporter = CrashReporter(
            mock(),
            prefsStorage,
            DUMMY,
            mock(),
            storage,
            jsonAdapter,
        )
        assertFailsWith<IOException> {
            crashReporter.initialize()
        }
        verify(prefsStorage).crashInfo
        verify(jsonAdapter).fromJson(anyString())
        verify(storage, never()).add(any())
    }

    @Test
    fun initializeNoCrash() {
        val prefsStorage = mock<PrefsStorage> {
            on { crashInfo } doReturn null
        }
        val jsonAdapter = mock<JsonAdapter<Set<Property>>>()
        val storage = mock<ContextPropertiesStorage>()
        val crashReporter = CrashReporter(
            mock(),
            prefsStorage,
            DUMMY,
            mock(),
            storage,
            jsonAdapter,
        )
        crashReporter.initialize()
        verify(prefsStorage).crashInfo
        verify(jsonAdapter, never()).fromJson(anyString())
        verify(storage, never()).add(any())
    }

    @Test
    fun processUncaughtException() {
        val configuration = mock<Configuration> {
            on { detectCrashes } doReturn true
        }
        val prefsStorage = mock<PrefsStorage> {
            on { crashInfo } doReturn null
        }
        val screenNameProvider = mock<ScreenNameProvider> {
            on { screenName } doReturn DUMMY
        }
        val jsonAdapter = mock<JsonAdapter<Set<Property>>> {
            on { toJson(any()) } doReturn DUMMY
        }
        val storage = mock<ContextPropertiesStorage>()
        val crashReporter = CrashReporter(
            configuration,
            prefsStorage,
            DUMMY,
            screenNameProvider,
            storage,
            jsonAdapter,
        )
        val argumentCaptor = argumentCaptor<ContextProperty>()
        crashReporter.processUncaughtException(mock(), Exception())
        verify(configuration).detectCrashes
        verify(screenNameProvider).screenName
        verify(jsonAdapter).toJson(any())
        verify(prefsStorage).crashInfo = DUMMY
        verify(storage).add(argumentCaptor.capture())
        assertEquals(3, argumentCaptor.lastValue.properties.size)
    }

    @Test
    fun processUncaughtExceptionDisabledDetect() {
        val configuration = mock<Configuration> {
            on { detectCrashes } doReturn false
        }
        val prefsStorage = mock<PrefsStorage>()
        val jsonAdapter = mock<JsonAdapter<Set<Property>>>()
        val storage = mock<ContextPropertiesStorage>()
        val crashReporter = CrashReporter(
            configuration,
            prefsStorage,
            DUMMY,
            mock(),
            storage,
            jsonAdapter,
        )
        crashReporter.processUncaughtException(mock(), mock())
        verify(configuration).detectCrashes
        verify(jsonAdapter, never()).toJson(any())
        verify(prefsStorage, never()).crashInfo
        verify(storage, never()).add(any())
    }

    companion object {
        private const val DUMMY = "dummy"
    }
}
