package io.piano.android.analytics.eventprocessors

import android.content.pm.PackageInfo
import android.util.DisplayMetrics
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import io.piano.android.analytics.Configuration
import io.piano.android.analytics.DeviceInfoProvider
import io.piano.android.analytics.model.ConnectionType
import io.piano.android.analytics.model.Event
import io.piano.android.analytics.model.OfflineStorageMode

import org.junit.Test
import kotlin.test.assertEquals

class InternalPropertiesEventProcessorTest {
    private val event = Event.Builder(DUMMY).build()
    private val metrics = DisplayMetrics().apply {
        widthPixels = 1
        heightPixels = 2
    }

    @Test
    fun processWithAppData() {
        val configuration = mock<Configuration> {
            on { offlineStorageMode } doReturn OfflineStorageMode.REQUIRED
        }
        val deviceInfoProvider = mock<DeviceInfoProvider> {
            on { displayMetrics } doReturn metrics
            on { packageInfo } doReturn PackageInfo().apply {
                packageName = DUMMY
                versionName = DUMMY
            }
            on { connectionType } doReturn ConnectionType.MOBILE
        }
        val processor = InternalPropertiesEventProcessor(configuration, deviceInfoProvider)
        val events = processor.process(listOf(event))
        verify(deviceInfoProvider).displayMetrics
        verify(deviceInfoProvider).packageInfo
        verify(deviceInfoProvider).connectionType
        assertEquals(1, events.size)
        assertEquals(APP_PROPERTIES_SIZE + COMMON_PROPERTIES_SIZE, events.first().properties.size)
    }

    @Test
    fun processOfflineWithoutAppData() {
        val configuration = mock<Configuration> {
            on { offlineStorageMode } doReturn OfflineStorageMode.ALWAYS
        }
        val deviceInfoProvider = mock<DeviceInfoProvider> {
            on { displayMetrics } doReturn metrics
        }
        val processor = InternalPropertiesEventProcessor(configuration, deviceInfoProvider)
        val events = processor.process(listOf(event))
        verify(deviceInfoProvider).displayMetrics
        verify(deviceInfoProvider).packageInfo
        verify(deviceInfoProvider, never()).connectionType
        assertEquals(1, events.size)
        assertEquals(COMMON_PROPERTIES_SIZE, events.first().properties.size)
    }

    companion object {
        private const val DUMMY = "dummy"
        private const val COMMON_PROPERTIES_SIZE = 13
        private const val APP_PROPERTIES_SIZE = 2
    }
}
