package io.piano.android.analytics.model

@JvmInline
public value class PropertyName(
    public val key: String,
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

    public companion object {
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
        public val ANY_PROPERTY: PropertyName = PropertyName("*")

        @JvmStatic
        public val APP_CRASH: PropertyName = PropertyName("app_crash")

        @JvmStatic
        public val APP_CRASH_CLASS: PropertyName = PropertyName("app_crash_class")

        @JvmStatic
        public val APP_CRASH_SCREEN: PropertyName = PropertyName("app_crash_screen")

        @JvmStatic
        public val APP_DAYS_SINCE_FIRST_SESSION: PropertyName = PropertyName("app_dsfs")

        @JvmStatic
        public val APP_DAYS_SINCE_LAST_SESSION: PropertyName = PropertyName("app_dsls")

        @JvmStatic
        public val APP_DAYS_SINCE_UPDATE: PropertyName = PropertyName("app_dsu")

        @JvmStatic
        public val APP_ID: PropertyName = PropertyName("app_id")

        @JvmStatic
        public val APP_FIRST_SESSION: PropertyName = PropertyName("app_fs")

        @JvmStatic
        public val APP_FIRST_SESSION_AFTER_UPDATE: PropertyName = PropertyName("app_fsau")

        @JvmStatic
        public val APP_FIRST_SESSION_DATE: PropertyName = PropertyName("app_fsd")

        @JvmStatic
        public val APP_FIRST_SESSION_DATE_AFTER_UPDATE: PropertyName = PropertyName("app_fsdau")

        @JvmStatic
        public val APP_SESSION_COUNT: PropertyName = PropertyName("app_sc")

        @JvmStatic
        public val APP_SESSION_COUNT_SINCE_UPDATE: PropertyName = PropertyName("app_scsu")

        @JvmStatic
        public val APP_SESSION_ID: PropertyName = PropertyName("app_sessionid")

        @JvmStatic
        public val APP_VERSION: PropertyName = PropertyName("app_version")

        @JvmStatic
        public val BROWSER: PropertyName = PropertyName("browser")

        @JvmStatic
        public val BROWSER_LANGUAGE: PropertyName = PropertyName("browser_language")

        @JvmStatic
        public val BROWSER_LANGUAGE_LOCAL: PropertyName = PropertyName("browser_language_local")

        @JvmStatic
        public val BROWSER_COOKIE_ACCEPTANCE: PropertyName = PropertyName("browser_cookie_acceptance")

        @JvmStatic
        public val BROWSER_GROUP: PropertyName = PropertyName("browser_group")

        @JvmStatic
        public val BROWSER_VERSION: PropertyName = PropertyName("browser_version")

        @JvmStatic
        public val CLICK: PropertyName = PropertyName("click")

        @JvmStatic
        public val CLICK_CHAPTER1: PropertyName = PropertyName("click_chapter1")

        @JvmStatic
        public val CLICK_CHAPTER2: PropertyName = PropertyName("click_chapter2")

        @JvmStatic
        public val CLICK_CHAPTER3: PropertyName = PropertyName("click_chapter3")

        @JvmStatic
        public val CLICK_FULL_NAME: PropertyName = PropertyName("click_full_name")

        @JvmStatic
        public val CONNECTION_MONITOR: PropertyName = PropertyName("connection_monitor")

        @JvmStatic
        public val CONNECTION_ORGANISATION: PropertyName = PropertyName("connection_organisation")

        @JvmStatic
        public val CONNECTION_TYPE: PropertyName = PropertyName("connection_type")

        @JvmStatic
        public val DATE: PropertyName = PropertyName("date")

        @JvmStatic
        public val DATE_DAY: PropertyName = PropertyName("date_day")

        @JvmStatic
        public val DATE_DAYNUMBER: PropertyName = PropertyName("date_daynumber")

        @JvmStatic
        public val DATE_MONTH: PropertyName = PropertyName("date_month")

        @JvmStatic
        public val DATE_MONTHNUMBER: PropertyName = PropertyName("date_monthnumber")

        @JvmStatic
        public val DATE_WEEK: PropertyName = PropertyName("date_week")

        @JvmStatic
        public val DATE_YEAR: PropertyName = PropertyName("date_year")

        @JvmStatic
        public val DATE_YEAROFWEEK: PropertyName = PropertyName("date_yearofweek")

        @JvmStatic
        public val DEVICE_BRAND: PropertyName = PropertyName("device_brand")

        @JvmStatic
        public val DEVICE_DISPLAY_HEIGHT: PropertyName = PropertyName("device_display_height")

        @JvmStatic
        public val DEVICE_DISPLAY_WIDTH: PropertyName = PropertyName("device_display_width")

        @JvmStatic
        public val DEVICE_NAME: PropertyName = PropertyName("device_name")

        @JvmStatic
        public val DEVICE_NAME_TECH: PropertyName = PropertyName("device_name_tech")

        @JvmStatic
        public val DEVICE_SCREEN_DIAGONAL: PropertyName = PropertyName("device_screen_diagonal")

        @JvmStatic
        public val DEVICE_SCREEN_HEIGHT: PropertyName = PropertyName("device_screen_height")

        @JvmStatic
        public val DEVICE_SCREEN_WIDTH: PropertyName = PropertyName("device_screen_width")

        @JvmStatic
        public val DEVICE_TIMESTAMP_UTC: PropertyName = PropertyName("device_timestamp_utc")

        @JvmStatic
        public val DEVICE_TYPE: PropertyName = PropertyName("device_type")

        @JvmStatic
        public val EVENT_COLLECTION_PLATFORM: PropertyName = PropertyName("event_collection_platform")

        @JvmStatic
        public val EVENT_COLLECTION_VERSION: PropertyName = PropertyName("event_collection_version")

        @JvmStatic
        public val EVENT_HOUR: PropertyName = PropertyName("event_hour")

        @JvmStatic
        public val EVENT_ID: PropertyName = PropertyName("event_id")

        @JvmStatic
        public val EVENT_MINUTE: PropertyName = PropertyName("event_minute")

        @JvmStatic
        public val EVENT_NAME: PropertyName = PropertyName("event_name")

        @JvmStatic
        public val EVENT_POSITION: PropertyName = PropertyName("event_position")

        @JvmStatic
        public val EVENT_SECOND: PropertyName = PropertyName("event_second")

        @JvmStatic
        public val EVENT_TIME: PropertyName = PropertyName("event_time")

        @JvmStatic
        public val EVENT_TIME_UTC: PropertyName = PropertyName("event_time_utc")

        @JvmStatic
        public val EVENT_URL: PropertyName = PropertyName("event_url")

        @JvmStatic
        public val EVENT_URL_DOMAIN: PropertyName = PropertyName("event_url_domain")

        @JvmStatic
        public val EVENT_URL_FULL: PropertyName = PropertyName("event_url_full")

        @JvmStatic
        public val EXCLUSION_CAUSE: PropertyName = PropertyName("exclusion_cause")

        @JvmStatic
        public val EXCLUSION_TYPE: PropertyName = PropertyName("exclusion_type")

        @JvmStatic
        public val GEO_CITY: PropertyName = PropertyName("geo_city")

        @JvmStatic
        public val GEO_CONTINENT: PropertyName = PropertyName("geo_continent")

        @JvmStatic
        public val GEO_COUNTRY: PropertyName = PropertyName("geo_country")

        @JvmStatic
        public val GEO_METRO: PropertyName = PropertyName("geo_metro")

        @JvmStatic
        public val GEO_REGION: PropertyName = PropertyName("geo_region")

        @JvmStatic
        public val HIT_TIME_UTC: PropertyName = PropertyName("hit_time_utc")

        @JvmStatic
        public val DEVICE_MANUFACTURER: PropertyName = PropertyName("device_manufacturer")

        @JvmStatic
        public val DEVICE_MODEL: PropertyName = PropertyName("device_model")

        @JvmStatic
        public val OS: PropertyName = PropertyName("os")

        @JvmStatic
        public val OS_GROUP: PropertyName = PropertyName("os_group")

        @JvmStatic
        public val OS_VERSION: PropertyName = PropertyName("os_version")

        @JvmStatic
        public val OS_VERSION_NAME: PropertyName = PropertyName("os_version_name")

        @JvmStatic
        public val PAGE: PropertyName = PropertyName("page")

        @JvmStatic
        public val PAGE_CHAPTER1: PropertyName = PropertyName("page_chapter1")

        @JvmStatic
        public val PAGE_CHAPTER2: PropertyName = PropertyName("page_chapter2")

        @JvmStatic
        public val PAGE_CHAPTER3: PropertyName = PropertyName("page_chapter3")

        @JvmStatic
        public val PAGE_DURATION: PropertyName = PropertyName("page_duration")

        @JvmStatic
        public val PAGE_FULL_NAME: PropertyName = PropertyName("page_full_name")

        @JvmStatic
        public val PAGE_POSITION: PropertyName = PropertyName("page_position")

        @JvmStatic
        public val PRIVACY_STATUS: PropertyName = PropertyName("privacy_status")

        @JvmStatic
        public val SITE: PropertyName = PropertyName("site")

        @JvmStatic
        public val SITE_ENV: PropertyName = PropertyName("site_env")

        @JvmStatic
        public val SITE_ID: PropertyName = PropertyName("site_id")

        @JvmStatic
        public val SITE_PLATFORM: PropertyName = PropertyName("site_platform")

        @JvmStatic
        public val SRC: PropertyName = PropertyName("src")

        @JvmStatic
        public val SRC_DETAIL: PropertyName = PropertyName("src_detail")

        @JvmStatic
        public val SRC_DIRECT_ACCESS: PropertyName = PropertyName("src_direct_access")

        @JvmStatic
        public val SRC_ORGANIC: PropertyName = PropertyName("src_organic")

        @JvmStatic
        public val SRC_ORGANIC_DETAIL: PropertyName = PropertyName("src_organic_detail")

        @JvmStatic
        public val SRC_PORTAL_DOMAIN: PropertyName = PropertyName("src_portal_domain")

        @JvmStatic
        public val SRC_PORTAL_SITE: PropertyName = PropertyName("src_portal_site")

        @JvmStatic
        public val SRC_PORTAL_SITE_ID: PropertyName = PropertyName("src_portal_site_id")

        @JvmStatic
        public val SRC_PORTAL_URL: PropertyName = PropertyName("src_portal_url")

        @JvmStatic
        public val SRC_REFERRER_SITE_DOMAIN: PropertyName = PropertyName("src_referrer_site_domain")

        @JvmStatic
        public val SRC_REFERRER_SITE_URL: PropertyName = PropertyName("src_referrer_site_url")

        @JvmStatic
        public val SRC_REFERRER_URL: PropertyName = PropertyName("src_referrer_url")

        @JvmStatic
        public val SRC_SE: PropertyName = PropertyName("src_se")

        @JvmStatic
        public val SRC_SE_CATEGORY: PropertyName = PropertyName("src_se_category")

        @JvmStatic
        public val SRC_SE_COUNTRY: PropertyName = PropertyName("src_se_country")

        @JvmStatic
        public val SRC_TYPE: PropertyName = PropertyName("src_type")

        @JvmStatic
        public val SRC_URL: PropertyName = PropertyName("src_url")

        @JvmStatic
        public val SRC_URL_DOMAIN: PropertyName = PropertyName("src_url_domain")

        @JvmStatic
        public val SRC_WEBMAIL: PropertyName = PropertyName("src_webmail")

        @JvmStatic
        public val USER_ID: PropertyName = PropertyName("user_id")

        @JvmStatic
        public val USER_RECOGNITION: PropertyName = PropertyName("user_recognition")

        @JvmStatic
        public val USER_CATEGORY: PropertyName = PropertyName("user_category")

        @JvmStatic
        public val VISITOR_PRIVACY_CONSENT: PropertyName = PropertyName("visitor_privacy_consent")

        @JvmStatic
        public val VISITOR_PRIVACY_MODE: PropertyName = PropertyName("visitor_privacy_mode")
    }
}
