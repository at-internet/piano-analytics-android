package io.piano.android.analytics.model

/**
 * A privacy visitor mode
 *
 * @param visitorMode mode name
 * @param visitorConsent if user consented
 * @param allowedEventNames set of allowed event names for mode
 * @param forbiddenEventNames set of forbidden event names for mode
 * @param allowedStorageFeatures set of allowed storage keys for mode
 * @param forbiddenStorageFeatures set of forbidden storage keys for mode
 * @param allowedPropertyKeys set of allowed property names for mode
 * @param forbiddenPropertyKeys set of forbidden property names for mode
 * @constructor Creates a new privacy mode
 */
public class PrivacyMode(
    public val visitorMode: String,
    public val visitorConsent: Boolean = false,
    public val allowedEventNames: MutableSet<String> = DEFAULT_EVENT_NAMES.toMutableSet(),
    public val forbiddenEventNames: MutableSet<String> = mutableSetOf(),
    public val allowedStorageFeatures: MutableSet<PrivacyStorageFeature> = DEFAULT_STORAGE_FEATURES.toMutableSet(),
    public val forbiddenStorageFeatures: MutableSet<PrivacyStorageFeature> = mutableSetOf(),
    public val allowedPropertyKeys: MutableMap<String, MutableSet<PropertyName>> = mutableMapOf(
        Event.ANY to EXEMPT_DEFAULT_PROPERTY_KEYS.toMutableSet()
    ),
    public val forbiddenPropertyKeys: MutableMap<String, MutableSet<PropertyName>> = mutableMapOf(),
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        return other is PrivacyMode && visitorMode == other.visitorMode
    }

    override fun hashCode(): Int = visitorMode.hashCode()

    override fun toString(): String = visitorMode

    public companion object {
        @JvmStatic
        internal val DEFAULT_EVENT_NAMES = setOf(
            Event.CLICK_ACTION,
            Event.CLICK_DOWNLOAD,
            Event.CLICK_EXIT,
            Event.CLICK_NAVIGATION,
            Event.PAGE_DISPLAY
        )

        @JvmStatic
        internal val DEFAULT_STORAGE_FEATURES = setOf(
            PrivacyStorageFeature.VISITOR,
            PrivacyStorageFeature.PRIVACY
        )

        @JvmStatic
        internal val MINIMUM_DEFAULT_PROPERTY_KEYS = setOf(
            PropertyName.CONNECTION_TYPE,
            PropertyName.DEVICE_TIMESTAMP_UTC,
            PropertyName.VISITOR_PRIVACY_CONSENT,
            PropertyName.VISITOR_PRIVACY_MODE
        )

        @JvmStatic
        internal val EXEMPT_DEFAULT_PROPERTY_KEYS = MINIMUM_DEFAULT_PROPERTY_KEYS + setOf(
            PropertyName.APP_CRASH,
            PropertyName.APP_CRASH_CLASS,
            PropertyName.APP_CRASH_SCREEN,
            PropertyName.APP_VERSION,
            PropertyName.BROWSER,
            PropertyName.BROWSER_COOKIE_ACCEPTANCE,
            PropertyName.BROWSER_GROUP,
            PropertyName.BROWSER_VERSION,
            PropertyName.CLICK,
            PropertyName.CLICK_CHAPTER1,
            PropertyName.CLICK_CHAPTER2,
            PropertyName.CLICK_CHAPTER3,
            PropertyName.CLICK_FULL_NAME,
            PropertyName.CONNECTION_MONITOR,
            PropertyName.CONNECTION_ORGANISATION,
            PropertyName.DATE,
            PropertyName.DATE_DAY,
            PropertyName.DATE_DAYNUMBER,
            PropertyName.DATE_MONTH,
            PropertyName.DATE_MONTHNUMBER,
            PropertyName.DATE_WEEK,
            PropertyName.DATE_YEAR,
            PropertyName.DATE_YEAROFWEEK,
            PropertyName.DEVICE_BRAND,
            PropertyName.DEVICE_DISPLAY_HEIGHT,
            PropertyName.DEVICE_DISPLAY_WIDTH,
            PropertyName.DEVICE_MANUFACTURER,
            PropertyName.DEVICE_MODEL,
            PropertyName.DEVICE_NAME,
            PropertyName.DEVICE_NAME_TECH,
            PropertyName.DEVICE_SCREEN_DIAGONAL,
            PropertyName.DEVICE_SCREEN_HEIGHT,
            PropertyName.DEVICE_SCREEN_WIDTH,
            PropertyName.DEVICE_TYPE,
            PropertyName.EVENT_COLLECTION_PLATFORM,
            PropertyName.EVENT_COLLECTION_VERSION,
            PropertyName.EVENT_HOUR,
            PropertyName.EVENT_ID,
            PropertyName.EVENT_MINUTE,
            PropertyName.EVENT_NAME,
            PropertyName.EVENT_POSITION,
            PropertyName.EVENT_SECOND,
            PropertyName.EVENT_TIME,
            PropertyName.EVENT_TIME_UTC,
            PropertyName.EVENT_URL,
            PropertyName.EVENT_URL_DOMAIN,
            PropertyName.EVENT_URL_FULL,
            PropertyName.EXCLUSION_CAUSE,
            PropertyName.EXCLUSION_TYPE,
            PropertyName.GEO_CITY,
            PropertyName.GEO_CONTINENT,
            PropertyName.GEO_COUNTRY,
            PropertyName.GEO_METRO,
            PropertyName.GEO_REGION,
            PropertyName.HIT_TIME_UTC,
            PropertyName.OS,
            PropertyName.OS_GROUP,
            PropertyName.OS_VERSION,
            PropertyName.OS_VERSION_NAME,
            PropertyName.PAGE,
            PropertyName.PAGE_CHAPTER1,
            PropertyName.PAGE_CHAPTER2,
            PropertyName.PAGE_CHAPTER3,
            PropertyName.PAGE_DURATION,
            PropertyName.PAGE_FULL_NAME,
            PropertyName.PAGE_POSITION,
            PropertyName.PRIVACY_STATUS,
            PropertyName.SITE,
            PropertyName.SITE_ENV,
            PropertyName.SITE_ID,
            PropertyName.SITE_PLATFORM,
            PropertyName.SRC,
            PropertyName.SRC_DETAIL,
            PropertyName.SRC_DIRECT_ACCESS,
            PropertyName.SRC_ORGANIC,
            PropertyName.SRC_ORGANIC_DETAIL,
            PropertyName.SRC_PORTAL_DOMAIN,
            PropertyName.SRC_PORTAL_SITE,
            PropertyName.SRC_PORTAL_SITE_ID,
            PropertyName.SRC_PORTAL_URL,
            PropertyName.SRC_REFERRER_SITE_DOMAIN,
            PropertyName.SRC_REFERRER_SITE_URL,
            PropertyName.SRC_REFERRER_URL,
            PropertyName.SRC_SE,
            PropertyName.SRC_SE_CATEGORY,
            PropertyName.SRC_SE_COUNTRY,
            PropertyName.SRC_TYPE,
            PropertyName.SRC_URL,
            PropertyName.SRC_URL_DOMAIN,
            PropertyName.SRC_WEBMAIL
        )

        /**
         * Opt-in privacy mode
         */
        @JvmStatic
        public val OPTIN: PrivacyMode = PrivacyMode(
            visitorMode = "optin",
            visitorConsent = true,
            allowedEventNames = mutableSetOf(Event.ANY),
            allowedStorageFeatures = mutableSetOf(PrivacyStorageFeature.ALL),
            allowedPropertyKeys = mutableMapOf(Event.ANY to mutableSetOf(PropertyName.ANY_PROPERTY))
        )

        /**
         * Opt-out privacy mode
         */
        @JvmStatic
        public val OPTOUT: PrivacyMode = PrivacyMode(
            visitorMode = "optout",
            allowedEventNames = mutableSetOf(Event.ANY),
            allowedPropertyKeys = mutableMapOf(Event.ANY to MINIMUM_DEFAULT_PROPERTY_KEYS.toMutableSet())
        )

        /**
         * Exempt privacy mode
         */
        @JvmStatic
        public val EXEMPT: PrivacyMode = PrivacyMode(visitorMode = "exempt")

        /**
         * No consent privacy mode
         */
        @JvmStatic
        public val NO_CONSENT: PrivacyMode = PrivacyMode(
            visitorMode = "no-consent",
            allowedEventNames = mutableSetOf(Event.ANY),
            allowedStorageFeatures = mutableSetOf(),
            forbiddenStorageFeatures = mutableSetOf(PrivacyStorageFeature.ALL),
            allowedPropertyKeys = mutableMapOf(Event.ANY to MINIMUM_DEFAULT_PROPERTY_KEYS.toMutableSet())
        )

        /**
         * No storage privacy mode
         */
        @JvmStatic
        public val NO_STORAGE: PrivacyMode = PrivacyMode(
            visitorMode = "no-storage",
            allowedEventNames = mutableSetOf(Event.ANY),
            allowedStorageFeatures = mutableSetOf(),
            forbiddenStorageFeatures = mutableSetOf(PrivacyStorageFeature.ALL),
            allowedPropertyKeys = mutableMapOf(Event.ANY to mutableSetOf(PropertyName.ANY_PROPERTY))
        )
    }
}
