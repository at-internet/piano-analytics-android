/*
 * This SDK is licensed under the MIT license (MIT)
 * Copyright (c) 2015- Applied Technologies Internet SAS (registration number B 403 261 258 - Trade and Companies Register of Bordeaux – France)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.piano.analytics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class Configuration extends HashMap<String, Object> {

    /// region PUBLIC SECTION

    public String get(ConfigurationKey key) {
        return CastUtils.toString(this.get(key.stringValue()));
    }

    /// region Enums

    public enum ConfigurationKey {
        COLLECT_DOMAIN("collectDomain"),
        SITE("site"),
        PATH("path"),
        PRIVACY_DEFAULT_MODE("privacyDefaultMode"),
        OFFLINE_STORAGE_MODE("offlineStorageMode"),
        IGNORE_LIMITED_AD_TRACKING("ignoreLimitedAdTracking"),
        CRASH_DETECTION("crashDetection"),
        VISITOR_STORAGE_MODE("visitorStorageMode"),
        STORAGE_LIFETIME_USER("storageLifetimeUser"),
        STORAGE_LIFETIME_PRIVACY("storageLifetimePrivacy"),
        STORAGE_LIFETIME_VISITOR("storageLifetimeVisitor"),
        SESSION_BACKGROUND_DURATION("sessionBackgroundDuration"),
        SEND_EVENT_WHEN_OPT_OUT("sendEventWhenOptOut"),
        ENCRYPTION_MODE("encryptionMode"),
        VISITOR_ID("visitorId"),
        VISITOR_ID_TYPE("visitorIdType"),
        CUSTOM_USER_AGENT("customUserAgent");

        private final String str;

        ConfigurationKey(String val) {
            str = val;
        }

        String stringValue() {
            return str;
        }

        public static ConfigurationKey fromString(String s) {
            for (ConfigurationKey v : values()) {
                if (v.stringValue().equalsIgnoreCase(s)) {
                    return v;
                }
            }
            PianoAnalytics.InternalLogger.severe("ConfigurationKeysEnum.fromString : requested value is unknown");
            return null;
        }
    }

    public enum VisitorIDType {
        /***
         * Careful when this type used : https://developer.android.com/training/articles/user-data-ids
         */
        ANDROID_ID("androidId"),
        ADVERTISING_ID("advertisingId"),
        GOOGLE_ADVERTISING_ID("googleAdvertisingId"),
        HUAWEI_OPEN_ADVERTISING_ID("huaweiOpenAdvertisingId"),
        UUID("uuid"),
        CUSTOM("custom");

        private final String str;

        VisitorIDType(String val) {
            str = val;
        }

        String stringValue() {
            return str;
        }

        public static VisitorIDType fromString(String s) {
            for (VisitorIDType v : values()) {
                if (v.stringValue().equalsIgnoreCase(s)) {
                    return v;
                }
            }
            PianoAnalytics.InternalLogger.severe("VisitorIDTypeEnum.fromString : requested value is unknown");
            return VisitorIDType.UUID;
        }
    }

    public enum OfflineStorageMode {
        /***
         * Hits are stored in all cases and requires calling method to send
         */
        ALWAYS("always"),
        /***
         * Hits are sent if network is available, stored otherwise
         */
        REQUIRED("required"),
        /***
         * Hits are sent if network is available, lost otherwise
         */
        NEVER("never");

        private final String str;

        OfflineStorageMode(String val) {
            str = val;
        }

        String stringValue() {
            return str;
        }

        public static OfflineStorageMode fromString(String s) {
            for (OfflineStorageMode v : values()) {
                if (v.stringValue().equalsIgnoreCase(s)) {
                    return v;
                }
            }
            PianoAnalytics.InternalLogger.severe("OfflineModeEnum.fromString : fallback on OfflineModeEnum.NEVER mode because requested value is unknown");
            return OfflineStorageMode.NEVER;
        }
    }

    public enum VisitorStorageMode {
        /***
         * FIXED : UUID will expires in all cases
         */
        FIXED("fixed"),
        /***
         * RELATIVE : UUID will expires in rare cases
         */
        RELATIVE("relative");

        private final String str;

        VisitorStorageMode(String val) {
            str = val;
        }

        String stringValue() {
            return str;
        }

        public static VisitorStorageMode fromString(String s) {
            for (VisitorStorageMode v : values()) {
                if (v.stringValue().equalsIgnoreCase(s)) {
                    return v;
                }
            }
            PianoAnalytics.InternalLogger.severe("VisitorStorageModeEnum.fromString : fallback on VisitorStorageModeEnum.FIXED mode because requested value is unknown");
            return VisitorStorageMode.FIXED;
        }
    }

    public enum EncryptionMode {
        /**
         * No encryption stored data
         */
        NONE("none"),
        /**
         * encryption stored data enabled if device is compatible
         */
        IF_COMPATIBLE("ifCompatible"),
        /**
         * /!\ encryption stored data enable AND if not data not stored
         */
        FORCE("force");

        private final String str;

        EncryptionMode(String val) {
            str = val;
        }

        String stringValue() {
            return str;
        }

        public static EncryptionMode fromString(String s) {
            for (EncryptionMode v : values()) {
                if (v.stringValue().equalsIgnoreCase(s)) {
                    return v;
                }
            }
            PianoAnalytics.InternalLogger.severe("EncryptionModeEnum.fromString : fallback on EncryptionModeEnum.NONE mode because requested value is unknown");
            return EncryptionMode.IF_COMPATIBLE;
        }
    }

    /// endregion

    /// region Configuration.Builder

    public static class Builder {

        /// region Constructors

        private final Map<String, Object> configuration = new HashMap<>();

        /// endregion

        /// region PUBLIC SECTION

        /***
         * Set a new collect endpoint to send your tagging data
         * @param collectDomain fully qualified domain name (FQDN) collect
         * @return updated Builder instance
         */
        public Builder withCollectDomain(String collectDomain) {
            return this.set(ConfigurationKey.COLLECT_DOMAIN, collectDomain);
        }

        /***
         * Set a new site ID
         * @param site site identifier
         * @return updated Builder instance
         */
        public Builder withSite(int site) {
            return this.set(ConfigurationKey.SITE, site);
        }

        /***
         * Set a new pixel path, to prevent potential tracking blockers by resource
         * @param path a resource name string prefixed by '/'
         * @return updated Builder instance
         */
        public Builder withPath(String path) {
            return this.set(ConfigurationKey.PATH, path);
        }

        /***
         * Set a default privacy mode, that will be used on sent event(s) if privacy mode is empty
         * @param mode a privacy mode string
         * @return updated Builder instance
         */
        public Builder withPrivacyDefaultMode(String mode) {
            return this.set(ConfigurationKey.PRIVACY_DEFAULT_MODE, mode);
        }

        /***
         * Set a type of visitorID
         * @param visitorIDType a visitorID type
         * @return updated Builder instance
         */
        public Builder withVisitorIDType(VisitorIDType visitorIDType) {
            return this.set(ConfigurationKey.VISITOR_ID_TYPE, visitorIDType.stringValue());
        }

        /***
         * Set an offline mode
         * @param offlineStorageMode an offline mode
         * @return updated Builder instance
         */
        public Builder withOfflineStorageMode(OfflineStorageMode offlineStorageMode) {
            return this.set(ConfigurationKey.OFFLINE_STORAGE_MODE, offlineStorageMode.stringValue());
        }

        /***
         * Enable/disable ignorance advertising tracking limitation
         * @param enabled enabling ignorance
         * @return updated Builder instance
         */
        public Builder enableIgnoreLimitedAdTracking(boolean enabled) {
            return this.set(ConfigurationKey.IGNORE_LIMITED_AD_TRACKING, enabled);
        }

        /***
         * Enable/disable crash detection
         * @param enabled enabling detection
         * @return updated Builder instance
         */
        public Builder enableCrashDetection(boolean enabled) {
            return this.set(ConfigurationKey.CRASH_DETECTION, enabled);
        }

        /***
         * Set a new expiration mode (UUID visitor ID only)
         * @param visitorStorageMode a uuid expiration mode defined in enum
         * @return updated Builder instance
         */
        public Builder withVisitorStorageMode(VisitorStorageMode visitorStorageMode) {
            return this.set(ConfigurationKey.VISITOR_STORAGE_MODE, visitorStorageMode.stringValue());
        }

        /***
         * Set a new expiration privacy mode
         * @param storageLifetimePrivacy an int in days
         * @return updated Builder instance
         */
        public Builder withStorageLifetimePrivacy(int storageLifetimePrivacy) {
            return this.set(ConfigurationKey.STORAGE_LIFETIME_PRIVACY, storageLifetimePrivacy);
        }

        /***
         * Set a new duration before visitor expiring (visitor ID only)
         * @param storageLifetimeVisitor a visitor expiration duration (in days)
         * @return updated Builder instance
         */
        public Builder withStorageLifetimeVisitor(int storageLifetimeVisitor) {
            return this.set(ConfigurationKey.STORAGE_LIFETIME_VISITOR, storageLifetimeVisitor);
        }

        /***
         * Set a new duration before user expiring (visitor ID only)
         * @param storageLifetimeUser a user expiration duration (in days)
         * @return updated Builder instance
         */
        public Builder withStorageLifetimeUser(int storageLifetimeUser) {
            return this.set(ConfigurationKey.STORAGE_LIFETIME_USER, storageLifetimeUser);
        }

        /***
         * Set a new encryption mode
         * @param encryptionMode an encryption mode for at-rest data
         * @return updated Builder instance
         */
        public Builder withEncryptionMode(EncryptionMode encryptionMode) {
            return this.set(ConfigurationKey.ENCRYPTION_MODE, encryptionMode.stringValue());
        }

        /***
         * Set a new session background duration before a new session will be created
         * @param sessionBackgroundDuration a session background duration (in seconds)
         * @return updated Builder instance
         */
        public Builder withSessionBackgroundDuration(int sessionBackgroundDuration) {
            return this.set(ConfigurationKey.SESSION_BACKGROUND_DURATION, sessionBackgroundDuration);
        }

        /***
         * Enable/disable hit sending when user is opt-out
         * @param sendEventWhenOptOut allow hit sending
         * @return updated Builder instance
         */
        public Builder enableSendEventWhenOptOut(boolean sendEventWhenOptOut) {
            return this.set(ConfigurationKey.SEND_EVENT_WHEN_OPT_OUT, sendEventWhenOptOut);
        }

        /***
         * Set a custom visitorID
         * @param visitorID custom visitor ID
         * @return updated Builder instance
         */
        public Builder withVisitorID(String visitorID) {
            return this.set(ConfigurationKey.VISITOR_ID, visitorID)
                    .withVisitorIDType(VisitorIDType.CUSTOM);
        }

        /***
         * Set a custom user agent
         * @param customUserAgent custom user agent
         * @return updated Builder instance
         */
        public Builder withCustomUserAgent(String customUserAgent) {
            return this.set(ConfigurationKey.CUSTOM_USER_AGENT, customUserAgent);
        }

        /***
         * Get a new Configuration instance from Builder data set
         * @return an Configuration instance
         */
        public Configuration build() {
            return new Configuration(this.configuration);
        }

        /// endregion

        /// region Private methods

        private Builder set(ConfigurationKey key, Object value) {
            this.configuration.put(key.stringValue(), CastUtils.toString(value));

            return this;
        }

        /// endregion
    }

    /// endregion

    /// endregion

    /// region Constants

    static final String DEFAULT_COLLECT_DOMAIN = "";
    static final String DEFAULT_PATH = "/event";
    static final String PRIVACY_DEFAULT_MODE = PianoAnalytics.PrivacyVisitorMode.OPTIN.stringValue();

    static final int DEFAULT_SITE = 0;
    static final int DEFAULT_SESSION_BACKGROUND_DURATION = 30;
    static final int DEFAULT_STORAGE_LIFETIME_PRIVACY = PianoAnalytics.DEFAULT_STORAGE_LIFETIME_PRIVACY;
    static final int DEFAULT_STORAGE_LIFETIME_VISITOR = PianoAnalytics.DEFAULT_STORAGE_LIFETIME_VISITOR;
    static final int DEFAULT_STORAGE_LIFETIME_USER = PianoAnalytics.DEFAULT_STORAGE_LIFETIME_USER;

    static final boolean DEFAULT_IGNORE_LIMITED_ADVERTISING_TRACKING = false;
    static final boolean DEFAULT_CRASH_DETECTION = true;
    static final boolean DEFAULT_SEND_WHEN_OPT_OUT = true;

    static final OfflineStorageMode DEFAULT_OFFLINE_STORAGE_MODE = OfflineStorageMode.NEVER;
    static final VisitorStorageMode DEFAULT_VISITOR_STORAGE_MODE = VisitorStorageMode.FIXED;
    static final VisitorIDType DEFAULT_VISITOR_ID_TYPE = VisitorIDType.UUID;
    static final EncryptionMode DEFAULT_ENCRYPTION_MODE = EncryptionMode.IF_COMPATIBLE;

    /// endregion

    /// region Constructors

    private final Set<String> rootProperties = new HashSet<>(Arrays.asList(
            ConfigurationKey.COLLECT_DOMAIN.stringValue(),
            ConfigurationKey.PATH.stringValue(),
            ConfigurationKey.SITE.stringValue(),
            ConfigurationKey.VISITOR_ID.stringValue(),
            ConfigurationKey.VISITOR_ID_TYPE.stringValue(),
            ConfigurationKey.CUSTOM_USER_AGENT.stringValue()));

    Configuration() {
    }

    Configuration(Map<String, Object> m) {
        if (m != null) {
            this.putAll(m);
        }
    }

    /// endregion

    /// region Package methods

    Configuration getRootConfiguration() {
        Configuration c = new Configuration();
        for (Map.Entry<String, Object> entry : this.entrySet()) {
            String key = entry.getKey();
            if (rootProperties.contains(key)) {
                c.put(key, entry.getValue());
            }
        }
        return c;
    }

    boolean containsKey(ConfigurationKey key) {
        return this.containsKey(key.stringValue());
    }

    void set(ConfigurationKey key, String value) {
        this.put(key.stringValue(), value);
    }

    /// endregion
}
