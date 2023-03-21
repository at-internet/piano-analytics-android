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
package io.piano.analytics;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

final class PrivacyStep implements WorkingQueue.IStep {

    /// region Constructors

    static PrivacyStep instance = null;
    private static Map<PianoAnalytics.PrivacyVisitorMode, Map<String, Object>> privacyDefaultConfig;

    private static final String privacyFileEvents = "events";
    private static final String privacyFileProperties = "properties";
    private static final String privacyFileStorage = "storage";
    private static final String privacyFileAuthorize = "allowed";
    private static final String privacyFileForbid = "forbidden";

    static PrivacyStep getInstance(Context ctx, Configuration configuration) {
        if (instance == null) {
            instance = new PrivacyStep(ctx, configuration);
        }
        return instance;
    }

    private final Map<String, Set<String>> authorizedEventsByMode;
    private final Map<String, Set<String>> forbiddenEventsByMode;

    private final Map<String, Map<String, Set<String>>> authorizedPropertiesByMode;
    private final Map<String, Map<String, Set<String>>> forbiddenPropertiesByMode;
    private final Map<String, Set<PianoAnalytics.PrivacyStorageFeature>> authorizedStorageFeatureByMode;
    private final Map<String, Set<PianoAnalytics.PrivacyStorageFeature>> forbiddenStorageFeatureByMode;
    private final Map<PianoAnalytics.PrivacyStorageFeature, Set<String>> storageKeysByFeature;
    private String defaultPrivacyMode;
    private int storageLifetimePrivacy;
    private final SharedPreferences sharedPreferences;
    private boolean inNoConsentMode = false;
    private boolean inNoStorageMode = false;

    /// region Map constructors

    private Map<String, Set<String>> createEventsNameByModeMapFromAction(String action) {
        Map<String, Set<String>> m = new HashMap<>();

        for (PianoAnalytics.PrivacyVisitorMode mode : PianoAnalytics.PrivacyVisitorMode.values()) {
            Map<String, Object> events = getDeepInPrivacyConfig(mode, privacyFileEvents, action, new HashMap<>());
            m.put(mode.stringValue(), events.keySet());
        }

        return m;
    }

    private Map<String, Map<String, Set<String>>> createPropertiesByModeMapFromAction(String action) {
        Map<String, Map<String, Set<String>>> m = new HashMap<>();

        for (PianoAnalytics.PrivacyVisitorMode mode : PianoAnalytics.PrivacyVisitorMode.values()) {
            Map<String, Set<String>> properties = new HashMap<>();

            Map<String, Map<String, Boolean>> propertiesByEventName = getDeepInPrivacyConfig(mode, privacyFileProperties, action, new HashMap<>());
            for (Map.Entry<String, Map<String, Boolean>> entry : propertiesByEventName.entrySet()) {
                properties.put(entry.getKey(), entry.getValue().keySet());
            }

            m.put(mode.stringValue(), properties);
        }

        return m;
    }

    private Map<String, Set<PianoAnalytics.PrivacyStorageFeature>> createStorageFeatureByModeMapFromAction(String action) {
        Map<String, Set<PianoAnalytics.PrivacyStorageFeature>> m = new HashMap<>();

        for (PianoAnalytics.PrivacyVisitorMode mode : PianoAnalytics.PrivacyVisitorMode.values()) {
            Map<String, Boolean> storage = getDeepInPrivacyConfig(mode, privacyFileStorage, action, new HashMap<>());
            Set<PianoAnalytics.PrivacyStorageFeature> storageFeatures = new HashSet<>();
            for (String feature : storage.keySet()) {
                if (feature.equals(WILDCARD)) {
                    Collections.addAll(storageFeatures, PianoAnalytics.PrivacyStorageFeature.values());
                    break;
                }
                PianoAnalytics.PrivacyStorageFeature feat = PianoAnalytics.PrivacyStorageFeature.fromString(feature);
                if (feat != null) {
                    storageFeatures.add(feat);
                }
            }
            m.put(mode.stringValue(), storageFeatures);
        }

        return m;
    }

    private Map<PianoAnalytics.PrivacyStorageFeature, Set<String>> createStorageKeysByFeatureMap() {
        Map<PianoAnalytics.PrivacyStorageFeature, Set<String>> m = new HashMap<>();
        m.put(PianoAnalytics.PrivacyStorageFeature.VISITOR, new HashSet<>(Arrays.asList(
                PreferencesKeys.VISITOR_UUID_AT,
                PreferencesKeys.VISITOR_UUID,
                PreferencesKeys.VISITOR_UUID_GENERATION_TIMESTAMP)));

        m.put(PianoAnalytics.PrivacyStorageFeature.PRIVACY, new HashSet<>(Arrays.asList(
                PreferencesKeys.PRIVACY_MODE,
                PreferencesKeys.PRIVACY_MODE_EXPIRATION_TIMESTAMP,
                PreferencesKeys.PRIVACY_VISITOR_ID,
                PreferencesKeys.PRIVACY_VISITOR_CONSENT)));

        m.put(PianoAnalytics.PrivacyStorageFeature.CRASH, new HashSet<>(Arrays.asList(
                PreferencesKeys.CRASHED,
                PreferencesKeys.CRASH_INFO)));

        m.put(PianoAnalytics.PrivacyStorageFeature.LIFECYCLE, new HashSet<>(Arrays.asList(
                PreferencesKeys.FIRST_INIT_LIFECYCLE_DONE,
                PreferencesKeys.INIT_LIFECYCLE_DONE,
                PreferencesKeys.FIRST_SESSION,
                PreferencesKeys.FIRST_SESSION_AFTER_UPDATE,
                PreferencesKeys.SESSION_COUNT,
                PreferencesKeys.SESSION_COUNT_SINCE_UPDATE,
                PreferencesKeys.DAYS_SINCE_FIRST_SESSION,
                PreferencesKeys.DAYS_SINCE_LAST_SESSION,
                PreferencesKeys.DAYS_SINCE_UPDATE,
                PreferencesKeys.FIRST_SESSION_DATE,
                PreferencesKeys.FIRST_SESSION_DATE_AFTER_UPDATE,
                PreferencesKeys.LAST_SESSION_DATE,
                PreferencesKeys.VERSION_CODE_KEY
        )));

        m.put(PianoAnalytics.PrivacyStorageFeature.USER, new HashSet<>(Arrays.asList(
                PreferencesKeys.USER,
                PreferencesKeys.USER_GENERATION_TIMESTAMP
        )));
        return m;
    }

    private <T> T getDeepInPrivacyConfig(PianoAnalytics.PrivacyVisitorMode mode, String key1, String key2, T defaultValue) {
        Map<String, Object> privacyConfigForAll = privacyDefaultConfig.get(mode);
        if (privacyConfigForAll == null || !privacyConfigForAll.containsKey(key1)) {
            return defaultValue;
        }

        Map<String, T> eventsRule = (Map<String, T>) privacyConfigForAll.get(key1);
        if (eventsRule == null || !eventsRule.containsKey(key2)) {
            return defaultValue;
        }

        return eventsRule.get(key2);
    }

    /// endregion

    PrivacyStep(Context ctx, Configuration configuration) {
        sharedPreferences = ctx.getSharedPreferences(PreferencesKeys.PREFERENCES, Context.MODE_PRIVATE);

        try {
            String ccc = configuration.get(Configuration.ConfigurationKey.STORAGE_LIFETIME_PRIVACY);
            storageLifetimePrivacy = Integer.parseInt(ccc);
        } catch (NumberFormatException e) {
            PianoAnalytics.InternalLogger.severe("PrivacyStep constructor error on parsing " + Configuration.ConfigurationKey.STORAGE_LIFETIME_PRIVACY.stringValue());
        }
        if (PianoAnalyticsUtils.isEmptyUnsignedInteger(storageLifetimePrivacy)) {
            storageLifetimePrivacy = PianoAnalytics.DEFAULT_STORAGE_LIFETIME_PRIVACY;
        }

        String privacyConfigStr = "{\"optin\":{\"properties\":{\"include\":{\"visitor_privacy_consent\":true,\"visitor_privacy_mode\":\"optin\"},\"allowed\":{\"*\":{\"*\":true}},\"forbidden\":{\"*\":{}}},\"storage\":{\"allowed\":{\"*\":true},\"forbidden\":{}},\"events\":{\"allowed\":{\"*\":true},\"forbidden\":{}}},\"optout\":{\"visitorId\":\"OPT-OUT\",\"properties\":{\"include\":{\"visitor_privacy_consent\":false,\"visitor_privacy_mode\":\"optout\"},\"allowed\":{\"*\":{}},\"forbidden\":{\"*\":{}}},\"storage\":{\"allowed\":{\"pa_vid\":true,\"pa_privacy\":true},\"forbidden\":{}},\"events\":{\"allowed\":{\"*\":true},\"forbidden\":{}}},\"no-consent\":{\"visitorId\":\"Consent-NO\",\"properties\":{\"include\":{\"visitor_privacy_consent\":false,\"visitor_privacy_mode\":\"no-consent\"},\"allowed\":{\"*\":{}},\"forbidden\":{\"*\":{}}},\"storage\":{\"allowed\":{},\"forbidden\":{\"*\":true}},\"events\":{\"allowed\":{\"*\":true},\"forbidden\":{}}},\"no-storage\":{\"visitorId\":\"no-storage\",\"properties\":{\"include\":{\"visitor_privacy_consent\":false,\"visitor_privacy_mode\":\"no-storage\"},\"allowed\":{\"*\":{\"*\":true}},\"forbidden\":{\"*\":{}}},\"storage\":{\"allowed\":{},\"forbidden\":{\"*\":true}},\"events\":{\"allowed\":{\"*\":true},\"forbidden\":{}}},\"exempt\":{\"properties\":{\"include\":{\"visitor_privacy_consent\":false,\"visitor_privacy_mode\":\"exempt\"},\"allowed\":{\"*\":{\"app_crash\":true,\"app_crash_class\":true,\"app_crash_screen\":true,\"app_version\":true,\"browser\":true,\"browser_cookie_acceptance\":true,\"browser_group\":true,\"browser_version\":true,\"click\":true,\"click_chapter1\":true,\"click_chapter2\":true,\"click_chapter3\":true,\"click_full_name\":true,\"connection_monitor\":true,\"connection_organisation\":true,\"connection_type\":true,\"date\":true,\"date_day\":true,\"date_daynumber\":true,\"date_month\":true,\"date_monthnumber\":true,\"date_week\":true,\"date_year\":true,\"date_yearofweek\":true,\"device_brand\":true,\"device_display_height\":true,\"device_display_width\":true,\"device_manufacturer\":true,\"device_model\": true,\"device_name\":true,\"device_name_tech\":true,\"device_screen_diagonal\":true,\"device_screen_height\":true,\"device_screen_width\":true,\"device_timestamp_utc\":true,\"device_type\":true,\"event_collection_platform\":true,\"event_collection_version\":true,\"event_hour\":true,\"event_id\":true,\"event_minute\":true,\"event_name\":true,\"event_position\":true,\"event_second\":true,\"event_time\":true,\"event_time_utc\":true,\"event_url\":true,\"event_url_domain\":true,\"event_url_full\":true,\"exclusion_cause\":true,\"exclusion_type\":true,\"geo_city\":true,\"geo_continent\":true,\"geo_country\":true,\"geo_metro\":true,\"geo_region\":true,\"hit_time_utc\":true,\"os\":true,\"os_group\":true,\"os_version\":true,\"os_version_name\":true,\"page\":true,\"page_chapter1\":true,\"page_chapter2\":true,\"page_chapter3\":true,\"page_duration\":true,\"page_full_name\":true,\"page_position\":true,\"privacy_status\":true,\"site\":true,\"site_env\":true,\"site_id\":true,\"site_platform\":true,\"src\":true,\"src_detail\":true,\"src_direct_access\":true,\"src_organic\":true,\"src_organic_detail\":true,\"src_portal_domain\":true,\"src_portal_site\":true,\"src_portal_site_id\":true,\"src_portal_url\":true,\"src_referrer_site_domain\":true,\"src_referrer_site_url\":true,\"src_referrer_url\":true,\"src_se\":true,\"src_se_category\":true,\"src_se_country\":true,\"src_type\":true,\"src_url\":true,\"src_url_domain\":true,\"src_webmail\":true,\"visit_bounce\":true,\"visit_duration\":true,\"visit_entrypage\":true,\"visit_entrypage_chapter1\":true,\"visit_entrypage_chapter2\":true,\"visit_entrypage_chapter3\":true,\"visit_entrypage_full_name\":true,\"visit_exitpage\":true,\"visit_exitpage_chapter1\":true,\"visit_exitpage_chapter2\":true,\"visit_exitpage_chapter3\":true,\"visit_exitpage_full_name\":true,\"visit_hour\":true,\"visit_id\":true,\"visit_minute\":true,\"visit_page_view\":true,\"visit_second\":true,\"visit_time\":true,\"visit_privacy_consent\":true,\"visit_privacy_mode\":true}},\"forbidden\":{\"*\":{}}},\"storage\":{\"allowed\":{\"pa_vid\":true,\"pa_privacy\":true},\"forbidden\":{}},\"events\":{\"allowed\":{\"click.action\":true,\"click.download\":true,\"click.exit\":true,\"click.navigation\":true,\"page.display\":true},\"forbidden\":{}}},\"*\":{\"properties\":{\"allowed\":{\"*\":{\"connection_type\":true,\"device_timestamp_utc\":true,\"visitor_privacy_consent\":true,\"visitor_privacy_mode\":true}},\"forbidden\":{\"*\":{}}},\"storage\":{\"allowed\":{},\"forbidden\":{}},\"events\":{\"allowed\":{},\"forbidden\":{}}}}";
        privacyDefaultConfig = PianoAnalyticsUtils.getPrivacyConfig(privacyConfigStr);

        authorizedEventsByMode = createEventsNameByModeMapFromAction(privacyFileAuthorize);
        forbiddenEventsByMode = createEventsNameByModeMapFromAction(privacyFileForbid);
        authorizedPropertiesByMode = createPropertiesByModeMapFromAction(privacyFileAuthorize);
        forbiddenPropertiesByMode = createPropertiesByModeMapFromAction(privacyFileForbid);
        authorizedStorageFeatureByMode = createStorageFeatureByModeMapFromAction(privacyFileAuthorize);
        forbiddenStorageFeatureByMode = createStorageFeatureByModeMapFromAction(privacyFileForbid);
        storageKeysByFeature = createStorageKeysByFeatureMap();

        defaultPrivacyMode = configuration.get(Configuration.ConfigurationKey.PRIVACY_DEFAULT_MODE);
        if (PianoAnalyticsUtils.isEmptyString(defaultPrivacyMode) || !authorizedEventsByMode.containsKey(defaultPrivacyMode)) {
            defaultPrivacyMode = PianoAnalytics.PrivacyVisitorMode.OPTIN.stringValue();
        }
    }

    /// endregion

    /// region Constants

    static final String VISITOR_PRIVACY_CONSENT_PROPERTY = "visitor_privacy_consent";
    static final String VISITOR_PRIVACY_MODE_PROPERTY = "visitor_privacy_mode";
    static final String WILDCARD = "*";
    private static final String PAGE_PROPERTIES_FORMAT = "page_%s";

    /// endregion

    /// region Package methods

    final void setInNoConsent(boolean b) {
        this.inNoConsentMode = b;
    }

    @SafeVarargs
    final void storeData(SharedPreferences.Editor editor, PianoAnalytics.PrivacyStorageFeature storageFeature, Pair<String, Object>... pairs) {
        String visitorMode = getVisitorMode();

        for (Pair<String, Object> p : pairs) {
            String key = p.first;
            Object v = p.second;

            if (v != null && !getVisitorModeAuthorizedStorageFeature(visitorMode).contains(storageFeature) && !getVisitorModeAuthorizedStorageFeature(PianoAnalytics.PrivacyVisitorMode.ALL.stringValue()).contains(storageFeature)) {
                v = null;
            }
            if (v != null && (getVisitorModeForbiddenStorageFeature(visitorMode).contains(storageFeature) || getVisitorModeForbiddenStorageFeature(PianoAnalytics.PrivacyVisitorMode.ALL.stringValue()).contains(storageFeature))) {
                v = null;
            }

            if (v == null) {
                editor.remove(key);
            } else if (v instanceof Boolean) {
                editor.putBoolean(key, (Boolean) v);
            } else if (v instanceof String) {
                editor.putString(key, (String) v);
            } else if (v instanceof Long) {
                editor.putLong(key, (Long) v);
            } else if (v instanceof Integer) {
                editor.putInt(key, (Integer) v);
            }
        }
        editor.apply();
    }

    final <T> T getData(SharedPreferences prefs, PianoAnalytics.PrivacyStorageFeature storageFeature, String key, T defValue, T type) {
        Object value;

        if (storageFeature == null) {
            return null;
        } else if (type instanceof Boolean) {
            value = prefs.getBoolean(key, (boolean) defValue);
        } else if (type instanceof String) {
            value = prefs.getString(key, (String) defValue);
        } else if (type instanceof Long) {
            value = prefs.getLong(key, (Long) defValue);
        } else if (type instanceof Integer) {
            value = prefs.getInt(key, (Integer) defValue);
        } else {
            return null;
        }

        if (value != null && !getVisitorModeAuthorizedStorageFeature(getVisitorMode()).contains(storageFeature) && !getVisitorModeAuthorizedStorageFeature(PianoAnalytics.PrivacyVisitorMode.ALL.stringValue()).contains(storageFeature)) {
            storeData(prefs.edit(), storageFeature, new Pair<>(key, null));
            return null;
        }
        if (value != null && (getVisitorModeForbiddenStorageFeature(getVisitorMode()).contains(storageFeature) || getVisitorModeForbiddenStorageFeature(PianoAnalytics.PrivacyVisitorMode.ALL.stringValue()).contains(storageFeature))) {
            storeData(prefs.edit(), storageFeature, new Pair<>(key, null));
            return null;
        }

        return (T) value;
    }

    Set<PianoAnalytics.PrivacyStorageFeature> getVisitorModeAuthorizedStorageFeature(String visitorMode) {
        return MapUtils.getValueOrPutDefault(
                authorizedStorageFeatureByMode,
                visitorMode,
                MapUtils.getValueOrPutDefault(
                        authorizedStorageFeatureByMode,
                        PianoAnalytics.PrivacyVisitorMode.EXEMPT.stringValue(),
                        new HashSet<>()
                )
        );
    }

    Set<PianoAnalytics.PrivacyStorageFeature> getVisitorModeForbiddenStorageFeature(String visitorMode) {
        return MapUtils.getValueOrPutDefault(
                forbiddenStorageFeatureByMode,
                visitorMode,
                MapUtils.getValueOrPutDefault(
                        forbiddenStorageFeatureByMode,
                        PianoAnalytics.PrivacyVisitorMode.EXEMPT.stringValue(),
                        new HashSet<>()
                )
        );
    }

    Set<String> getVisitorModeAuthorizedEventsName(String visitorMode) {
        return MapUtils.getValueOrPutDefault(authorizedEventsByMode, visitorMode, new HashSet<>(Collections.singletonList(WILDCARD)));
    }

    Set<String> getVisitorModeForbiddenEventsName(String visitorMode) {
        return MapUtils.getValueOrPutDefault(forbiddenEventsByMode, visitorMode, new HashSet<>());
    }

    Map<String, Set<String>> getVisitorModeAuthorizedProperties(String visitorMode) {
        return MapUtils.getValueOrPutDefault(authorizedPropertiesByMode, visitorMode, new HashMap<>());
    }

    Map<String, Set<String>> getVisitorModeForbiddenProperties(String visitorMode) {
        return MapUtils.getValueOrPutDefault(forbiddenPropertiesByMode, visitorMode, new HashMap<>());
    }

    String getVisitorMode() {
        /// Specific case : no-consent and no-storage must not be stored in device
        if (inNoConsentMode) {
            return PianoAnalytics.PrivacyVisitorMode.NO_CONSENT.stringValue();
        } else if (inNoStorageMode) {
            return PianoAnalytics.PrivacyVisitorMode.NO_STORAGE.stringValue();
        }

        long privacyModeExpirationTs = sharedPreferences.getLong(PreferencesKeys.PRIVACY_MODE_EXPIRATION_TIMESTAMP, -1);
        if (privacyModeExpirationTs != -1 && PianoAnalyticsUtils.currentTimeMillis() >= privacyModeExpirationTs) {
            sharedPreferences.edit()
                    .putString(PreferencesKeys.PRIVACY_MODE, defaultPrivacyMode)
                    .putLong(PreferencesKeys.PRIVACY_MODE_EXPIRATION_TIMESTAMP, PianoAnalyticsUtils.currentTimeMillis() + (storageLifetimePrivacy * 86_400_000L))
                    .putBoolean(PreferencesKeys.PRIVACY_VISITOR_CONSENT, true)
                    .remove(PreferencesKeys.PRIVACY_VISITOR_ID)
                    .apply();
        }
        String visitorMode = sharedPreferences.getString(PreferencesKeys.PRIVACY_MODE, defaultPrivacyMode);
        if (!authorizedEventsByMode.containsKey(visitorMode)) {
            return defaultPrivacyMode;
        }
        return visitorMode;
    }

    boolean isAuthorizedEventName(String eventName, Set<String> authorizedEvents, Set<String> forbiddenEvents) {
        if (authorizedEvents.size() == 0) {
            return false;
        }

        boolean isAuthorized = false;
        for (String name : authorizedEvents) {
            int wildcardIndex = name.indexOf(WILDCARD);

            if (wildcardIndex == 0 ||
                    (wildcardIndex == -1 && eventName.equals(name)) ||
                    (wildcardIndex != -1 && name.startsWith(name.substring(0, wildcardIndex)))) {
                isAuthorized = true;
                break;
            }
        }
        if (!isAuthorized) {
            return false;
        }

        for (String name : forbiddenEvents) {
            int wildcardIndex = name.indexOf(WILDCARD);
            if (wildcardIndex == 0 ||
                    (wildcardIndex == -1 && eventName.equals(name)) ||
                    (wildcardIndex != -1 && name.startsWith(name.substring(0, wildcardIndex)))) {
                return false;
            }
        }
        return true;
    }

    @NonNull
    Set<String> getPropertiesFromEventName(@NonNull Map<String, Set<String>> propertiesKeysByEventNames, String... eventNames) {
        Set<String> authorizedPropertiesKeys = new HashSet<>();

        for (Map.Entry<String, Set<String>> eventNameEntry : propertiesKeysByEventNames.entrySet()) {
            int wildcardIndex = eventNameEntry.getKey().indexOf(WILDCARD);
            switch (wildcardIndex) {
                case 0:
                    /// WILDCARD ONLY
                    authorizedPropertiesKeys.addAll(eventNameEntry.getValue());
                    break;
                case -1:
                    for (String eventName : eventNames)
                        if (eventName.equals(eventNameEntry.getKey())) {
                            authorizedPropertiesKeys.addAll(eventNameEntry.getValue());
                        }
                    break;
                default:
                    for (String eventName : eventNames)
                        if (eventName.startsWith(eventNameEntry.getKey().substring(0, wildcardIndex))) {
                            authorizedPropertiesKeys.addAll(eventNameEntry.getValue());
                        }
            }
        }

        return authorizedPropertiesKeys;
    }

    Map<String, Object> applyAuthorizedPropertiesPrivacyRules(Map<String, Object> data, Set<String> authorizedPropertiesKeys) {
        Map<String, Object> resultData = new HashMap<>();
        for (String propertyKey : authorizedPropertiesKeys) {
            Object o;
            int wildcardIndex = propertyKey.indexOf(WILDCARD);
            switch (wildcardIndex) {
                case 0:
                    /// WILDCARD ONLY
                    return new HashMap<>(data);
                case -1:
                    o = data.get(propertyKey);
                    if (o != null) {
                        resultData.put(propertyKey, o);
                    }
                    break;
                default:
                    String prefixKey = propertyKey.substring(0, wildcardIndex);
                    for (Map.Entry<String, Object> property : data.entrySet()) {
                        String dataPropertyKey = property.getKey();
                        if (!dataPropertyKey.startsWith(prefixKey)) {
                            continue;
                        }
                        o = property.getValue();
                        if (o != null) {
                            resultData.put(dataPropertyKey, o);
                        }
                    }
                    break;
            }
        }
        return resultData;
    }

    Map<String, Object> applyForbiddenPropertiesPrivacyRules(Map<String, Object> data, Set<String> forbiddenPropertiesKeys) {
        Map<String, Object> resultData = new HashMap<>(data);
        for (String key : forbiddenPropertiesKeys) {
            int wildcardIndex = key.indexOf(WILDCARD);
            switch (wildcardIndex) {
                case 0:
                    /// WILDCARD ONLY
                    return new HashMap<>();
                case -1:
                    resultData.remove(key);
                    break;
                default:
                    String prefixKey = key.substring(0, wildcardIndex);
                    List<String> keysToDelete = new ArrayList<>();
                    for (String propertyKey : resultData.keySet()) {
                        if (propertyKey.startsWith(prefixKey)) {
                            keysToDelete.add(propertyKey);
                        }
                    }
                    for (String ktd : keysToDelete) {
                        resultData.remove(ktd);
                    }
                    break;
            }
        }
        return resultData;
    }

    /// endregion

    /// region Private methods

    private void clearStorageFromVisitorMode(String visitorMode, SharedPreferences.Editor editor) {
        for (Map.Entry<PianoAnalytics.PrivacyStorageFeature, Set<String>> entry : storageKeysByFeature.entrySet()) {
            PianoAnalytics.PrivacyStorageFeature storageFeature = entry.getKey();
            if (getVisitorModeAuthorizedStorageFeature(visitorMode).contains(storageFeature) || getVisitorModeAuthorizedStorageFeature(PianoAnalytics.PrivacyVisitorMode.ALL.stringValue()).contains(storageFeature)) {
                continue;
            }
            if (!getVisitorModeForbiddenStorageFeature(visitorMode).contains(storageFeature) || !getVisitorModeForbiddenStorageFeature(PianoAnalytics.PrivacyVisitorMode.ALL.stringValue()).contains(storageFeature)) {
                continue;
            }
            Set<String> keys = entry.getValue();
            for (String key : keys) {
                editor.remove(key);
            }
        }
        editor.apply();
    }

    private boolean getPrivacyVisitorConsent() {
        return sharedPreferences.getBoolean(PreferencesKeys.PRIVACY_VISITOR_CONSENT, false);
    }

    private String getPrivacyVisitorId() {
        if (inNoStorageMode) {
            return PianoAnalytics.PrivacyVisitorMode.NO_STORAGE.stringValue();
        }
        if (inNoConsentMode) {
            return PianoAnalytics.PrivacyVisitorMode.NO_CONSENT.stringValue();
        }
        if (PianoAnalytics.PrivacyVisitorMode.OPTOUT.stringValue().equals(getVisitorMode())) {
            return PianoAnalytics.PrivacyVisitorMode.OPTOUT.stringValue();
        }
        return sharedPreferences.getString(PreferencesKeys.PRIVACY_VISITOR_ID, null);
    }

    private int getPrivacyVisitorModeRemainingDuration() {
        long expiration = sharedPreferences.getLong(PreferencesKeys.PRIVACY_MODE_EXPIRATION_TIMESTAMP, -1);
        if (expiration == -1) {
            return 0;
        }
        long timeRemaining = expiration - PianoAnalyticsUtils.currentTimeMillis();
        return PianoAnalyticsUtils.convertMillisTo(TimeUnit.DAYS, timeRemaining);
    }

    private void updatePropertyMap(Map<String, Set<String>> currentPropertyKeys, Map<String, Set<String>> propertyKeysToAdd) {
        if (currentPropertyKeys == null) {
            currentPropertyKeys = new HashMap<>();
        }
        if (propertyKeysToAdd == null) {
            return;
        }

        for (Map.Entry<String, Set<String>> eventName : propertyKeysToAdd.entrySet()) {
            Set<String> lowercaseProperties = new HashSet<>();

            for (String propertyKey : eventName.getValue()) {
                lowercaseProperties.add(propertyKey.toLowerCase());
            }

            if (currentPropertyKeys.get(eventName.getKey().toLowerCase()) == null) {
                currentPropertyKeys.put(eventName.getKey().toLowerCase(), lowercaseProperties);
            } else {
                Set<String> currentProperties = currentPropertyKeys.get(eventName.getKey().toLowerCase());
                if (currentProperties == null) {
                    currentPropertyKeys.put(eventName.getKey().toLowerCase(), lowercaseProperties);
                } else {
                    Set<String> newProperties = new HashSet<>();
                    newProperties.addAll(currentProperties);
                    newProperties.addAll(lowercaseProperties);
                    currentPropertyKeys.put(eventName.getKey().toLowerCase(), newProperties);
                }
            }
        }
    }

    /// endregion

    /// region WorkingQueue.IStep implementation

    @Override
    public void processPrivacyMode(Model m) {
        m.setPrivacyModel(new PrivacyModel(this.getVisitorMode()));
    }

    @Override
    public void processUpdatePrivacyContext(Model m) {
        /// REQUIREMENTS
        PrivacyModel privacy = m.getPrivacyModel();
        String privacyVisitorMode = privacy.getVisitorMode();

        switch (privacy.getUpdateRequestKey()) {
            case NEW_VISITOR_MODE:
                this.authorizedEventsByMode.put(privacyVisitorMode, this.authorizedEventsByMode.get("exempt"));
                this.forbiddenEventsByMode.put(privacyVisitorMode, this.forbiddenEventsByMode.get("exempt"));
                this.authorizedPropertiesByMode.put(privacyVisitorMode, this.authorizedPropertiesByMode.get("exempt"));
                this.forbiddenPropertiesByMode.put(privacyVisitorMode, this.forbiddenPropertiesByMode.get("exempt"));
                this.authorizedStorageFeatureByMode.put(privacyVisitorMode, this.authorizedStorageFeatureByMode.get("exempt"));
                this.forbiddenStorageFeatureByMode.put(privacyVisitorMode, this.forbiddenStorageFeatureByMode.get("exempt"));
                break;
            case VISITOR_MODE:
                inNoConsentMode = privacyVisitorMode.equalsIgnoreCase(PianoAnalytics.PrivacyVisitorMode.NO_CONSENT.stringValue());
                inNoStorageMode = privacyVisitorMode.equalsIgnoreCase(PianoAnalytics.PrivacyVisitorMode.NO_STORAGE.stringValue());

                SharedPreferences.Editor editor = sharedPreferences.edit();

                int newDuration = privacy.getDuration();
                if (newDuration <= 0) {
                    newDuration = this.storageLifetimePrivacy;
                }

                String currentVisitorMode = getVisitorMode();
                if (currentVisitorMode.equals(PianoAnalytics.PrivacyVisitorMode.OPTOUT.stringValue())
                        || currentVisitorMode.equals(PianoAnalytics.PrivacyVisitorMode.NO_STORAGE.stringValue())
                        || currentVisitorMode.equals(PianoAnalytics.PrivacyVisitorMode.NO_CONSENT.stringValue())) {
                    storeData(editor, PianoAnalytics.PrivacyStorageFeature.VISITOR, new Pair<>(PreferencesKeys.VISITOR_UUID, null));
                }

                /// Update storage
                clearStorageFromVisitorMode(privacyVisitorMode, editor);
                storeData(editor, PianoAnalytics.PrivacyStorageFeature.PRIVACY,
                        new Pair<>(PreferencesKeys.PRIVACY_MODE, privacyVisitorMode),
                        new Pair<>(PreferencesKeys.PRIVACY_MODE_EXPIRATION_TIMESTAMP, PianoAnalyticsUtils.currentTimeMillis() + (newDuration * 86_400_000L)), /// days to millis
                        new Pair<>(PreferencesKeys.PRIVACY_VISITOR_CONSENT, privacy.isVisitorConsent()),
                        new Pair<>(PreferencesKeys.PRIVACY_VISITOR_ID, privacy.getCustomVisitorId()));
                break;
            case EVENTS_NAME:
                Set<String> lowercaseEventsName = new HashSet<>();
                for (String n : privacy.getAuthorizedEventsName()) {
                    lowercaseEventsName.add(n.toLowerCase());
                }
                Set<String> authorizedEventsName = SetUtils.copy(getVisitorModeAuthorizedEventsName(privacyVisitorMode));
                authorizedEventsName.addAll(lowercaseEventsName);
                authorizedEventsByMode.put(privacyVisitorMode, authorizedEventsName);

                lowercaseEventsName = new HashSet<>();
                for (String n : privacy.getForbiddenEventsName()) {
                    lowercaseEventsName.add(n.toLowerCase());
                }
                Set<String> forbiddenEventsName = SetUtils.copy(getVisitorModeForbiddenEventsName(privacyVisitorMode));
                forbiddenEventsName.addAll(lowercaseEventsName);
                forbiddenEventsByMode.put(privacyVisitorMode, forbiddenEventsName);

                break;
            case PROPERTIES:
                updatePropertyMap(getVisitorModeAuthorizedProperties(privacyVisitorMode), privacy.getAuthorizedPropertyKeys());
                updatePropertyMap(getVisitorModeForbiddenProperties(privacyVisitorMode), privacy.getForbiddenPropertyKeys());
                break;
            case STORAGE:
                getVisitorModeAuthorizedStorageFeature(privacyVisitorMode).addAll(privacy.getAuthorizedStorageFeatures());
                getVisitorModeForbiddenStorageFeature(privacyVisitorMode).addAll(privacy.getForbiddenStorageFeatures());
                break;
            default:
                PianoAnalytics.InternalLogger.warning("PrivacyStep.processUpdatePrivacyContext : unknown update request key case");
                break;
        }
    }

    @Override
    public void processGetModel(Context ctx, Model m) {
        String visitorMode = getVisitorMode();
        m.setPrivacyModel(new PrivacyModel(visitorMode)
                .setAuthorizedEventsName(SetUtils.copy(getVisitorModeAuthorizedEventsName(visitorMode)))
                .setForbiddenEventsName(SetUtils.copy(getVisitorModeForbiddenEventsName(visitorMode)))
                .setAuthorizedPropertyKeys(MapUtils.copy(getVisitorModeAuthorizedProperties(visitorMode)))
                .setForbiddenPropertyKeys(MapUtils.copy(getVisitorModeForbiddenProperties(visitorMode)))
                .setAuthorizedStorageFeatures(getVisitorModeAuthorizedStorageFeature(visitorMode))
                .setForbiddenStorageFeatures(getVisitorModeForbiddenStorageFeature(visitorMode))
                .setDuration(getPrivacyVisitorModeRemainingDuration())
                .setCustomVisitorId(getPrivacyVisitorId())
                .setVisitorConsent(getPrivacyVisitorConsent()));
    }

    @Override
    public boolean processTrackEvents(Context ctx, Model m, PianoAnalytics.OnWorkListener l) {
        /// REQUIREMENTS
        Configuration configuration = m.getConfiguration();
        Map<String, Object> contextProperties = m.getContextProperties();
        List<Event> events = m.getEvents();

        String privacyVisitorMode = getVisitorMode();

        if (privacyVisitorMode.equals(PianoAnalytics.PrivacyVisitorMode.OPTOUT.stringValue()) && !CastUtils.toBool(configuration.get(Configuration.ConfigurationKey.SEND_EVENT_WHEN_OPT_OUT))) {
            PianoAnalytics.InternalLogger.warning("PrivacyStep.processTrackEvents : user opted out and send when opt-out disabled");

            return false;
        }

        Set<String> authorizedEvents = SetUtils.copy(getVisitorModeAuthorizedEventsName(privacyVisitorMode));
        Set<String> forbiddenEvents = SetUtils.copy(getVisitorModeForbiddenEventsName(privacyVisitorMode));
        authorizedEvents = SetUtils.getSingletonIfContains(authorizedEvents, WILDCARD);
        forbiddenEvents = SetUtils.getSingletonIfContains(forbiddenEvents, WILDCARD);

        Map<String, Set<String>> authorizedPropertiesKeys = MapUtils.mergeSets(getVisitorModeAuthorizedProperties(privacyVisitorMode), getVisitorModeAuthorizedProperties(WILDCARD));
        Map<String, Set<String>> forbiddenPropertiesKeys = MapUtils.mergeSets(getVisitorModeForbiddenProperties(privacyVisitorMode), getVisitorModeForbiddenProperties(WILDCARD));
        MapUtils.minimizeSet(authorizedPropertiesKeys, WILDCARD);
        MapUtils.minimizeSet(forbiddenPropertiesKeys, WILDCARD);

        /// EVENTS
        List<Event> resultEvents = new ArrayList<>();
        for (Event e : events) {
            if (isAuthorizedEventName(e.getName(), authorizedEvents, forbiddenEvents)) {
                Map<String, Object> data = e.getData();

                data = applyAuthorizedPropertiesPrivacyRules(data, getPropertiesFromEventName(authorizedPropertiesKeys, e.getName(), WILDCARD));
                data = applyForbiddenPropertiesPrivacyRules(data, getPropertiesFromEventName(forbiddenPropertiesKeys, e.getName(), WILDCARD));

                Map<String, Object> contextPropertiesForEvent = MapUtils.copy(contextProperties);
                contextPropertiesForEvent = applyAuthorizedPropertiesPrivacyRules(contextPropertiesForEvent, getPropertiesFromEventName(authorizedPropertiesKeys, e.getName()));
                contextPropertiesForEvent = applyForbiddenPropertiesPrivacyRules(contextPropertiesForEvent, getPropertiesFromEventName(forbiddenPropertiesKeys, e.getName()));

                for (Map.Entry<String, Object> cp : contextPropertiesForEvent.entrySet()) {
                    if (!data.containsKey(cp.getKey()))
                        data.put(cp.getKey(), cp.getValue());
                }

                data.put(VISITOR_PRIVACY_MODE_PROPERTY, privacyVisitorMode);

                switch (PianoAnalytics.PrivacyVisitorMode.fromString(privacyVisitorMode)) {
                    case OPTIN:
                        data.put(VISITOR_PRIVACY_CONSENT_PROPERTY, true);
                        break;
                    case OPTOUT:
                        data.put(VISITOR_PRIVACY_CONSENT_PROPERTY, false);
                        configuration.set(Configuration.ConfigurationKey.VISITOR_ID, "opt-out");
                        break;
                    case NO_CONSENT:
                        data.put(VISITOR_PRIVACY_CONSENT_PROPERTY, false);
                        configuration.set(Configuration.ConfigurationKey.VISITOR_ID, "Consent-NO");
                        break;
                    case NO_STORAGE:
                        data.put(VISITOR_PRIVACY_CONSENT_PROPERTY, false);
                        configuration.set(Configuration.ConfigurationKey.VISITOR_ID, "no-storage");
                        break;
                    case EXEMPT:
                        data.put(VISITOR_PRIVACY_CONSENT_PROPERTY, false);
                        break;
                    default:
                        /// CUSTOM
                        data.put(VISITOR_PRIVACY_CONSENT_PROPERTY, getPrivacyVisitorConsent());
                        String privacyCustomVisitorId = getPrivacyVisitorId();
                        if (!PianoAnalyticsUtils.isEmptyString(privacyCustomVisitorId)) {
                            configuration.set(Configuration.ConfigurationKey.VISITOR_ID, privacyCustomVisitorId);
                        }
                }

                resultEvents.add(new Event(e.getName(), data));
            }
        }

        if (resultEvents.isEmpty()) {
            PianoAnalytics.InternalLogger.fine("No more events have to be sent, end of process");
            return false;
        }

        /// CONTEXT PROPERTIES

        m.setContextProperties(contextProperties)
                .setEvents(resultEvents);
        return true;
    }

    /// endregion
}
