package io.piano.android.analytics

import com.squareup.moshi.JsonAdapter
import io.piano.android.analytics.idproviders.VisitorIdProvider
import io.piano.android.analytics.model.ConnectionType
import io.piano.android.analytics.model.EventRecord
import io.piano.android.analytics.model.EventsRequest
import io.piano.android.analytics.model.OfflineStorageMode
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Response
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SendTaskTest {

    private val eventRepository: EventRepository = mock()
    private val deviceInfoProvider: DeviceInfoProvider = mock {
        on { connectionType } doReturn ConnectionType.WIFI
    }
    private val visitorIdProvider: VisitorIdProvider = mock {
        on { visitorId } doReturn "test-visitor"
    }
    private val response: Response = mock()
    private val call: Call = mock {
        on { execute() } doReturn response
    }
    private val okHttpClient: OkHttpClient = mock {
        on { newCall(any()) } doReturn call
    }
    private val eventsJsonAdapter: JsonAdapter<EventsRequest> = mock()

    @Test
    fun `run should not send events when offline`() {
        whenever(deviceInfoProvider.connectionType).thenReturn(ConnectionType.OFFLINE)

        createSendTask().run()

        verify(eventRepository, never()).getNotSentEvents()
        verify(okHttpClient, never()).newCall(any())
    }

    @Test
    fun `run should delete events if offline storage is never`() {
        whenever(eventRepository.getNotSentEvents()).thenReturn(emptyList())

        createSendTask(offlineStorageMode = OfflineStorageMode.NEVER).run()

        verify(eventRepository).deleteOldEvents(0)
    }

    @Test
    fun `send should not mark events as sent on failure`() {
        val events = listOf(EventRecord("{}"))
        whenever(call.execute()).thenThrow(RuntimeException())

        createSendTask().send(events)

        verify(eventRepository, never()).markEventsAsSent(events)
    }

    @Test
    fun `send should mark events as sent on success`() {
        val events = listOf(EventRecord("{}"))

        createSendTask().send(events)

        verify(okHttpClient).newCall(any())
        verify(eventRepository).markEventsAsSent(events)
    }

    private fun createSendTask(offlineStorageMode: OfflineStorageMode = OfflineStorageMode.REQUIRED) = SendTask(
        Configuration.Builder(
            collectDomain = "example.com",
            site = 1,
            offlineStorageMode = offlineStorageMode,
        ).build(),
        eventRepository,
        deviceInfoProvider,
        visitorIdProvider,
        okHttpClient,
        eventsJsonAdapter,
        EmptyCustomHttpDataProvider,
    )
}
