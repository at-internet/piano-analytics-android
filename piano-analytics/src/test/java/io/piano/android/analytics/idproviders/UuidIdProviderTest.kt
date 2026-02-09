package io.piano.android.analytics.idproviders

import io.piano.android.analytics.Configuration
import io.piano.android.analytics.PrefsStorage
import io.piano.android.analytics.model.VisitorStorageMode
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertFalse

class UuidIdProviderTest {
    private val configuration = mock<Configuration>()
    private val prefsStorage = mock<PrefsStorage>()
    private val idProvider = spy(UuidIdProvider(configuration, prefsStorage))

    @Test
    fun getVisitorIdNotExpired() {
        doReturn(DUMMY).whenever(prefsStorage).visitorUuid
        doReturn(2).whenever(configuration).visitorStorageLifetime
        val timestamp = System.currentTimeMillis()
        doReturn(timestamp).whenever(idProvider).getGenerationTimestamp()
        var isFirstCall = true
        whenever(prefsStorage.visitorUuidGenerateTimestamp).then {
            if (isFirstCall) {
                isFirstCall = false
                0L
            } else {
                timestamp
            }
        }
        doReturn(VisitorStorageMode.RELATIVE).whenever(configuration).visitorStorageMode
        idProvider.visitorId
        verify(prefsStorage, times(2)).visitorUuidGenerateTimestamp
        verify(configuration).visitorStorageMode
        verify(idProvider, never()).createNewUuid()
        verify(prefsStorage, never()).visitorUuid = anyString()
        verify(prefsStorage, times(2)).visitorUuidGenerateTimestamp
    }

    @Test
    fun getVisitorIdSavedExpired() {
        doReturn(DUMMY).whenever(prefsStorage).visitorUuid
        doReturn(-2).whenever(configuration).visitorStorageLifetime
        val timestamp = System.currentTimeMillis()
        doReturn(timestamp).whenever(idProvider).getGenerationTimestamp()
        doReturn(timestamp).whenever(prefsStorage).visitorUuidGenerateTimestamp
        idProvider.visitorId
        verify(prefsStorage, times(2)).visitorUuidGenerateTimestamp
        verify(configuration, never()).visitorStorageMode
        verify(idProvider).createNewUuid()
        verify(prefsStorage).visitorUuid = anyString()
        verify(prefsStorage).visitorUuidGenerateTimestamp = timestamp
    }

    @Test
    fun getVisitorIdNotSaved() {
        doReturn(null).whenever(prefsStorage).visitorUuid
        val timestamp = System.currentTimeMillis()
        doReturn(timestamp).whenever(idProvider).getGenerationTimestamp()
        idProvider.visitorId
        verify(prefsStorage, never()).visitorUuidGenerateTimestamp
        verify(configuration, never()).visitorStorageMode
        verify(idProvider).createNewUuid()
        verify(prefsStorage).visitorUuid = anyString()
        verify(prefsStorage).visitorUuidGenerateTimestamp = timestamp
    }

    @Test
    fun isLimitAdTrackingEnabled() {
        assertFalse { idProvider.isLimitAdTrackingEnabled }
    }

    companion object {
        private const val DUMMY = "dummy"
    }
}
