package com.piano.analytics;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/// region PUBLIC SECTION

public final class Event {

    private String name;
    private final Map<String, Object> data;

    public Event(@NonNull String name, @NonNull Map<String, Object> data) {
        this.name = name;
        this.data = MapUtils.toFlatten(data);
    }

    public String getName() {
        return this.name;
    }

    public Map<String, Object> getData() {
        return this.data;
    }

    public void setName(String name) {
        this.name = name;
    }

    Map<String, Object> toMap() {
        Map<String, Object> m = new HashMap<>();
        m.put("name", this.name);
        m.put("data", this.data);
        return m;
    }
}

/// endregion