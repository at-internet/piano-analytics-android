package io.piano.android.analytics

import android.content.Context
import androidx.annotation.RestrictTo
import io.piano.android.analytics.model.PrivacyStorageFeature
import timber.log.Timber

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class PrefsStorage(
    context: Context,
) {
    private val prefs = context.getSharedPreferences("PAPreferencesKey", Context.MODE_PRIVATE)
    internal var privacyStorageFilter: (PrivacyStorageFeature) -> Boolean = { _ -> true }
    private val privacyFilter: (String) -> Boolean = { key ->
        privacyStorageFeatureMap[key]?.let { privacyStorageFilter(it) } ?: false
    }

    internal fun clear() = prefs.edit().clear().apply()

    internal fun cleanStorageFeature(privacyStorageFeature: PrivacyStorageFeature) = prefs.edit().apply {
        if (privacyStorageFeature != PrivacyStorageFeature.ALL) {
            keysByPrivacyStorageFeature[privacyStorageFeature]?.forEach {
                remove(it)
            }
        } else {
            clear()
        }
    }.apply()

    init {
        if (REMOVED_KEYS.any { prefs.contains(it) }) {
            Timber.w("Updating from old version, breaking changes detected. Removing some stored data")
            prefs.edit().apply {
                (REMOVED_KEYS + CHANGED_KEYS).forEach { remove(it) }
            }.apply()
        }
    }

    // Lifecycle
    var versionCode: Long by prefs.delegates.long(key = VERSION_CODE, canBeSaved = privacyFilter)
    var firstSessionDate: Long by prefs.delegates.long(key = FIRST_SESSION_DATE, canBeSaved = privacyFilter)
    var firstSessionDateAfterUpdate: Long by prefs.delegates.long(
        key = FIRST_SESSION_DATE_AFTER_UPDATE,
        canBeSaved = privacyFilter
    )
    var lastSessionDate: Long by prefs.delegates.long(key = LAST_SESSION_DATE, canBeSaved = privacyFilter)
    var sessionCount: Int by prefs.delegates.int(key = SESSION_COUNT, canBeSaved = privacyFilter)
    var sessionCountAfterUpdate: Int by prefs.delegates.int(
        key = SESSION_COUNT_SINCE_UPDATE,
        canBeSaved = privacyFilter
    )

    // Visitor ID
    var visitorUuid: String? by prefs.delegates.nullableString(key = VISITOR_UUID, canBeSaved = privacyFilter)
    var visitorUuidGenerateTimestamp: Long by prefs.delegates.long(
        key = VISITOR_UUID_GENERATION_TIMESTAMP,
        canBeSaved = privacyFilter
    )

    // Privacy
    var privacyMode: String by prefs.delegates.string(key = PRIVACY_MODE, canBeSaved = privacyFilter)
    var privacyExpirationTimestamp: Long by prefs.delegates.long(
        key = PRIVACY_MODE_EXPIRATION_TIMESTAMP,
        canBeSaved = privacyFilter
    )
    var privacyVisitorConsent: Boolean by prefs.delegates.boolean(
        key = PRIVACY_VISITOR_CONSENT,
        canBeSaved = privacyFilter
    )

    // Crash
    var crashInfo: String? by prefs.delegates.nullableString(key = CRASHED, canBeSaved = privacyFilter)

    // Users
    var user: String? by prefs.delegates.nullableString(key = USER, canBeSaved = privacyFilter)
    var userGenerateTimestamp: Long by prefs.delegates.long(key = USER_GENERATION_TIMESTAMP, canBeSaved = privacyFilter)

    companion object {
        private const val VERSION_CODE = "PAVersionCode"
        private const val FIRST_SESSION_DATE = "PAFirstLaunchDate"
        private const val FIRST_SESSION_DATE_AFTER_UPDATE = "PAFirstLaunchDateAfterUpdate"
        private const val LAST_SESSION_DATE = "PALastLaunchDate"
        private const val SESSION_COUNT = "PALaunchCount"
        private const val SESSION_COUNT_SINCE_UPDATE = "PALaunchCountSinceUpdate"
        private const val VISITOR_UUID = "PAIdclientUUID"
        private const val VISITOR_UUID_GENERATION_TIMESTAMP = "PAIdclientUUIDGenerationTimestamp"
        private const val PRIVACY_MODE = "PAPrivacyMode"
        private const val PRIVACY_MODE_EXPIRATION_TIMESTAMP = "PAPrivacyModeExpirationTimestamp"
        private const val PRIVACY_VISITOR_CONSENT = "PAPrivacyVisitorConsent"
        private const val PRIVACY_VISITOR_ID = "PAPrivacyUserId"
        private const val CRASHED = "PACrashed"
        private const val USER = "PAUser"
        private const val USER_GENERATION_TIMESTAMP = "PAUserGenerationTimestamp"

        @JvmStatic
        private val privacyStorageFeatureMap = mapOf(
            VERSION_CODE to PrivacyStorageFeature.LIFECYCLE,
            FIRST_SESSION_DATE to PrivacyStorageFeature.LIFECYCLE,
            FIRST_SESSION_DATE_AFTER_UPDATE to PrivacyStorageFeature.LIFECYCLE,
            LAST_SESSION_DATE to PrivacyStorageFeature.LIFECYCLE,
            SESSION_COUNT to PrivacyStorageFeature.LIFECYCLE,
            SESSION_COUNT_SINCE_UPDATE to PrivacyStorageFeature.LIFECYCLE,
            VISITOR_UUID to PrivacyStorageFeature.VISITOR,
            VISITOR_UUID_GENERATION_TIMESTAMP to PrivacyStorageFeature.VISITOR,
            PRIVACY_MODE to PrivacyStorageFeature.PRIVACY,
            PRIVACY_MODE_EXPIRATION_TIMESTAMP to PrivacyStorageFeature.PRIVACY,
            PRIVACY_VISITOR_CONSENT to PrivacyStorageFeature.PRIVACY,
            PRIVACY_VISITOR_ID to PrivacyStorageFeature.PRIVACY,
            CRASHED to PrivacyStorageFeature.CRASH,
            USER to PrivacyStorageFeature.USER,
            USER_GENERATION_TIMESTAMP to PrivacyStorageFeature.USER
        )

        @JvmStatic
        private val keysByPrivacyStorageFeature = privacyStorageFeatureMap.entries.groupBy({ it.value }, { it.key })

        val REMOVED_KEYS = arrayOf(
            "PAFirstInitLifecycleDone",
            "PAInitLifecycleDone",
            "PAFirstLaunch",
            "PAFirstLaunchAfterUpdate",
            "PADaysSinceFirstLaunch",
            "PADaysSinceFirstLaunchAfterUpdate",
            "PADaysSinceLastUse",
            "ATIdclientUUID",
            "PACrashed"
        )
        val CHANGED_KEYS = arrayOf(
            VERSION_CODE,
            FIRST_SESSION_DATE,
            FIRST_SESSION_DATE_AFTER_UPDATE,
            LAST_SESSION_DATE
        )
    }
}
