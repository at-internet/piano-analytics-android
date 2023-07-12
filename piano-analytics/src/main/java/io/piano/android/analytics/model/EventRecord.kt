package io.piano.android.analytics.model

import android.provider.BaseColumns

internal class EventRecord(
    val data: String,
    val timestamp: Long = System.currentTimeMillis(),
    var id: Long? = null,
    var isSent: Boolean = false,
) {
    val isValid = data.startsWith("{") && data.endsWith("}")

    companion object {
        internal const val ID = BaseColumns._ID
        internal const val DATA = "data"
        internal const val TIME = "time"
        internal const val IS_SENT = "isSent"
        internal const val TABLE_NAME = "events"

        @JvmStatic
        val COLUMNS = arrayOf(
            ID,
            DATA,
            TIME,
            IS_SENT
        )
    }
}
