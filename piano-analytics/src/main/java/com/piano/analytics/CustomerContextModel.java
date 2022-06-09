package com.piano.analytics;

import java.util.Map;

final class CustomerContextModel {

    /// region enum

    enum UpdateTypeKey {
        ADD,
        DELETE
    }

    /// endregion

    /// region Constructors

    private final UpdateTypeKey updateTypeKey;
    private final Map<String, Object> properties;
    private final Boolean persistent;
    private final String[] events;

    CustomerContextModel(UpdateTypeKey updateTypeKey, Map<String, Object> properties) {
        this.updateTypeKey = updateTypeKey;
        this.properties = properties;
        this.persistent = false;
        this.events = null;
    }

    CustomerContextModel(UpdateTypeKey updateTypeKey, Map<String, Object> properties, Boolean persistent) {
        this.updateTypeKey = updateTypeKey;
        this.properties = properties;
        this.persistent = persistent;
        this.events = null;
    }

    CustomerContextModel(UpdateTypeKey updateTypeKey, Map<String, Object> properties, Boolean persistent, String[] events) {
        this.updateTypeKey = updateTypeKey;
        this.properties = properties;
        this.persistent = persistent;
        this.events = events;
    }

    /// endregion

    /// region Package methods

    Map<String, Object> getProperties() {
        return properties;
    }

    UpdateTypeKey getUpdateRequestKey() {
        return updateTypeKey;
    }

    Boolean getPersistent() {
        return persistent;
    }

    String[] getEvents() {
        return events;
    }

    /// endregion
}
