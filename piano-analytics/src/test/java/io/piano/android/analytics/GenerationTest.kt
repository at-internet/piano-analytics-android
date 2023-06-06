package io.piano.android.analytics

import kotlin.test.Test
import kotlin.test.assertNotEquals

class GenerationTest {
    @Test
    fun versionTest() {
        assertNotEquals("unspecified", BuildConfig.SDK_VERSION)
    }
}
