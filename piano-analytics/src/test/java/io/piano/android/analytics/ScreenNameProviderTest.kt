package io.piano.android.analytics

import android.app.Activity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class ScreenNameProviderTest {
    private val screenNameProvider = ScreenNameProvider()

    @Test
    fun getScreenName() {
        screenNameProvider.currentActivityName = DUMMY
        assertEquals(DUMMY, screenNameProvider.screenName)
        screenNameProvider.customScreenName = DUMMY2
        assertEquals(DUMMY2, screenNameProvider.screenName)
        screenNameProvider.customScreenName = null
        assertEquals(DUMMY, screenNameProvider.screenName)
    }

    @Test
    fun onActivityCreated() {
        screenNameProvider.customScreenName = DUMMY
        screenNameProvider.onActivityCreated(mock(), null)
        assertEquals(null, screenNameProvider.customScreenName)
    }

    @Test
    fun onActivityResumed() {
        val activity = mock<Activity> {
            on { localClassName } doReturn DUMMY
        }
        screenNameProvider.currentActivityName = DUMMY2
        screenNameProvider.onActivityResumed(activity)
        assertEquals(DUMMY, screenNameProvider.currentActivityName)
    }

    companion object {
        private const val DUMMY = "dummy"
        private const val DUMMY2 = "dummy2"
    }
}
