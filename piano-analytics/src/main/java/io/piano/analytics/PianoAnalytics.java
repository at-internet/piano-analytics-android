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

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public final class PianoAnalytics {

    /// region PUBLIC SECTION

    /// region Enums

    static final Logger InternalLogger = Logger.getLogger("PIANO-ANALYTICS");
    static final int DEFAULT_STORAGE_LIFETIME_PRIVACY = 395;
    static final int DEFAULT_STORAGE_LIFETIME_VISITOR = 395;
    static final int DEFAULT_STORAGE_LIFETIME_USER = 395;

    /// endregion

    /// region Privacy options

    private final Map<String, Boolean> visitorConsentByMode = getVisitorConsentByMode();
    private final Map<String, String> customVisitorIDByMode = getCustomVisitorIDByMode();

    /// endregion

    /// region OnWorkListener
    private static final String DEFAULT_CONFIG_FILE = "piano-analytics-config.json";

    /// endregion

    /// region OnGetModelListener
    private static PianoAnalytics instance = null;

    /// endregion
    private final WorkingQueue queue;

    private PianoAnalytics(Context ctx, String configFileLocation) {
        queue = new WorkingQueue(ctx.getApplicationContext(), configFileLocation);
    }

    /***
     * Simple default getInstance
     * @param ctx activity context
     */
    public static PianoAnalytics getInstance(@NonNull Context ctx) {
        return getInstance(ctx, DEFAULT_CONFIG_FILE);
    }

    /***
     * Specific getInstance
     * @param ctx activity context
     * @param configFileLocation file path from assets folder
     */
    public static PianoAnalytics getInstance(@NonNull Context ctx, @NonNull String configFileLocation) {
        if (instance == null) {
            instance = new PianoAnalytics(ctx, configFileLocation);
        }
        return instance;
    }

    private Map<String, Boolean> getVisitorConsentByMode() {
        Map<String, Boolean> m = new HashMap<>();
        m.put(PrivacyVisitorMode.OPTIN.stringValue(), true);
        m.put(PrivacyVisitorMode.OPTOUT.stringValue(), false);
        m.put(PrivacyVisitorMode.NO_CONSENT.stringValue(), false);
        m.put(PrivacyVisitorMode.NO_STORAGE.stringValue(), false);
        m.put(PrivacyVisitorMode.EXEMPT.stringValue(), false);
        m.put(PrivacyVisitorMode.ALL.stringValue(), false);
        return m;
    }

    private Map<String, String> getCustomVisitorIDByMode() {
        Map<String, String> m = new HashMap<>();
        m.put(PrivacyVisitorMode.OPTIN.stringValue(), null);
        m.put(PrivacyVisitorMode.OPTOUT.stringValue(), "opt-out");
        m.put(PrivacyVisitorMode.NO_CONSENT.stringValue(), "no-consent");
        m.put(PrivacyVisitorMode.NO_STORAGE.stringValue(), "no-storage");
        m.put(PrivacyVisitorMode.EXEMPT.stringValue(), "exempt");
        m.put(PrivacyVisitorMode.ALL.stringValue(), null);
        return m;
    }

    private void createMode(String mode, boolean consent) {
        visitorConsentByMode.put(mode, consent);
        customVisitorIDByMode.put(mode, mode);
    }

    private Boolean getVisitorConsentByMode(String mode) {
        return MapUtils.getFirstValue(visitorConsentByMode, mode, PrivacyVisitorMode.ALL.stringValue());
    }

    private String getCustomVisitorIDByMode(String mode) {
        return MapUtils.getFirstValue(customVisitorIDByMode, mode, PrivacyVisitorMode.ALL.stringValue());
    }

    private boolean modeExists(String mode) {
        return visitorConsentByMode.containsKey(mode);
    }

    /***
     * Send event data
     * @param event a custom event
     */
    public void sendEvent(@NonNull Event event) {
        sendEvents(Collections.singletonList(event), null, null);
    }

    /***
     * Send event data
     * @param event a custom event
     * @param config custom config used only for this action
     */
    public void sendEvent(@NonNull Event event, Configuration config) {
        sendEvents(Collections.singletonList(event), config, null);
    }

    /***
     * Send event data
     * @param event a custom event
     * @param config custom config used only for this action
     * @param l work listener to leave customer handling
     */
    public void sendEvent(@NonNull Event event, Configuration config, OnWorkListener l) {
        sendEvents(Collections.singletonList(event), config, l);
    }

    /***
     * Send events data
     * @param events a custom event list
     */
    public void sendEvents(@NonNull List<Event> events) {
        sendEvents(events, null, null);
    }

    /***
     * Send events data
     * @param events a custom event list
     * @param config custom config used only for this action
     */
    public void sendEvents(@NonNull List<Event> events, Configuration config) {
        sendEvents(events, config, null);
    }

    /***
     * Send events data
     * @param events a custom event list
     * @param config custom config used only for this action
     * @param l work listener to leave customer handling
     */
    public void sendEvents(@NonNull List<Event> events, Configuration config, OnWorkListener l) {
        if (ListUtils.isEmpty(events)) {
            return;
        }

        Model m = new Model()
                .setEvents(new ArrayList<>(events));

        if (config != null) {
            if (config.containsKey(Configuration.ConfigurationKey.VISITOR_ID)) {
                config.set(Configuration.ConfigurationKey.VISITOR_ID_TYPE, Configuration.VisitorIDType.CUSTOM.stringValue());
            }
            m.Config(config.getRootConfiguration());
        }
        queue.push(WorkingQueue.ProcessingType.TRACK_EVENTS, m, l);
    }

    /***
     * Get configuration
     * @param key configuration key to get
     * @param l work listener to leave customer handling
     */
    public void getConfiguration(Configuration.ConfigurationKey key, OnGetConfigurationListener l) {
        queue.getConfigurationAsync(key, l);
    }

    /***
     * Update configuration
     * @param key configuration key
     * @param value configuration value
     */
    public void setConfiguration(Configuration.ConfigurationKey key, String value) {
        Configuration config = new Configuration();
        config.set(key, value);
        setConfiguration(config);
    }

    /***
     * Update configuration
     * @param config configuration object
     */
    @SuppressWarnings("ConstantConditions") // checking if @NonNull variable is null (java exemption)
    public void setConfiguration(@NonNull Configuration config) {
        if (config == null) {
            return;
        }
        Model m = new Model()
                .Config(new Configuration(config));
        queue.push(WorkingQueue.ProcessingType.SET_CONFIG, m, null);
    }

    /***
     * Update customer context
     * @param key property key
     * @param value property value
     */
    public void setProperty(String key, Object value) {
        setProperty(key, value, false, null);
    }

    /***
     * Update customer context
     * @param key property key
     * @param value property value
     * @param persistent whether the property will be persistent or not
     */
    public void setProperty(String key, Object value, Boolean persistent) {
        setProperty(key, value, persistent, null);
    }

    /***
     * Update customer context
     * @param key property key
     * @param value property value
     * @param persistent whether the property will be persistent or not
     * @param events will send the property only those specific events
     */
    public void setProperty(String key, Object value, Boolean persistent, String[] events) {
        Map<String, Object> data = new HashMap<>();
        data.put(key, value);
        setProperties(data, persistent, events);
    }

    /***
     * dictionary of properties to set
     * @param data property key
     */
    public void setProperties(@NonNull Map<String, Object> data) {
        setProperties(data, false, null);
    }

    /***
     * dictionary of properties to set
     * @param data property key
     * @param persistent whether the property will be persistent or not
     */
    public void setProperties(@NonNull Map<String, Object> data, Boolean persistent) {
        setProperties(data, persistent, null);
    }

    /***
     * dictionary of properties to set
     * @param data property key
     * @param persistent whether the property will be persistent or not
     * @param events will send the property only those specific events
     */
    @SuppressWarnings("ConstantConditions") // checking if @NonNull variable is null (java exemption)
    public void setProperties(@NonNull Map<String, Object> data, Boolean persistent, String[] events) {
        if (data == null || data.isEmpty()) {
            return;
        }

        Model m = new Model()
                .setCustomerContextModel(
                        new CustomerContextModel(
                                CustomerContextModel.UpdateTypeKey.ADD,
                                MapUtils.toFlatten(data),
                                persistent,
                                events
                        )
                );
        queue.push(WorkingQueue.ProcessingType.UPDATE_CONTEXT, m, null);
    }

    /***
     * Delete property from customer context
     * @param key customer context property key
     */
    public void deleteProperty(@NonNull String key) {
        if (PianoAnalyticsUtils.isEmptyString(key)) {
            return;
        }
        Map<String, Object> properties = new HashMap<>();
        properties.put(key, true);

        Model m = new Model()
                .setCustomerContextModel(new CustomerContextModel(CustomerContextModel.UpdateTypeKey.DELETE, properties));
        queue.push(WorkingQueue.ProcessingType.UPDATE_CONTEXT, m, null);
    }

    /// region Privacy

    public void getUser(OnGetUserListener l) {
        if (l == null) {
            return;
        }
        queue.getUserAsync(l);
    }

    /***
     * Set user
     * @param userId new user id
     */
    public void setUser(@NonNull String userId) {
        setUser(userId, null, true);
    }

    /***
     * Set user
     * @param userId new user id
     * @param userCategory new user category
     */
    public void setUser(@NonNull String userId, String userCategory) {
        setUser(userId, userCategory, true);
    }

    /***
     * Set user
     * @param userId new user id
     * @param userCategory new user category
     * @param enableStorage to store user in user defaults
     */
    public void setUser(@NonNull String userId, String userCategory, boolean enableStorage) {
        if (PianoAnalyticsUtils.isEmptyString(userId)) {
            return;
        }

        Model m = new Model()
                .setUserModel(new UserModel(UserModel.UpdateTypeKey.SET, enableStorage)
                        .setUser(new User(userId).setCategory(userCategory)));
        queue.push(WorkingQueue.ProcessingType.UPDATE_CONTEXT, m, null);
    }

    /***
     * Delete user
     */
    public void deleteUser() {
        Model m = new Model()
                .setUserModel(new UserModel(UserModel.UpdateTypeKey.DELETE, false));
        queue.push(WorkingQueue.ProcessingType.UPDATE_CONTEXT, m, null);
    }

    /***
     * Create privacy context
     * @param mode a new privacy visitor mode
     * @param consent if user consented
     */
    public void privacyCreateMode(@NonNull String mode, boolean consent) {
        if (PianoAnalyticsUtils.isEmptyString(mode) || mode.equals(PrivacyStep.WILDCARD) || modeExists(mode)) {
            return;
        }

        createMode(mode, consent);

        PrivacyModel privacyModel = new PrivacyModel(mode, PrivacyModel.UpdateDataKey.NEW_VISITOR_MODE)
                .setVisitorConsent(consent)
                .setCustomVisitorId(mode);

        Model m = new Model()
                .setPrivacyModel(privacyModel);

        queue.push(WorkingQueue.ProcessingType.UPDATE_PRIVACY_CONTEXT, m, null);
    }

    /***
     * Update privacy context
     * @param mode a privacy visitor mode
     */
    public void privacySetMode(@NonNull String mode) {
        if (PianoAnalyticsUtils.isEmptyString(mode) || mode.equals(PrivacyStep.WILDCARD) || !modeExists(mode)) {
            return;
        }

        Boolean consent = getVisitorConsentByMode(mode);
        String customVisitorID = getCustomVisitorIDByMode(mode);

        if (PianoAnalyticsUtils.isEmptyString(customVisitorID)) {
            customVisitorID = mode;
        }

        PrivacyModel privacyModel = new PrivacyModel(mode, PrivacyModel.UpdateDataKey.VISITOR_MODE)
                .setVisitorConsent(consent)
                .setCustomVisitorId(customVisitorID);

        Model m = new Model()
                .setPrivacyModel(privacyModel);

        queue.push(WorkingQueue.ProcessingType.UPDATE_PRIVACY_CONTEXT, m, null);
    }

    public void privacyGetMode(OnGetPrivacyModeListener l) {
        queue.getPrivacyModeAsync(l);
    }

    /***
     * add privacy visitor mode authorized events name
     * @param eventName string of an event name appended with what is currently set
     */
    public void privacyIncludeEvent(String eventName) {
        privacyIncludeEvents(new String[]{eventName}, new String[]{PrivacyVisitorMode.ALL.stringValue()});
    }

    /***
     * add privacy visitor mode authorized events name
     * @param eventName string of an event name appended with what is currently set
     * @param privacyModes a privacy visitor mode
     */
    public void privacyIncludeEvent(String eventName, String[] privacyModes) {
        privacyIncludeEvents(new String[]{eventName}, privacyModes);
    }

    /***
     * add privacy visitor mode authorized events name
     * @param eventsName string set of events name appended with currently set
     */
    public void privacyIncludeEvents(@NonNull String[] eventsName) {
        privacyIncludeEvents(eventsName, new String[]{PrivacyVisitorMode.ALL.stringValue()});
    }

    /***
     * add privacy visitor mode authorized events name
     * @param eventsName string set of events name appended with currently set
     * @param privacyModes a set of privacy visitor modes or by default applies to all modes
     */
    @SuppressWarnings("ConstantConditions") // checking if @NonNull variable is null (java exemption)
    public void privacyIncludeEvents(@NonNull String[] eventsName, String[] privacyModes) {
        if (eventsName == null || eventsName.length == 0) {
            return;
        }
        if (privacyModes == null || privacyModes.length == 0) {
            privacyModes = new String[]{PrivacyStep.WILDCARD};
        }

        for (String privacyMode : privacyModes) {
            PrivacyModel privacyModel = new PrivacyModel(privacyMode, PrivacyModel.UpdateDataKey.EVENTS_NAME)
                    .setAuthorizedEventsName(new HashSet<>(Arrays.asList(eventsName)));

            Model m = new Model()
                    .setPrivacyModel(privacyModel);
            queue.push(WorkingQueue.ProcessingType.UPDATE_PRIVACY_CONTEXT, m, null);
        }
    }

    /***
     * add privacy visitor mode forbidden events name
     * @param eventName string of an event name appended with what is currently set
     */
    public void privacyExcludeEvent(String eventName) {
        privacyExcludeEvents(new String[]{eventName}, new String[]{PrivacyVisitorMode.ALL.stringValue()});
    }

    /***
     * add privacy visitor mode forbidden events name
     * @param eventName string of an event name appended with what is currently set
     * @param privacyModes a privacy visitor mode
     */
    public void privacyExcludeEvent(String eventName, String[] privacyModes) {
        privacyExcludeEvents(new String[]{eventName}, privacyModes);
    }

    /***
     * add privacy visitor mode forbidden events name
     * @param eventsName string set of events name appended with currently set
     */
    public void privacyExcludeEvents(@NonNull String[] eventsName) {
        privacyExcludeEvents(eventsName, new String[]{PrivacyVisitorMode.ALL.stringValue()});
    }

    /***
     * add privacy visitor mode forbidden events name
     * @param eventsName string set of events name appended with currently set
     * @param privacyModes a privacy visitor mode
     */
    @SuppressWarnings("ConstantConditions") // checking if @NonNull variable is null (java exemption)
    public void privacyExcludeEvents(@NonNull String[] eventsName, String[] privacyModes) {
        if (eventsName == null || eventsName.length == 0) {
            return;
        }
        if (privacyModes == null || privacyModes.length == 0) {
            privacyModes = new String[]{PrivacyStep.WILDCARD};
        }

        for (String privacyMode : privacyModes) {
            PrivacyModel privacyModel = new PrivacyModel(privacyMode, PrivacyModel.UpdateDataKey.EVENTS_NAME)
                    .setForbiddenEventsName(new HashSet<>(Arrays.asList(eventsName)));

            Model m = new Model()
                    .setPrivacyModel(privacyModel);
            queue.push(WorkingQueue.ProcessingType.UPDATE_PRIVACY_CONTEXT, m, null);
        }
    }

    /// endregion

    /***
     * add privacy visitor mode authorized properties on events
     * @param property string property appended with what is currently set
     */
    public void privacyIncludeProperty(@NonNull String property) {
        privacyIncludeProperties(new String[]{property}, null, null);
    }

    /***
     * add privacy visitor mode authorized properties on events
     * @param property string property appended with what is currently set
     * @param privacyModes array of privacy modes on which we will authorize the properties, by default we authorize the properties for all the privacy modes
     */
    public void privacyIncludeProperty(@NonNull String property, String[] privacyModes) {
        privacyIncludeProperties(new String[]{property}, privacyModes, null);
    }

    /***
     * add privacy visitor mode authorized properties on events
     * @param property string property appended with what is currently set
     * @param privacyModes array of privacy modes on which we will authorize the properties, by default we authorize the properties for all the privacy modes
     * @param eventNames string property appended with what is currently set
     */
    public void privacyIncludeProperty(@NonNull String property, String[] privacyModes, String[] eventNames) {
        privacyIncludeProperties(new String[]{property}, privacyModes, eventNames);
    }

    /***
     * add privacy visitor mode authorized properties on events
     * @param properties string array of properties to authorize
     */
    public void privacyIncludeProperties(@NonNull String[] properties) {
        privacyIncludeProperties(properties, null, null);
    }

    /***
     * add privacy visitor mode authorized properties on events
     * @param properties string array of properties to authorize
     * @param privacyModes string array of modes to apply authorization
     */
    public void privacyIncludeProperties(@NonNull String[] properties, String[] privacyModes) {
        privacyIncludeProperties(properties, privacyModes, null);
    }

    /***
     * add privacy visitor mode authorized properties on events
     * @param properties string array of properties to authorize
     * @param privacyVisitorModes string array of modes to apply authorization
     * @param eventNames string array of event names to apply authorization
     */
    @SuppressWarnings("ConstantConditions") // checking if @NonNull variable is null (java exemption)
    public void privacyIncludeProperties(@NonNull String[] properties, String[] privacyVisitorModes, String[] eventNames) {
        if (properties == null || properties.length == 0) {
            return;
        }
        if (privacyVisitorModes == null || privacyVisitorModes.length == 0) {
            privacyVisitorModes = new String[]{PrivacyStep.WILDCARD};
        }
        if (eventNames == null || eventNames.length == 0) {
            eventNames = new String[]{PrivacyStep.WILDCARD};
        }

        for (String privacyVisitorMode : privacyVisitorModes) {
            Map<String, Set<String>> propertiesByEvents = new HashMap<>();
            for (String eventName : eventNames) {
                propertiesByEvents.put(eventName, new HashSet<>(Arrays.asList(properties)));
            }

            PrivacyModel privacyModel = new PrivacyModel(privacyVisitorMode, PrivacyModel.UpdateDataKey.PROPERTIES)
                    .setAuthorizedPropertyKeys(propertiesByEvents);

            Model m = new Model()
                    .setPrivacyModel(privacyModel);
            queue.push(WorkingQueue.ProcessingType.UPDATE_PRIVACY_CONTEXT, m, null);
        }
    }

    /***
     * add privacy visitor mode forbidden properties on events
     * @param property string property appended with what is currently set
     */
    public void privacyExcludeProperty(@NonNull String property) {
        privacyExcludeProperties(new String[]{property}, null, null);
    }

    /***
     * add privacy visitor mode forbidden properties on events
     * @param property string property appended with what is currently set
     * @param privacyModes string array of modes to apply forbiddance
     */
    public void privacyExcludeProperty(@NonNull String property, String[] privacyModes) {
        privacyExcludeProperties(new String[]{property}, privacyModes, null);
    }

    /***
     * add privacy visitor mode forbidden properties on events
     * @param property string property appended with what is currently set
     * @param privacyModes string array of modes to apply forbiddance
     * @param eventNames string array of event names to apply forbiddance
     */
    public void privacyExcludeProperty(@NonNull String property, String[] privacyModes, String[] eventNames) {
        privacyExcludeProperties(new String[]{property}, privacyModes, eventNames);
    }

    /***
     * add privacy visitor mode forbidden properties on events
     * @param properties string array of properties to forbid
     */
    public void privacyExcludeProperties(@NonNull String[] properties) {
        privacyExcludeProperties(properties, null, null);
    }

    /***
     * add privacy visitor mode forbidden properties on events
     * @param properties string array of properties to forbid
     * @param privacyModes string array of modes to apply forbiddance
     */
    public void privacyExcludeProperties(@NonNull String[] properties, String[] privacyModes) {
        privacyExcludeProperties(properties, privacyModes, null);
    }

    /***
     * add privacy visitor mode forbidden properties on events
     * @param properties string array of properties to forbid
     * @param privacyModes string array of modes to apply forbiddance
     * @param eventNames string array of event names to apply forbiddance
     */
    @SuppressWarnings("ConstantConditions") // checking if @NonNull variable is null (java exemption)
    public void privacyExcludeProperties(@NonNull String[] properties, String[] privacyModes, String[] eventNames) {
        if (properties == null || properties.length == 0) {
            return;
        }
        if (privacyModes == null || privacyModes.length == 0) {
            privacyModes = new String[]{PrivacyStep.WILDCARD};
        }
        if (eventNames == null || eventNames.length == 0) {
            eventNames = new String[]{PrivacyStep.WILDCARD};
        }

        for (String privacyVisitorMode : privacyModes) {
            Map<String, Set<String>> propertiesByEvents = new HashMap<>();
            for (String eventName : eventNames) {
                propertiesByEvents.put(eventName, new HashSet<>(Arrays.asList(properties)));
            }

            PrivacyModel privacyModel = new PrivacyModel(privacyVisitorMode, PrivacyModel.UpdateDataKey.PROPERTIES)
                    .setForbiddenPropertyKeys(propertiesByEvents);

            Model m = new Model()
                    .setPrivacyModel(privacyModel);
            queue.push(WorkingQueue.ProcessingType.UPDATE_PRIVACY_CONTEXT, m, null);
        }
    }

    /***
     * add privacy visitor mode storage
     * @param storageKey string of authorized key to store data into device
     */
    public void privacyIncludeStorageKey(@NonNull PrivacyStorageFeature storageKey) {
        privacyIncludeStorageKeys(new PrivacyStorageFeature[]{storageKey}, null);
    }

    /***
     * add privacy visitor mode storage
     * @param storageKey string of authorized key to store data into device
     * @param privacyModes an array of privacy visitor modes on which to include these keys
     */
    public void privacyIncludeStorageKey(@NonNull PrivacyStorageFeature storageKey, String[] privacyModes) {
        privacyIncludeStorageKeys(new PrivacyStorageFeature[]{storageKey}, privacyModes);
    }

    /***
     * add privacy visitor mode storage
     * @param storageKeys PrivacyStorageFeature set of authorized features to store data into device
     */
    public void privacyIncludeStorageKeys(@NonNull PrivacyStorageFeature[] storageKeys) {
        privacyIncludeStorageKeys(storageKeys, null);
    }

    /***
     * add privacy visitor mode storage
     * @param storageKeys PrivacyStorageFeature set of authorized features to store data into device
     * @param privacyModes an array of privacy visitor modes on which to include these keys
     */
    @SuppressWarnings("ConstantConditions") // checking if @NonNull variable is null (java exemption)
    public void privacyIncludeStorageKeys(@NonNull PrivacyStorageFeature[] storageKeys, String[] privacyModes) {
        if (storageKeys == null || storageKeys.length == 0) {
            return;
        }

        if (privacyModes == null || privacyModes.length == 0) {
            privacyModes = new String[]{PrivacyStep.WILDCARD};
        }

        for (String privacyVisitorMode : privacyModes) {
            PrivacyModel privacyModel = new PrivacyModel(privacyVisitorMode, PrivacyModel.UpdateDataKey.STORAGE)
                    .setAuthorizedStorageFeatures(new HashSet<>(Arrays.asList(storageKeys)));

            Model m = new Model()
                    .setPrivacyModel(privacyModel);
            queue.push(WorkingQueue.ProcessingType.UPDATE_PRIVACY_CONTEXT, m, null);
        }
    }

    /***
     * exclude privacy visitor mode storage
     * @param storageKey string of forbidden key to store data into device
     */
    public void privacyExcludeStorageKey(@NonNull PrivacyStorageFeature storageKey) {
        privacyExcludeStorageKeys(new PrivacyStorageFeature[]{storageKey}, null);
    }

    /***
     * exclude privacy visitor mode storage
     * @param storageKey string of forbidden key to store data into device
     * @param privacyModes an array of privacy visitor modes on which to include these keys
     */
    public void privacyExcludeStorageKey(@NonNull PrivacyStorageFeature storageKey, String[] privacyModes) {
        privacyExcludeStorageKeys(new PrivacyStorageFeature[]{storageKey}, privacyModes);
    }

    /***
     * exclude privacy visitor mode storage
     * @param storageKeys PrivacyStorageFeature set of forbidden features to store data into device
     */
    public void privacyExcludeStorageKeys(@NonNull PrivacyStorageFeature[] storageKeys) {
        privacyExcludeStorageKeys(storageKeys, null);
    }

    /***
     * exclude privacy visitor mode storage
     * @param storageKeys PrivacyStorageFeature set of forbidden features to store data into device
     * @param privacyModes an array of privacy visitor modes on which to include these keys
     */
    @SuppressWarnings("ConstantConditions") // checking if @NonNull variable is null (java exemption)
    public void privacyExcludeStorageKeys(@NonNull PrivacyStorageFeature[] storageKeys, String[] privacyModes) {
        if (storageKeys == null || storageKeys.length == 0) {
            return;
        }

        if (privacyModes == null || privacyModes.length == 0) {
            privacyModes = new String[]{PrivacyStep.WILDCARD};
        }

        for (String privacyVisitorMode : privacyModes) {
            PrivacyModel privacyModel = new PrivacyModel(privacyVisitorMode, PrivacyModel.UpdateDataKey.STORAGE)
                    .setForbiddenStorageFeatures(new HashSet<>(Arrays.asList(storageKeys)));

            Model m = new Model()
                    .setPrivacyModel(privacyModel);
            queue.push(WorkingQueue.ProcessingType.UPDATE_PRIVACY_CONTEXT, m, null);
        }
    }

    /// region Constructors

    /***
     * send offline data stored on device
     */
    public void sendOfflineData() {
        sendOfflineData(null, null);
    }

    /***
     * send offline data stored on device
     * @param config custom config used only for this action
     */
    public void sendOfflineData(Configuration config) {
        sendOfflineData(config, null);
    }

    /***
     * send offline data stored on device
     * @param config custom config used only for this action
     * @param l work listener to leave customer handling
     */
    public void sendOfflineData(Configuration config, OnWorkListener l) {
        Model m = new Model();
        if (config != null) {
            if (config.containsKey(Configuration.ConfigurationKey.VISITOR_ID)) {
                config.set(Configuration.ConfigurationKey.VISITOR_ID_TYPE, Configuration.VisitorIDType.CUSTOM.stringValue());
            }
            m.Config(config.getRootConfiguration());
        }

        queue.push(WorkingQueue.ProcessingType.SEND_OFFLINE_STORAGE, m, l);
    }

    /***
     * delete offline data stored on device
     */
    public void deleteOfflineStorage() {
        deleteOfflineStorage(0);
    }

    /***
     * delete offline data stored on device and keep only remaining days
     * @param remaining age of data which have to be kept (in days)
     */
    public void deleteOfflineStorage(int remaining) {
        Model m = new Model()
                .setStorageDaysRemaining(remaining);
        queue.push(WorkingQueue.ProcessingType.DELETE_OFFLINE_STORAGE, m, null);
    }

    /***
     * Get current visitor id
     * @param l work listener to leave customer handling
     */
    public void getVisitorId(OnGetPrivacyIdListener l) {
        queue.getConfigurationAsync(Configuration.ConfigurationKey.VISITOR_ID, l::onGetPrivacyId);
    }

    /***
     * Set current visitor id
     * @param visitorId custom visitor id to force in upcoming events
     */
    public void setVisitorId(String visitorId) {
        this.setConfiguration(Configuration.ConfigurationKey.VISITOR_ID, visitorId);
    }

    /***
     * Get all data in the model
     * @param l listener which have to be implemented to get data
     */
    @SuppressWarnings("ConstantConditions")
    public void getModel(@NonNull OnGetModelListener l) {
        if (l == null) {
            return;
        }
        queue.getModelAsync(l);
    }

    public enum PrivacyVisitorMode {
        OPTIN("optin"),
        OPTOUT("optout"),
        EXEMPT("exempt"),
        NO_CONSENT("no-consent"),
        NO_STORAGE("no-storage"),
        CUSTOM("custom"),
        ALL("*");

        private final String str;

        PrivacyVisitorMode(String val) {
            str = val;
        }

        public static PrivacyVisitorMode fromString(String s) {
            for (PrivacyVisitorMode v : values()) {
                if (v.stringValue().equalsIgnoreCase(s)) {
                    return v;
                }
            }
            PianoAnalytics.InternalLogger.severe("PrivacyVisitorMode.fromString: fallback on PrivacyVisitorMode.CUSTOM mode because requested value is unknown");
            return PrivacyVisitorMode.CUSTOM;
        }

        public String stringValue() {
            return str;
        }
    }

    /// endregion

    /// endregion

    /// region Constants

    public enum PrivacyStorageFeature {
        VISITOR("pa_vid"),
        CRASH("pa_crash"),
        LIFECYCLE("pa_lifecycle"),
        PRIVACY("pa_privacy"),
        USER("pa_uid");

        private final String str;

        PrivacyStorageFeature(String val) {
            str = val;
        }

        public static PrivacyStorageFeature fromString(String s) {
            for (PrivacyStorageFeature v : values()) {
                if (v.stringValue().equalsIgnoreCase(s)) {
                    return v;
                }
            }
            PianoAnalytics.InternalLogger.severe("PrivacyStorageFeature.fromString: fallback on null because requested value is unknown");
            return null;
        }

        public String stringValue() {
            return str;
        }
    }

    /***
     * Interface providing interaction possibilities
     */
    public interface OnWorkListener {
        /***
         * Called when raw data is available and customer want to override it before building
         * @param model all computed data
         * @return boolean indicates if process have to continue
         */
        default boolean onBeforeBuild(Model model) {
            return true;
        }

        /***
         * Called when built data is available and customer want to override sending method
         * @param built built data
         * @param stored stored data
         * @return boolean indicates if process have to continue
         */
        default boolean onBeforeSend(BuiltModel built, Map<String, BuiltModel> stored) {
            return true;
        }
    }

    public interface OnGetModelListener {
        /***
         * Called when model data is available and customer want to request it
         * @param model all computed data
         */
        void onGetModel(Model model);
    }

    public interface OnGetConfigurationListener {
        /***
         * Called when configuration data is available and customer want to request it
         * @param configurationValue current value of the requested key
         */
        void onGetConfiguration(String configurationValue);
    }

    public interface OnGetPrivacyModeListener {
        /***
         * Called when privacy mode data is available and customer want to request it
         * @param privacyMode current privacy mode
         */
        void onGetPrivacyMode(String privacyMode);
    }

    public interface OnGetPrivacyIdListener {
        /***
         * Called when privacy ID data is available and customer want to request it
         * @param privacyId current privacy ID
         */
        void onGetPrivacyId(String privacyId);
    }

    public interface OnGetUserListener {
        /***
         * Called when user data is available and customer want to request it
         * @param user current user
         */
        void onGetUser(User user);
    }

    /// endregion
}
