package io.piano.android.analytics.eventprocessors

import io.piano.android.analytics.UserStorage
import io.piano.android.analytics.model.Event
import io.piano.android.analytics.model.Property
import io.piano.android.analytics.model.PropertyName

internal class UserEventProcessor(
    private val userStorage: UserStorage,
) : EventProcessor {
    override fun process(events: List<Event>): List<Event> = userStorage.currentUser?.let {
        val properties = mutableListOf(
            Property(PropertyName.USER_ID, it.id),
            Property(PropertyName.USER_RECOGNITION, userStorage.userRecognized)
        )
        if (it.category != null) {
            properties.add(Property(PropertyName.USER_CATEGORY, it.category))
        }
        events.map { event ->
            event.newBuilder().properties(properties).build()
        }
    } ?: events
}
