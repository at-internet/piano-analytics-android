package io.piano.android.analytics.model

internal enum class ConnectionType(
    val key: String,
) {
    MOBILE("MOBILE"),
    WIFI("WIFI"),
    OFFLINE("OFFLINE"),
    UNKNOWN("UNKNOWN"),
}

public enum class VisitorIDType {
    ADVERTISING_ID,
    GOOGLE_ADVERTISING_ID,
    HUAWEI_OPEN_ADVERTISING_ID,
    UUID,
    CUSTOM,
}

public enum class PrivacyStorageFeature {
    VISITOR,
    CRASH,
    LIFECYCLE,
    PRIVACY,
    USER,
    ALL,
}

public enum class OfflineStorageMode {
    /**
     * Hits are stored in all cases and requires calling method to send
     */
    ALWAYS,

    /**
     * Hits are sent if network is available, stored otherwise
     */
    REQUIRED,
}

public enum class VisitorStorageMode {
    /**
     * FIXED : UUID will expires in all cases
     */
    FIXED,

    /**
     * RELATIVE : UUID will expires in rare cases
     */
    RELATIVE,
}
