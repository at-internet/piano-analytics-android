package io.piano.android.analytics

public object PlainDataEncoder : DataEncoder {
    override fun encode(data: String): String = data

    override fun decode(data: String): String = data
}
