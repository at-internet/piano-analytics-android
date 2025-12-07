package io.piano.android.analytics.idproviders

import io.piano.android.analytics.Configuration
import io.piano.android.analytics.PrivacyModesStorage
import io.piano.android.analytics.model.PrivacyMode
import io.piano.android.analytics.model.VisitorIDType
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class VisitorIdProviderTest {
    private val configuration = mock<Configuration> {
        on { visitorIDType } doReturn VisitorIDType.ADVERTISING_ID
    }
    private val privacyModesStorage = mock<PrivacyModesStorage>()
    private val limitedTrackingIdProvider = spy(CustomIdProvider(DUMMY2))
    private val mockIdProvider = spy(CustomIdProvider(DUMMY))
    private val idByTypeProvider: (VisitorIDType) -> IdProvider = {
        mockIdProvider
    }
    private val idProvider = VisitorIdProvider(
        configuration,
        privacyModesStorage,
        limitedTrackingIdProvider,
        idByTypeProvider,
    )

    @Test
    fun getVisitorIdNotLimited() {
        doReturn(PrivacyMode.EXEMPT).whenever(privacyModesStorage).currentMode
        doReturn(false).whenever(mockIdProvider).isLimitAdTrackingEnabled
        doReturn(true).whenever(configuration).ignoreLimitedAdTracking
        assertEquals(DUMMY, idProvider.visitorId)
        verify(privacyModesStorage).currentMode
        verify(mockIdProvider).isLimitAdTrackingEnabled
        verify(configuration, never()).ignoreLimitedAdTracking
        verify(mockIdProvider).visitorId
        verify(limitedTrackingIdProvider, never()).visitorId
    }

    @Test
    fun getVisitorIdLimitedWithIgnore() {
        doReturn(PrivacyMode.EXEMPT).whenever(privacyModesStorage).currentMode
        doReturn(true).whenever(mockIdProvider).isLimitAdTrackingEnabled
        doReturn(true).whenever(configuration).ignoreLimitedAdTracking
        assertEquals(DUMMY2, idProvider.visitorId)
        verify(privacyModesStorage).currentMode
        verify(mockIdProvider).isLimitAdTrackingEnabled
        verify(configuration).ignoreLimitedAdTracking
        verify(limitedTrackingIdProvider).visitorId
    }

    @Test
    fun getVisitorIdLimitedWithoutIgnore() {
        doReturn(PrivacyMode.EXEMPT).whenever(privacyModesStorage).currentMode
        doReturn(true).whenever(mockIdProvider).isLimitAdTrackingEnabled
        doReturn(false).whenever(configuration).ignoreLimitedAdTracking
        assertEquals(VisitorIdProvider.OPT_OUT_ID, idProvider.visitorId)
        verify(privacyModesStorage).currentMode
        verify(mockIdProvider).isLimitAdTrackingEnabled
        verify(configuration).ignoreLimitedAdTracking
        verify(mockIdProvider, never()).visitorId
        verify(limitedTrackingIdProvider, never()).visitorId
    }

    @Test
    fun getVisitorIdNoStorage() {
        doReturn(PrivacyMode.NO_STORAGE).whenever(privacyModesStorage).currentMode
        assertEquals(VisitorIdProvider.NO_STORAGE_ID, idProvider.visitorId)
        verify(privacyModesStorage).currentMode
        verify(mockIdProvider, never()).isLimitAdTrackingEnabled
        verify(mockIdProvider, never()).visitorId
    }

    @Test
    fun getVisitorIdNoConsent() {
        doReturn(PrivacyMode.NO_CONSENT).whenever(privacyModesStorage).currentMode
        assertEquals(VisitorIdProvider.NO_CONSENT_ID, idProvider.visitorId)
        verify(privacyModesStorage).currentMode
        verify(mockIdProvider, never()).isLimitAdTrackingEnabled
        verify(mockIdProvider, never()).visitorId
    }

    @Test
    fun getVisitorIdOptOut() {
        doReturn(PrivacyMode.OPTOUT).whenever(privacyModesStorage).currentMode
        assertEquals(VisitorIdProvider.OPT_OUT_ID, idProvider.visitorId)
        verify(privacyModesStorage).currentMode
        verify(mockIdProvider, never()).isLimitAdTrackingEnabled
        verify(mockIdProvider, never()).visitorId
    }

    @Test
    fun isLimitAdTrackingEnabled() {
        assertFalse { idProvider.isLimitAdTrackingEnabled }
        verify(mockIdProvider).isLimitAdTrackingEnabled
    }

    companion object {
        private const val DUMMY = "dummy"
        private const val DUMMY2 = "dummy2"
    }
}
