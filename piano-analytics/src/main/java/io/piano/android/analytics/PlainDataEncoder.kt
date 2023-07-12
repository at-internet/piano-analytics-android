package io.piano.android.analytics

object PlainDataEncoder : DataEncoder {
    override fun encode(data: String): String = data

    override fun decode(data: String): String = data
}
