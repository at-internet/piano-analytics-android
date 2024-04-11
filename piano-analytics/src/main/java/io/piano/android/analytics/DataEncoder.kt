package io.piano.android.analytics

/**
 * An events data encoder. Can be used for encrypting/decrypting events' data at saving in device storage
 * Your implementation must satisfy condition `decode(encode(data)) == data` for any `data`
 */
public interface DataEncoder {
    public fun encode(data: String): String
    public fun decode(data: String): String
}
