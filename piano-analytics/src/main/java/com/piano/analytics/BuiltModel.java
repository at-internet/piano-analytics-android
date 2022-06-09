package com.piano.analytics;

public final class BuiltModel {

    /// region PUBLIC SECTION

    public String getUri() {
        return uri;
    }

    public String getBody() {
        return body;
    }

    /// endregion

    /// region Constructors

    private final String uri;
    private final String body;
    private final boolean mustBeSaved;

    BuiltModel(String uri, String body, boolean mustBeSaved) {
        this.uri = uri;
        this.body = body;
        this.mustBeSaved = mustBeSaved;
    }

    /// endregion

    /// region Package methods

    boolean isMustBeSaved() {
        return mustBeSaved;
    }

    /// endregion
}
