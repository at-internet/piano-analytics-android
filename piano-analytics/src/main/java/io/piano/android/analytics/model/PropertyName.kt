package io.piano.android.analytics.model

@JvmInline
value class PropertyName(
    val key: String
) {
    init {
        require(
            key.length <= MAX_LENGTH &&
                !key.startsWith(PREFIX_M, ignoreCase = true) &&
                !key.startsWith(PREFIX_VISIT, ignoreCase = true) &&
                // * is allowed for privacy filters
                (key == "*" || PROPERTY_REGEX.matches(key))
        ) {
            PROPERTY_NAME_NOT_VALID
        }
    }

    companion object {
        internal const val PREFIX_M = "m_"
        internal const val PREFIX_VISIT = "visit_"
        internal const val MAX_LENGTH = 40
        internal const val PROPERTY_NAME_NOT_VALID =
            "Property name can contain only `a-z`, `0-9`, `_`, " +
                "must begin with `a-z`, " +
                "must not begin with $PREFIX_M or $PREFIX_VISIT. " +
                "Max allowed length: $MAX_LENGTH"
        internal val PROPERTY_REGEX by lazy(LazyThreadSafetyMode.NONE) {
            "^[a-z]\\w*\$".toRegex(RegexOption.IGNORE_CASE)
        }

        @JvmStatic
        val ANY_PROPERTY = PropertyName("*")

        @JvmStatic
        val APP_CRASH = PropertyName("app_crash")

        @JvmStatic
        val APP_CRASH_CLASS = PropertyName("app_crash_class")

        @JvmStatic
        val APP_CRASH_SCREEN = PropertyName("app_crash_screen")

        @JvmStatic
        val APP_DAYS_SINCE_FIRST_SESSION = PropertyName("app_dsfs")

        @JvmStatic
        val APP_DAYS_SINCE_LAST_SESSION = PropertyName("app_dsls")

        @JvmStatic
        val APP_DAYS_SINCE_UPDATE = PropertyName("app_dsu")

        @JvmStatic
        val APP_ID = PropertyName("app_id")

        @JvmStatic
        val APP_FIRST_SESSION = PropertyName("app_fs")

        @JvmStatic
        val APP_FIRST_SESSION_AFTER_UPDATE = PropertyName("app_fsau")

        @JvmStatic
        val APP_FIRST_SESSION_DATE = PropertyName("app_fsd")

        @JvmStatic
        val APP_FIRST_SESSION_DATE_AFTER_UPDATE = PropertyName("app_fsdau")

        @JvmStatic
        val APP_SESSION_COUNT = PropertyName("app_sc")

        @JvmStatic
        val APP_SESSION_COUNT_SINCE_UPDATE = PropertyName("app_scsu")

        @JvmStatic
        val APP_SESSION_ID = PropertyName("app_sessionid")

        @JvmStatic
        val APP_VERSION = PropertyName("app_version")

        @JvmStatic
        val BROWSER = PropertyName("browser")

        @JvmStatic
        val BROWSER_LANGUAGE = PropertyName("browser_language")

        @JvmStatic
        val BROWSER_LANGUAGE_LOCAL = PropertyName("browser_language_local")

        @JvmStatic
        val BROWSER_COOKIE_ACCEPTANCE = PropertyName("browser_cookie_acceptance")

        @JvmStatic
        val BROWSER_GROUP = PropertyName("browser_group")

        @JvmStatic
        val BROWSER_VERSION = PropertyName("browser_version")

        @JvmStatic
        val CLICK = PropertyName("click")

        @JvmStatic
        val CLICK_CHAPTER1 = PropertyName("click_chapter1")

        @JvmStatic
        val CLICK_CHAPTER2 = PropertyName("click_chapter2")

        @JvmStatic
        val CLICK_CHAPTER3 = PropertyName("click_chapter3")

        @JvmStatic
        val CLICK_FULL_NAME = PropertyName("click_full_name")

        @JvmStatic
        val CONNECTION_MONITOR = PropertyName("connection_monitor")

        @JvmStatic
        val CONNECTION_ORGANISATION = PropertyName("connection_organisation")

        @JvmStatic
        val CONNECTION_TYPE = PropertyName("connection_type")

        @JvmStatic
        val DATE = PropertyName("date")

        @JvmStatic
        val DATE_DAY = PropertyName("date_day")

        @JvmStatic
        val DATE_DAYNUMBER = PropertyName("date_daynumber")

        @JvmStatic
        val DATE_MONTH = PropertyName("date_month")

        @JvmStatic
        val DATE_MONTHNUMBER = PropertyName("date_monthnumber")

        @JvmStatic
        val DATE_WEEK = PropertyName("date_week")

        @JvmStatic
        val DATE_YEAR = PropertyName("date_year")

        @JvmStatic
        val DATE_YEAROFWEEK = PropertyName("date_yearofweek")

        @JvmStatic
        val DEVICE_BRAND = PropertyName("device_brand")

        @JvmStatic
        val DEVICE_DISPLAY_HEIGHT = PropertyName("device_display_height")

        @JvmStatic
        val DEVICE_DISPLAY_WIDTH = PropertyName("device_display_width")

        @JvmStatic
        val DEVICE_NAME = PropertyName("device_name")

        @JvmStatic
        val DEVICE_NAME_TECH = PropertyName("device_name_tech")

        @JvmStatic
        val DEVICE_SCREEN_DIAGONAL = PropertyName("device_screen_diagonal")

        @JvmStatic
        val DEVICE_SCREEN_HEIGHT = PropertyName("device_screen_height")

        @JvmStatic
        val DEVICE_SCREEN_WIDTH = PropertyName("device_screen_width")

        @JvmStatic
        val DEVICE_TIMESTAMP_UTC = PropertyName("device_timestamp_utc")

        @JvmStatic
        val DEVICE_TYPE = PropertyName("device_type")

        @JvmStatic
        val EVENT_COLLECTION_PLATFORM = PropertyName("event_collection_platform")

        @JvmStatic
        val EVENT_COLLECTION_VERSION = PropertyName("event_collection_version")

        @JvmStatic
        val EVENT_HOUR = PropertyName("event_hour")

        @JvmStatic
        val EVENT_ID = PropertyName("event_id")

        @JvmStatic
        val EVENT_MINUTE = PropertyName("event_minute")

        @JvmStatic
        val EVENT_NAME = PropertyName("event_name")

        @JvmStatic
        val EVENT_POSITION = PropertyName("event_position")

        @JvmStatic
        val EVENT_SECOND = PropertyName("event_second")

        @JvmStatic
        val EVENT_TIME = PropertyName("event_time")

        @JvmStatic
        val EVENT_TIME_UTC = PropertyName("event_time_utc")

        @JvmStatic
        val EVENT_URL = PropertyName("event_url")

        @JvmStatic
        val EVENT_URL_DOMAIN = PropertyName("event_url_domain")

        @JvmStatic
        val EVENT_URL_FULL = PropertyName("event_url_full")

        @JvmStatic
        val EXCLUSION_CAUSE = PropertyName("exclusion_cause")

        @JvmStatic
        val EXCLUSION_TYPE = PropertyName("exclusion_type")

        @JvmStatic
        val GEO_CITY = PropertyName("geo_city")

        @JvmStatic
        val GEO_CONTINENT = PropertyName("geo_continent")

        @JvmStatic
        val GEO_COUNTRY = PropertyName("geo_country")

        @JvmStatic
        val GEO_METRO = PropertyName("geo_metro")

        @JvmStatic
        val GEO_REGION = PropertyName("geo_region")

        @JvmStatic
        val HIT_TIME_UTC = PropertyName("hit_time_utc")

        @JvmStatic
        val MANUFACTURER = PropertyName("manufacturer")

        @JvmStatic
        val MODEL = PropertyName("model")

        @JvmStatic
        val OS = PropertyName("os")

        @JvmStatic
        val OS_GROUP = PropertyName("os_group")

        @JvmStatic
        val OS_VERSION = PropertyName("os_version")

        @JvmStatic
        val OS_VERSION_NAME = PropertyName("os_version_name")

        @JvmStatic
        val PAGE = PropertyName("page")

        @JvmStatic
        val PAGE_CHAPTER1 = PropertyName("page_chapter1")

        @JvmStatic
        val PAGE_CHAPTER2 = PropertyName("page_chapter2")

        @JvmStatic
        val PAGE_CHAPTER3 = PropertyName("page_chapter3")

        @JvmStatic
        val PAGE_DURATION = PropertyName("page_duration")

        @JvmStatic
        val PAGE_FULL_NAME = PropertyName("page_full_name")

        @JvmStatic
        val PAGE_POSITION = PropertyName("page_position")

        @JvmStatic
        val PRIVACY_STATUS = PropertyName("privacy_status")

        @JvmStatic
        val SITE = PropertyName("site")

        @JvmStatic
        val SITE_ENV = PropertyName("site_env")

        @JvmStatic
        val SITE_ID = PropertyName("site_id")

        @JvmStatic
        val SITE_PLATFORM = PropertyName("site_platform")

        @JvmStatic
        val SRC = PropertyName("src")

        @JvmStatic
        val SRC_DETAIL = PropertyName("src_detail")

        @JvmStatic
        val SRC_DIRECT_ACCESS = PropertyName("src_direct_access")

        @JvmStatic
        val SRC_ORGANIC = PropertyName("src_organic")

        @JvmStatic
        val SRC_ORGANIC_DETAIL = PropertyName("src_organic_detail")

        @JvmStatic
        val SRC_PORTAL_DOMAIN = PropertyName("src_portal_domain")

        @JvmStatic
        val SRC_PORTAL_SITE = PropertyName("src_portal_site")

        @JvmStatic
        val SRC_PORTAL_SITE_ID = PropertyName("src_portal_site_id")

        @JvmStatic
        val SRC_PORTAL_URL = PropertyName("src_portal_url")

        @JvmStatic
        val SRC_REFERRER_SITE_DOMAIN = PropertyName("src_referrer_site_domain")

        @JvmStatic
        val SRC_REFERRER_SITE_URL = PropertyName("src_referrer_site_url")

        @JvmStatic
        val SRC_REFERRER_URL = PropertyName("src_referrer_url")

        @JvmStatic
        val SRC_SE = PropertyName("src_se")

        @JvmStatic
        val SRC_SE_CATEGORY = PropertyName("src_se_category")

        @JvmStatic
        val SRC_SE_COUNTRY = PropertyName("src_se_country")

        @JvmStatic
        val SRC_TYPE = PropertyName("src_type")

        @JvmStatic
        val SRC_URL = PropertyName("src_url")

        @JvmStatic
        val SRC_URL_DOMAIN = PropertyName("src_url_domain")

        @JvmStatic
        val SRC_WEBMAIL = PropertyName("src_webmail")

        @JvmStatic
        val USER_ID = PropertyName("user_id")

        @JvmStatic
        val USER_RECOGNITION = PropertyName("user_recognition")

        @JvmStatic
        val USER_CATEGORY = PropertyName("user_category")

        @JvmStatic
        val VISITOR_PRIVACY_CONSENT = PropertyName("visitor_privacy_consent")

        @JvmStatic
        val VISITOR_PRIVACY_MODE = PropertyName("visitor_privacy_mode")
    }
}
