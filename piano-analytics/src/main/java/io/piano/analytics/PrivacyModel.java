package io.piano.analytics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class PrivacyModel {

    /// region enum

    enum UpdateDataKey {
        VISITOR_MODE,
        NEW_VISITOR_MODE,
        EVENTS_NAME,
        PROPERTIES,
        STORAGE
    }

    /// region PUBLIC SECTION

    public String getVisitorMode() {
        return visitorMode;
    }

    public Set<String> getAuthorizedEventsName() {
        return authorizedEventsName;
    }

    public Set<String> getForbiddenEventsName() {
        return forbiddenEventsName;
    }

    public Map<String, Set<String>> getAuthorizedPropertyKeys() {
        return authorizedPropertyKeys;
    }

    public Map<String, Set<String>> getForbiddenPropertyKeys() {
        return forbiddenPropertyKeys;
    }

    public Set<PianoAnalytics.PrivacyStorageFeature> getAuthorizedStorageFeatures() {
        return authorizedStorageFeatures;
    }

    public Set<PianoAnalytics.PrivacyStorageFeature> getForbiddenStorageFeatures() {
        return forbiddenStorageFeatures;
    }

    public int getDuration() {
        return duration;
    }

    public boolean isVisitorConsent() {
        return visitorConsent;
    }

    public String getCustomVisitorId() {
        return customVisitorId;
    }

    /// endregion

    /// region Constructors

    private final UpdateDataKey updateDataKey;
    private final String visitorMode;
    private Map<String, Set<String>> authorizedPropertyKeys = new HashMap<>();
    private Map<String, Set<String>> forbiddenPropertyKeys = new HashMap<>();
    private Set<String> authorizedEventsName = new HashSet<>();
    private Set<String> forbiddenEventsName = new HashSet<>();
    private Set<PianoAnalytics.PrivacyStorageFeature> authorizedStorageFeatures = new HashSet<>();
    private Set<PianoAnalytics.PrivacyStorageFeature> forbiddenStorageFeatures = new HashSet<>();
    private int duration;
    private boolean visitorConsent;
    private String customVisitorId;

    PrivacyModel(String visitorMode) {
        this(visitorMode, null);
    }

    PrivacyModel(String visitorMode, UpdateDataKey updateDataKey) {
        this.visitorMode = visitorMode;
        this.updateDataKey = updateDataKey;
    }

    /// endregion

    /// region Package methods

    UpdateDataKey getUpdateRequestKey() {
        return updateDataKey;
    }

    PrivacyModel setAuthorizedEventsName(Set<String> eventsName) {
        this.authorizedEventsName = new HashSet<>(eventsName);
        return this;
    }

    PrivacyModel setForbiddenEventsName(Set<String> eventsName) {
        this.forbiddenEventsName = new HashSet<>(eventsName);
        return this;
    }

    PrivacyModel setAuthorizedPropertyKeys(Map<String, Set<String>> authorizedPropertyKeys) {
        this.authorizedPropertyKeys = authorizedPropertyKeys;
        return this;
    }

    PrivacyModel setForbiddenPropertyKeys(Map<String, Set<String>> forbiddenPropertyKeys) {
        this.forbiddenPropertyKeys = forbiddenPropertyKeys;
        return this;
    }

    PrivacyModel setAuthorizedStorageFeatures(Set<PianoAnalytics.PrivacyStorageFeature> authorizedStorageFeatures) {
        this.authorizedStorageFeatures = new HashSet<>(authorizedStorageFeatures);
        return this;
    }

    PrivacyModel setForbiddenStorageFeatures(Set<PianoAnalytics.PrivacyStorageFeature> forbiddenStorageFeatures) {
        this.forbiddenStorageFeatures = new HashSet<>(forbiddenStorageFeatures);
        return this;
    }

    PrivacyModel setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    PrivacyModel setVisitorConsent(boolean visitorConsent) {
        this.visitorConsent = visitorConsent;
        return this;
    }

    PrivacyModel setCustomVisitorId(String customVisitorId) {
        this.customVisitorId = customVisitorId;
        return this;
    }

    /// endregion
}
