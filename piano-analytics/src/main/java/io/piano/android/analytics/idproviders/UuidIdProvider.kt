package io.piano.android.analytics.idproviders

import io.piano.android.analytics.Configuration
import io.piano.android.analytics.PrefsStorage
import io.piano.android.analytics.model.VisitorStorageMode
import java.util.UUID
import java.util.concurrent.TimeUnit

internal class UuidIdProvider(
    private val configuration: Configuration,
    private val prefsStorage: PrefsStorage
) : IdProvider {
    override val visitorId: String?
        get() {
            val now = getGenerationTimestamp()
            val uuid = prefsStorage.visitorUuid?.let {
                if (prefsStorage.visitorUuidGenerateTimestamp == 0L)
                    prefsStorage.visitorUuidGenerateTimestamp = now
                val expireTimestamp = prefsStorage.visitorUuidGenerateTimestamp + TimeUnit.DAYS.toMillis(
                    configuration.visitorStorageLifetime.toLong()
                )
                if (expireTimestamp > now) {
                    if (configuration.visitorStorageMode == VisitorStorageMode.RELATIVE) {
                        prefsStorage.visitorUuidGenerateTimestamp = now
                    }
                    it
                } else null
            } ?: createNewUuid()
            return uuid
        }
    override val isLimitAdTrackingEnabled: Boolean = false

    internal fun createNewUuid() =
        UUID.randomUUID().toString().also {
            prefsStorage.visitorUuid = it
            prefsStorage.visitorUuidGenerateTimestamp = getGenerationTimestamp()
        }

    // for mocking in tests
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun getGenerationTimestamp() = System.currentTimeMillis()
}
