package io.piano.android.analytics.idproviders

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CustomIdProviderTest {
    private val idProvider = CustomIdProvider()

    @Test
    fun isLimitAdTrackingEnabled() {
        assertFalse { idProvider.isLimitAdTrackingEnabled }
    }

    @Test
    fun getVisitorId() {
        assertEquals(null, idProvider.visitorId)
        idProvider.visitorId = DUMMY
        assertEquals(DUMMY, idProvider.visitorId)
    }

    companion object {
        private const val DUMMY = "dummy"
    }
}
