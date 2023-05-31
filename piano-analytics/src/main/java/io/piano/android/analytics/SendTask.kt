package io.piano.android.analytics

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonWriter
import io.piano.android.analytics.idproviders.VisitorIdProvider
import io.piano.android.analytics.model.ConnectionType
import io.piano.android.analytics.model.EventRecord
import io.piano.android.analytics.model.EventsRequest
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okio.Buffer
import timber.log.Timber

internal class SendTask(
    private val configuration: Configuration,
    private val eventRepository: EventRepository,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val visitorIdProvider: VisitorIdProvider,
    private val okHttpClient: OkHttpClient,
    private val eventsJsonAdapter: JsonAdapter<EventsRequest>
) : Runnable {
    override fun run() {
        eventRepository.deleteOldEvents(configuration.eventsOfflineStorageLifetime)
        if (deviceInfoProvider.connectionType == ConnectionType.OFFLINE) {
            Timber.w("Can't send events - no connection")
            return
        }
        send(eventRepository.getNotSentEvents())
    }

    internal fun send(events: List<EventRecord>) {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host(configuration.collectDomain)
            .addEncodedPathSegment(configuration.path)
            .addQueryParameter("s", configuration.site.toString())
            .addQueryParameter("idclient", visitorIdProvider.visitorId)
            .build()
        val requestBody = with(Buffer()) {
            eventsJsonAdapter.toJson(JsonWriter.of(this), EventsRequest(events.map { it.data }))
            readByteString().toRequestBody(MEDIA_TYPE)
        }
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        runCatching {
            okHttpClient.newCall(request).execute()
        }.onFailure {
            Timber.w(it)
        }.onSuccess {
            eventRepository.markEventsAsSent(events)
        }
    }

    companion object {
        internal val MEDIA_TYPE by lazy(LazyThreadSafetyMode.NONE) { "application/json; charset=UTF-8".toMediaType() }
    }
}
