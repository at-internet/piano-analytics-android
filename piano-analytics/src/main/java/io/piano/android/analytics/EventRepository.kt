package io.piano.android.analytics

import com.squareup.moshi.JsonAdapter
import io.piano.android.analytics.model.Event
import io.piano.android.analytics.model.EventRecord
import java.util.concurrent.TimeUnit

internal class EventRepository(
    private val databaseHelper: DatabaseHelper,
    private val eventAdapter: JsonAdapter<Event>,
) {
    fun putEvents(events: Collection<Event>) {
        events.forEach { e ->
            databaseHelper.save(EventRecord(eventAdapter.toJson(e)))
        }
    }

    fun deleteOldEvents(eventsOfflineStorageLifetime: Int) {
        databaseHelper.delete(
            "${EventRecord.TIME} < ?",
            (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(eventsOfflineStorageLifetime.toLong())).toString()
        )
    }

    fun getNotSentEvents(): List<EventRecord> = databaseHelper.query(
        selection = "${EventRecord.IS_SENT} = 0",
        orderBy = "${EventRecord.TIME} ASC"
    )

    fun markEventsAsSent(events: Collection<EventRecord>) {
        events.forEach { e ->
            e.isSent = true
            databaseHelper.save(e)
        }
    }
}
