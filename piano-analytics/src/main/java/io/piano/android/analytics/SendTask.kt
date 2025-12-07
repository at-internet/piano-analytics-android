package io.piano.android.analytics

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonWriter
import io.piano.android.analytics.idproviders.VisitorIdProvider
import io.piano.android.analytics.model.ConnectionType
import io.piano.android.analytics.model.EventRecord
import io.piano.android.analytics.model.EventsRequest
import io.piano.android.analytics.model.OfflineStorageMode
import okhttp3.Headers.Companion.toHeaders
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
    private val eventsJsonAdapter: JsonAdapter<EventsRequest>,
    private val customHttpDataProvider: CustomHttpDataProvider,
) : Runnable {
    override fun run() {
        eventRepository.deleteOldEvents(configuration.eventsOfflineStorageLifetime)
        eventRepository.deleteOutOfLimitNotSentEvents(EVENTS_LIMIT)
        if (deviceInfoProvider.connectionType == ConnectionType.OFFLINE) {
            Timber.w("Can't send events - no connection")
        } else {
            eventRepository.getNotSentEvents().chunked(CHUNK_SIZE).forEach {
                send(it)
            }
        }
        if (configuration.offlineStorageMode == OfflineStorageMode.NEVER) {
            eventRepository.deleteOldEvents(0)
        }
    }

    internal fun send(events: List<EventRecord>) {
        runCatching {
            val url = HttpUrl.Builder()
                .scheme("https")
                .host(configuration.collectDomain)
                .addEncodedPathSegment(configuration.path)
                .apply {
                    customHttpDataProvider.parameters().forEach { (key, value) ->
                        addQueryParameter(key, value)
                    }
                }
                .addQueryParameter("s", configuration.site.toString())
                .addQueryParameter("idclient", visitorIdProvider.visitorId)
                .build()
            val requestBody = with(Buffer()) {
                eventsJsonAdapter.toJson(JsonWriter.of(this), EventsRequest(events.map { it.data }))
                readByteString().toRequestBody(MEDIA_TYPE)
            }
            val request = Request.Builder()
                .headers(customHttpDataProvider.headers().toHeaders())
                .url(url)
                .post(requestBody)
                .build()
            okHttpClient.newCall(request).execute()
        }.onFailure {
            Timber.w(it)
        }.onSuccess {
            eventRepository.markEventsAsSent(events)
        }
    }

    companion object {
        internal val MEDIA_TYPE by lazy(LazyThreadSafetyMode.NONE) { "application/json; charset=UTF-8".toMediaType() }
        private const val CHUNK_SIZE = 50
        private const val EVENTS_LIMIT = 2000
    }
}
