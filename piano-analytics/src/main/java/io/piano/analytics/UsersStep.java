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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

final class UsersStep implements WorkingQueue.IStep {

    /// region Constructors

    static UsersStep instance = null;

    static UsersStep getInstance(Context ctx, PrivacyStep privacyStep, Configuration configuration) {
        if (instance == null) {
            instance = new UsersStep(ctx, privacyStep, configuration);
        }
        return instance;
    }

    private final PrivacyStep privacyStep;
    private final SharedPreferences sharedPreferences;
    private boolean userRecognition = false;

    private User user = null;
    private int userLifetimeStorage;
    private boolean userStored;
    private boolean userStoredFetchedOnCreate;

    private UsersStep(Context ctx, PrivacyStep privacyStep, Configuration configuration) {
        this.privacyStep = privacyStep;
        this.sharedPreferences = ctx.getSharedPreferences(PreferencesKeys.PREFERENCES, Context.MODE_PRIVATE);

        String userJSONString = privacyStep.getData(this.sharedPreferences, PianoAnalytics.PrivacyStorageFeature.USER, PreferencesKeys.USER, "", "");
        if (!PianoAnalyticsUtils.isEmptyString(userJSONString)) {
            try {
                this.user = new User(new JSONObject(userJSONString));
            } catch (JSONException ignore) {
                // pass
            }
        }

        if (this.user != null) {
            userStoredFetchedOnCreate = true;
            userStored = true;
            userRecognition = true;
            long userStoredTimestamp = this.sharedPreferences.getLong(PreferencesKeys.USER_GENERATION_TIMESTAMP, -1L);
            if (userStoredTimestamp == -1) {
                privacyStep.storeData(this.sharedPreferences.edit(), PianoAnalytics.PrivacyStorageFeature.USER, new Pair<>(PreferencesKeys.USER_GENERATION_TIMESTAMP, PianoAnalyticsUtils.currentTimeMillis()));
            }
        }

        try {
            userLifetimeStorage = Integer.parseInt(configuration.get(Configuration.ConfigurationKey.STORAGE_LIFETIME_USER));
        } catch (NumberFormatException ignored) {
            // pass
        }

        if (PianoAnalyticsUtils.isEmptyUnsignedInteger(userLifetimeStorage)) {
            userLifetimeStorage = Configuration.DEFAULT_STORAGE_LIFETIME_USER;
        }
    }

    /// endregion

    /// region Private methods

    private Map<String, Object> getProperties() {
        Map<String, Object> m = new HashMap<>();
        User user = getUser();
        if (user == null) {
            return m;
        }

        String id = user.getId();
        if (id != null) {
            m.put("user_id", id);
            m.put("user_recognition", this.userRecognition);
        }

        String category = user.getCategory();
        if (category != null) {
            m.put("user_category", category);
        }
        return m;
    }

    /// endregion

    /// region Package methods

    User getUser() {
        if (!privacyStep.getVisitorModeAuthorizedStorageFeature(privacyStep.getVisitorMode()).contains(PianoAnalytics.PrivacyStorageFeature.USER)
                || privacyStep.getVisitorModeForbiddenStorageFeature(privacyStep.getVisitorMode()).contains(PianoAnalytics.PrivacyStorageFeature.USER)) {
            this.privacyStep.storeData(this.sharedPreferences.edit(), PianoAnalytics.PrivacyStorageFeature.USER, new Pair<>(PreferencesKeys.USER, null));
            this.userRecognition = false;
            if (userStoredFetchedOnCreate) {
                userStoredFetchedOnCreate = false;
                this.user = null;
            }
            return this.user;
        }
        long now = PianoAnalyticsUtils.currentTimeMillis();
        long userStoredTimestamp = this.sharedPreferences.getLong(PreferencesKeys.USER_GENERATION_TIMESTAMP, -1L);
        if (PianoAnalyticsUtils.isEmptyUnsignedLong(userStoredTimestamp)) {
            this.privacyStep.storeData(this.sharedPreferences.edit(), PianoAnalytics.PrivacyStorageFeature.USER, new Pair<>(PreferencesKeys.USER_GENERATION_TIMESTAMP, now));
            userStoredTimestamp = now;
        }

        if (now > userStoredTimestamp + (userLifetimeStorage * 86_400_000L)) {
            this.privacyStep.storeData(this.sharedPreferences.edit(), PianoAnalytics.PrivacyStorageFeature.USER, new Pair<>(PreferencesKeys.USER, null));
            this.userRecognition = false;
            return null;
        }

        return this.user;
    }

    boolean getUserRecognition() {
        return this.userRecognition;
    }

    /// endregion

    /// region WorkingQueue.IStep implementation

    @SuppressLint("CommitPrefEdits")
    @Override
    public void processUpdateContext(Model m) {
        /// REQUIREMENTS
        UserModel userModel = m.getUserModel();
        if (userModel == null) {
            return;
        }

        switch (userModel.getUpdateRequestKey()) {
            case SET:
                this.user = userModel.getUser();
                this.userRecognition = false;

                this.userStored = userModel.getEnableStorage();
                this.userStoredFetchedOnCreate = false;

                if (this.userStored) {
                    long now = PianoAnalyticsUtils.currentTimeMillis();
                    this.privacyStep.storeData(this.sharedPreferences.edit(), PianoAnalytics.PrivacyStorageFeature.USER,
                            new Pair<>(PreferencesKeys.USER, this.user.toJSONString()),
                            new Pair<>(PreferencesKeys.USER_GENERATION_TIMESTAMP, now));
                }
                break;
            case DELETE:
                this.user = null;
                this.userRecognition = false;

                this.privacyStep.storeData(this.sharedPreferences.edit(), PianoAnalytics.PrivacyStorageFeature.USER,
                        new Pair<>(PreferencesKeys.USER, null),
                        new Pair<>(PreferencesKeys.USER_GENERATION_TIMESTAMP, null));
                break;
            default:
                /// nothing
                break;
        }
    }

    @Override
    public void processGetModel(Context ctx, Model m) {
        m.setUser(getUser());
    }

    @Override
    public boolean processTrackEvents(Context ctx, Model m, PianoAnalytics.OnWorkListener l) {
        /// REQUIREMENTS
        m.addContextProperties(getProperties())
                .setUser(getUser());

        if (!this.userStored) {
            this.user = null;
            this.userRecognition = false;
            this.privacyStep.storeData(this.sharedPreferences.edit(), PianoAnalytics.PrivacyStorageFeature.USER,
                    new Pair<>(PreferencesKeys.USER, null),
                    new Pair<>(PreferencesKeys.USER_GENERATION_TIMESTAMP, 0L)
            );
        }

        return true;
    }

    /// endregion
}
