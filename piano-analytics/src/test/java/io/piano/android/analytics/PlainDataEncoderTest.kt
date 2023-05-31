package io.piano.android.analytics

import org.junit.Assert.assertEquals
import org.junit.Test

class PlainDataEncoderTest {

    @Test
    fun encode() {
        assertEquals(DUMMY, PlainDataEncoder.encode(DUMMY))
    }

    @Test
    fun decode() {
        assertEquals(DUMMY, PlainDataEncoder.decode(DUMMY))
    }

    companion object {
        private const val DUMMY = "dummy"
    }
}
