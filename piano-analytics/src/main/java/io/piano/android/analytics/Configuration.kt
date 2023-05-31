package io.piano.android.analytics

import io.piano.android.analytics.model.OfflineStorageMode
import io.piano.android.analytics.model.PrivacyMode
import io.piano.android.analytics.model.VisitorIDType
import io.piano.android.analytics.model.VisitorStorageMode

/**
 * Class for storing all configuration
 */
class Configuration private constructor(
    val collectDomain: String,
    val site: Int,
    val path: String,
    val defaultPrivacyMode: PrivacyMode,
    val visitorIDType: VisitorIDType,
    val offlineStorageMode: OfflineStorageMode,
    val visitorStorageMode: VisitorStorageMode,
    val eventsOfflineStorageLifetime: Int,
    val privacyStorageLifetime: Int,
    val visitorStorageLifetime: Int,
    val userStorageLifetime: Int,
    val sessionBackgroundDuration: Int,
    val detectCrashes: Boolean,
    val ignoreLimitedAdTracking: Boolean,
    val sendEventWhenOptOut: Boolean
) {
    class Builder @JvmOverloads constructor(
        var collectDomain: String,
        var site: Int,
        var path: String = DEFAULT_PATH,
        var defaultPrivacyMode: PrivacyMode = PrivacyMode.OPTIN,
        var visitorIDType: VisitorIDType = VisitorIDType.UUID,
        var offlineStorageMode: OfflineStorageMode = OfflineStorageMode.REQUIRED,
        var visitorStorageMode: VisitorStorageMode = VisitorStorageMode.FIXED,
        var eventsOfflineStorageLifetime: Int = DEFAULT_EVENTS_OFFLINE_STORAGE_LIFETIME,
        var privacyStorageLifetime: Int = DEFAULT_PRIVACY_STORAGE_LIFETIME,
        var visitorStorageLifetime: Int = DEFAULT_VISITOR_STORAGE_LIFETIME,
        var userStorageLifetime: Int = DEFAULT_USER_STORAGE_LIFETIME,
        var sessionBackgroundDuration: Int = DEFAULT_SESSION_BACKGROUND_DURATION,
        var detectCrashes: Boolean = true,
        var ignoreLimitedAdTracking: Boolean = false,
        var sendEventWhenOptOut: Boolean = true
    ) {

        /**
         * Set a new collect endpoint to send your tagging data
         * @param collectDomain fully qualified domain name (FQDN) collect
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        fun collectDomain(collectDomain: String) = apply { this.collectDomain = collectDomain }

        /**
         * Set a new site ID
         * @param site site identifier
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        fun site(site: Int) = apply { this.site = site }

        /**
         * Set a new pixel path, to prevent potential tracking blockers by resource
         * @param path a resource name string prefixed by '/'
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        fun path(path: String) = apply { this.path = path }

        /**
         * Set a default privacy mode, that will be used on sent event(s) if privacy mode is empty
         * @param mode a privacy mode string
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        fun defaultPrivacyMode(defaultPrivacyMode: PrivacyMode) = apply {
            this.defaultPrivacyMode = defaultPrivacyMode
        }

        /**
         * Set a type of visitorID
         * @param visitorIDType a visitorID type
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        fun visitorIDType(visitorIDType: VisitorIDType) = apply { this.visitorIDType = visitorIDType }

        /**
         * Set an offline mode
         * @param offlineStorageMode an offline mode
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        fun offlineStorageMode(offlineStorageMode: OfflineStorageMode) = apply {
            this.offlineStorageMode = offlineStorageMode
        }

        /**
         * Set a new expiration mode (UUID visitor ID only)
         * @param visitorStorageMode a uuid expiration mode defined in enum
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        fun visitorStorageMode(visitorStorageMode: VisitorStorageMode) = apply {
            this.visitorStorageMode = visitorStorageMode
        }

        /**
         * Set a new duration before events will be removed from offline storage
         * @param eventsOfflineStorageLifetime an int in days
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        fun eventsOfflineStorageLifetime(eventsOfflineStorageLifetime: Int) = apply {
            this.eventsOfflineStorageLifetime = eventsOfflineStorageLifetime
        }

        /**
         * Set a new expiration privacy mode
         * @param privacyStorageLifetime an int in days
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        fun privacyStorageLifetime(privacyStorageLifetime: Int) = apply {
            this.privacyStorageLifetime = privacyStorageLifetime
        }

        /**
         * Set a new duration before visitor expiring (visitor ID only)
         * @param visitorStorageLifetime a visitor expiration duration (in days)
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        fun visitorStorageLifetime(visitorStorageLifetime: Int) = apply {
            this.visitorStorageLifetime = visitorStorageLifetime
        }

        /**
         * Set a new duration before user expiring (visitor ID only)
         * @param userStorageLifetime a user expiration duration (in days)
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        fun userStorageLifetime(userStorageLifetime: Int) = apply { this.userStorageLifetime = userStorageLifetime }

        /**
         * Set a new session background duration before a new session will be created
         * @param sessionBackgroundDuration a session background duration (in seconds)
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        fun sessionBackgroundDuration(sessionBackgroundDuration: Int) = apply {
            this.sessionBackgroundDuration = sessionBackgroundDuration
        }

        /**
         * Enable/disable crash detection
         * @param enabled enabling detection
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        fun detectCrashes(detectCrashes: Boolean) = apply { this.detectCrashes = detectCrashes }

        /**
         * Enable/disable ignorance advertising tracking limitation
         * @param enabled enabling ignorance
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        fun ignoreLimitedAdTracking(ignoreLimitedAdTracking: Boolean) = apply {
            this.ignoreLimitedAdTracking = ignoreLimitedAdTracking
        }

        /**
         * Enable/disable hit sending when user is opt-out
         * @param sendEventWhenOptOut allow hit sending
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        fun sendEventWhenOptOut(sendEventWhenOptOut: Boolean) = apply { this.sendEventWhenOptOut = sendEventWhenOptOut }

        /**
         * Get a new Configuration instance from Builder data set
         * @return an Configuration instance
         */
        @Suppress("unused") // Public API.
        fun build() = Configuration(
            collectDomain,
            site,
            path,
            defaultPrivacyMode,
            visitorIDType,
            offlineStorageMode,
            visitorStorageMode,
            eventsOfflineStorageLifetime,
            privacyStorageLifetime,
            visitorStorageLifetime,
            userStorageLifetime,
            sessionBackgroundDuration.coerceAtLeast(MIN_SESSION_BACKGROUND_DURATION),
            detectCrashes,
            ignoreLimitedAdTracking,
            sendEventWhenOptOut
        )
    }

    companion object {
        const val DEFAULT_PATH = "event"
        const val MIN_SESSION_BACKGROUND_DURATION = 2
        const val DEFAULT_SESSION_BACKGROUND_DURATION = 30
        const val DEFAULT_EVENTS_OFFLINE_STORAGE_LIFETIME = 30
        const val DEFAULT_PRIVACY_STORAGE_LIFETIME = 395
        const val DEFAULT_VISITOR_STORAGE_LIFETIME = 395
        const val DEFAULT_USER_STORAGE_LIFETIME = 395
    }
}
