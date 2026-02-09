package io.piano.android.analytics.idproviders

import android.content.Context
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GoogleAdvertisingIdProviderTest {
    private val context = mock<Context>()
    private val idProvider = spy(GoogleAdvertisingIdProvider(context))

    @Test
    fun withoutVisitorIdLimited() {
        doReturn(AdvertisingIdInfo(null, true)).whenever(idProvider).loadAdvertisingInfo()
        assertEquals(null, idProvider.visitorId)
        assertTrue { idProvider.isLimitAdTrackingEnabled }
    }

    @Test
    fun withoutAdvertisingInfo() {
        doReturn(null).whenever(idProvider).loadAdvertisingInfo()
        assertEquals(null, idProvider.visitorId)
        assertTrue { idProvider.isLimitAdTrackingEnabled }
    }
}
