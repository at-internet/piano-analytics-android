package io.piano.analytics;

final class UserModel {

    /// region enum

    enum UpdateTypeKey {
        SET,
        DELETE
    }

    /// endregion

    /// region Constructors

    private final UpdateTypeKey updateTypeKey;
    private User user;
    private final boolean enableStorage;

    UserModel(UpdateTypeKey updateTypeKey, boolean enableStorage) {
        this.updateTypeKey = updateTypeKey;
        this.enableStorage = enableStorage;
    }

    /// endregion

    /// region Package methods

    UpdateTypeKey getUpdateRequestKey() {
        return updateTypeKey;
    }

    UserModel setUser(User user) {
        this.user = user;
        return this;
    }

    User getUser() {
        return user;
    }

    boolean getEnableStorage() {
        return enableStorage;
    }

    /// endregion
}
