package com.piano.analytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public final class User {

    /// region PUBLIC SECTION

    public String getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    /// endregion

    /// region Constants

    static final String ID_FIELD = "id";
    static final String CATEGORY_FIELD = "category";

    /// endregion

    private final String id;

    private String category;

    User(String id) {
        this.id = id;
    }

    User(JSONObject obj) throws JSONException {
        this.id = obj.getString(ID_FIELD);
        if (obj.has(CATEGORY_FIELD)) {
            this.category = obj.getString(CATEGORY_FIELD);
        }
    }

    User setCategory(String category) {
        this.category = category;
        return this;
    }

    Map<String, String> toMap() {
        Map<String, String> m = new HashMap<>();
        m.put(ID_FIELD, this.id);
        if (this.category != null) {
            m.put(CATEGORY_FIELD, this.category);
        }
        return m;
    }

    String toJSONString() {
        return new JSONObject(this.toMap()).toString();
    }
}
