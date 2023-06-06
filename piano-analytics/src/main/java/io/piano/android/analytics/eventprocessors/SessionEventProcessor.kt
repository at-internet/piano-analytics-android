package io.piano.android.analytics.eventprocessors

import io.piano.android.analytics.SessionStorage
import io.piano.android.analytics.model.Event
import io.piano.android.analytics.model.Property
import io.piano.android.analytics.model.PropertyName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal class SessionEventProcessor(
    private val sessionStorage: SessionStorage
) : EventProcessor {
    override fun process(events: List<Event>): List<Event> {
        val commonProperties = setOf(
            Property(PropertyName.APP_FIRST_SESSION, sessionStorage.isFirstSession),
            Property(PropertyName.APP_FIRST_SESSION_AFTER_UPDATE, sessionStorage.isFirstSessionAfterUpdate),
            Property(PropertyName.APP_SESSION_COUNT, sessionStorage.sessionCount),
            Property(PropertyName.APP_DAYS_SINCE_LAST_SESSION, sessionStorage.daysSinceLastSession),
            Property(PropertyName.APP_DAYS_SINCE_FIRST_SESSION, sessionStorage.daysSinceFirstSession),
            Property(
                PropertyName.APP_FIRST_SESSION_DATE,
                sessionStorage.firstSessionTimestamp.asReportDate()
            ),
            Property(PropertyName.APP_SESSION_ID, sessionStorage.sessionId)
        )

        val afterUpdateProperties = if (sessionStorage.isFirstSessionAfterUpdate) {
            setOf(
                Property(PropertyName.APP_SESSION_COUNT_SINCE_UPDATE, sessionStorage.sessionCountAfterUpdate),
                Property(
                    PropertyName.APP_FIRST_SESSION_DATE_AFTER_UPDATE,
                    sessionStorage.firstSessionTimestampAfterUpdate.asReportDate()
                ),
                Property(PropertyName.APP_DAYS_SINCE_UPDATE, sessionStorage.daysSinceUpdate)
            )
        } else {
            emptySet()
        }

        return events.map { event ->
            event.newBuilder()
                .properties(commonProperties)
                .properties(afterUpdateProperties)
                .build()
        }
    }

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
        private fun Long.asReportDate() = DATE_FORMAT.format(Date(this)).toInt() // fixme: do we really need it?
    }
}
