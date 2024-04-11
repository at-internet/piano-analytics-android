package io.piano.android.analytics

import io.piano.android.analytics.model.OfflineStorageMode
import io.piano.android.analytics.model.PrivacyMode
import io.piano.android.analytics.model.VisitorIDType
import io.piano.android.analytics.model.VisitorStorageMode

/**
 * Class for storing all configuration
 */
public class Configuration private constructor(
    public val reportUrlProvider: ReportUrlProvider,
    public val defaultPrivacyMode: PrivacyMode,
    public val visitorIDType: VisitorIDType,
    public val offlineStorageMode: OfflineStorageMode,
    public val visitorStorageMode: VisitorStorageMode,
    public val eventsOfflineStorageLifetime: Int,
    public val privacyStorageLifetime: Int,
    public val visitorStorageLifetime: Int,
    public val userStorageLifetime: Int,
    public val sessionBackgroundDuration: Int,
    public val detectCrashes: Boolean,
    public val ignoreLimitedAdTracking: Boolean,
    public val sendEventWhenOptOut: Boolean,
) : ReportUrlProvider by reportUrlProvider {
    public class Builder @JvmOverloads constructor(
        public var collectDomain: String = "",
        public var site: Int = 0,
        public var path: String = DEFAULT_PATH,
        public var defaultPrivacyMode: PrivacyMode = PrivacyMode.OPTIN,
        public var visitorIDType: VisitorIDType = VisitorIDType.UUID,
        public var offlineStorageMode: OfflineStorageMode = OfflineStorageMode.REQUIRED,
        public var visitorStorageMode: VisitorStorageMode = VisitorStorageMode.FIXED,
        public var eventsOfflineStorageLifetime: Int = DEFAULT_EVENTS_OFFLINE_STORAGE_LIFETIME,
        public var privacyStorageLifetime: Int = DEFAULT_PRIVACY_STORAGE_LIFETIME,
        public var visitorStorageLifetime: Int = DEFAULT_VISITOR_STORAGE_LIFETIME,
        public var userStorageLifetime: Int = DEFAULT_USER_STORAGE_LIFETIME,
        public var sessionBackgroundDuration: Int = DEFAULT_SESSION_BACKGROUND_DURATION,
        public var detectCrashes: Boolean = true,
        public var ignoreLimitedAdTracking: Boolean = false,
        public var sendEventWhenOptOut: Boolean = true,
        public var reportUrlProvider: ReportUrlProvider? = null,
    ) {

        /**
         * Set a new collect endpoint to send your tagging data
         * @param collectDomain fully qualified domain name (FQDN) collect
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        public fun collectDomain(collectDomain: String): Builder = apply { this.collectDomain = collectDomain }

        /**
         * Set a new site ID
         * @param site site identifier
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        public fun site(site: Int): Builder = apply { this.site = site }

        /**
         * Set a new pixel path, to prevent potential tracking blockers by resource
         * @param path a resource name string prefixed by '/'
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        public fun path(path: String): Builder = apply { this.path = path }

        /**
         * Set a default privacy mode, that will be used on sent event(s) if privacy mode is empty
         * @param mode a privacy mode string
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        public fun defaultPrivacyMode(defaultPrivacyMode: PrivacyMode): Builder = apply {
            this.defaultPrivacyMode = defaultPrivacyMode
        }

        /**
         * Set a type of visitorID
         * @param visitorIDType a visitorID type
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        public fun visitorIDType(visitorIDType: VisitorIDType): Builder = apply { this.visitorIDType = visitorIDType }

        /**
         * Set an offline mode
         * @param offlineStorageMode an offline mode
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        public fun offlineStorageMode(offlineStorageMode: OfflineStorageMode): Builder = apply {
            this.offlineStorageMode = offlineStorageMode
        }

        /**
         * Set a new expiration mode (UUID visitor ID only)
         * @param visitorStorageMode a uuid expiration mode defined in enum
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        public fun visitorStorageMode(visitorStorageMode: VisitorStorageMode): Builder = apply {
            this.visitorStorageMode = visitorStorageMode
        }

        /**
         * Set a new duration before events will be removed from offline storage
         * @param eventsOfflineStorageLifetime an int in days
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        public fun eventsOfflineStorageLifetime(eventsOfflineStorageLifetime: Int): Builder = apply {
            this.eventsOfflineStorageLifetime = eventsOfflineStorageLifetime
        }

        /**
         * Set a new expiration privacy mode
         * @param privacyStorageLifetime an int in days
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        public fun privacyStorageLifetime(privacyStorageLifetime: Int): Builder = apply {
            this.privacyStorageLifetime = privacyStorageLifetime
        }

        /**
         * Set a new duration before visitor expiring (visitor ID only)
         * @param visitorStorageLifetime a visitor expiration duration (in days)
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        public fun visitorStorageLifetime(visitorStorageLifetime: Int): Builder = apply {
            this.visitorStorageLifetime = visitorStorageLifetime
        }

        /**
         * Set a new duration before user expiring (visitor ID only)
         * @param userStorageLifetime a user expiration duration (in days)
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        public fun userStorageLifetime(userStorageLifetime: Int): Builder = apply {
            this.userStorageLifetime = userStorageLifetime
        }

        /**
         * Set a new session background duration before a new session will be created
         * @param sessionBackgroundDuration a session background duration (in seconds)
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        public fun sessionBackgroundDuration(sessionBackgroundDuration: Int): Builder = apply {
            this.sessionBackgroundDuration = sessionBackgroundDuration
        }

        /**
         * Enable/disable crash detection
         * @param enabled enabling detection
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        public fun detectCrashes(detectCrashes: Boolean): Builder = apply { this.detectCrashes = detectCrashes }

        /**
         * Enable/disable ignorance advertising tracking limitation
         * @param enabled enabling ignorance
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        public fun ignoreLimitedAdTracking(ignoreLimitedAdTracking: Boolean): Builder = apply {
            this.ignoreLimitedAdTracking = ignoreLimitedAdTracking
        }

        /**
         * Enable/disable hit sending when user is opt-out
         * @param sendEventWhenOptOut allow hit sending
         * @return updated Builder instance
         */
        @Suppress("unused") // Public API.
        public fun sendEventWhenOptOut(sendEventWhenOptOut: Boolean): Builder = apply {
            this.sendEventWhenOptOut = sendEventWhenOptOut
        }

        /**
         * Sets a custom [ReportUrlProvider], which overrides [collectDomain], [site] and [path] for [Configuration]
         * @param reportUrlProvider [ReportUrlProvider] instance
         * @return updated Builder instance
         */
        public fun reportUrlProvider(reportUrlProvider: ReportUrlProvider?): Builder = apply {
            this.reportUrlProvider = reportUrlProvider
        }

        /**
         * Get a new Configuration instance from Builder data set
         * @return an Configuration instance
         */
        @Suppress("unused") // Public API.
        public fun build(): Configuration {
            check(reportUrlProvider != null || (collectDomain.isNotEmpty() && site > 0)) {
                "You have to provide collectDomain and site or reportUrlProvider"
            }
            return Configuration(
                reportUrlProvider ?: StaticReportUrlProvider(collectDomain, site, path),
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
    }

    public companion object {
        public const val DEFAULT_PATH: String = "event"
        public const val MIN_SESSION_BACKGROUND_DURATION: Int = 2
        public const val DEFAULT_SESSION_BACKGROUND_DURATION: Int = 30
        public const val DEFAULT_EVENTS_OFFLINE_STORAGE_LIFETIME: Int = 7
        public const val DEFAULT_PRIVACY_STORAGE_LIFETIME: Int = 395
        public const val DEFAULT_VISITOR_STORAGE_LIFETIME: Int = 395
        public const val DEFAULT_USER_STORAGE_LIFETIME: Int = 395
    }
}
