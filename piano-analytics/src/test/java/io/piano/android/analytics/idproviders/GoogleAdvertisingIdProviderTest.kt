package io.piano.android.analytics.idproviders

import android.content.Context
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
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
