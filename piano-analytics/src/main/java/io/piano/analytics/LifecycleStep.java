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
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

final class LifecycleStep implements WorkingQueue.IStep, Application.ActivityLifecycleCallbacks {

    private interface ILifecycleFunction {
        void computeMetric(SharedPreferences preferences, SharedPreferences.Editor editor, PrivacyStep ps) throws ParseException;
    }

    /// region Constructors

    private static LifecycleStep instance = null;

    static LifecycleStep getInstance(Context ctx, PrivacyStep privacyStep) {
        if (instance == null) {
            instance = new LifecycleStep(ctx, privacyStep);
        }
        return instance;
    }

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
    private final PrivacyStep privacyStep;

    private String versionCode;
    private String sessionId;
    private int sessionBackgroundDuration;
    private long timestampBackgroundStarted = -1;
    private final SharedPreferences sharedPreferences;
    private String savedActivityName;
    private int savedActivityTaskId;

    private LifecycleStep(Context ctx, PrivacyStep privacyStep) {
        this.privacyStep = privacyStep;

        try {
            this.versionCode = CastUtils.toString(ctx.getPackageManager().getPackageInfo(ctx.getApplicationContext().getPackageName(), 0).versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            PianoAnalytics.InternalLogger.severe("error on constructor LifecycleStep : " + e.toString());
            this.versionCode = "";
        }

        ((Application) ctx.getApplicationContext()).registerActivityLifecycleCallbacks(this);
        this.sharedPreferences = ctx.getSharedPreferences(PreferencesKeys.PREFERENCES, Context.MODE_PRIVATE);
        this.sharedPreferences.edit().remove(PreferencesKeys.INIT_LIFECYCLE_DONE).apply();
    }

    /// endregion

    /// region Constants

    private static final int DEFAULT_SESSION_BACKGROUND_DURATION = 2;

    /// endregion

    /// region ILifecycleFunction.computeMetric implementations

    private final ILifecycleFunction daysSinceFirstSession = (preferences, editor, ps) -> {
        String firstSessionDateStr = preferences.getString(PreferencesKeys.FIRST_SESSION_DATE, "");
        if (!PianoAnalyticsUtils.isEmptyString(firstSessionDateStr)) {
            Date firstSessionDate = this.dateFormatter.parse(firstSessionDateStr);
            if (firstSessionDate != null) {
                long timeSinceFirstSession = firstSessionDate.getTime();
                ps.storeData(editor, PianoAnalytics.PrivacyStorageFeature.LIFECYCLE, new Pair<>(PreferencesKeys.DAYS_SINCE_FIRST_SESSION, PianoAnalyticsUtils.convertMillisTo(TimeUnit.DAYS, Math.abs(PianoAnalyticsUtils.currentTimeMillis() - timeSinceFirstSession))));
            }
        }
    };
    private final ILifecycleFunction daysSinceUpdate = (preferences, editor, ps) -> {
        String firstSessionDateAfterUpdateStr = preferences.getString(PreferencesKeys.FIRST_SESSION_DATE_AFTER_UPDATE, "");
        if (!PianoAnalyticsUtils.isEmptyString(firstSessionDateAfterUpdateStr)) {
            Date firstSessionDateAfterUpdate = this.dateFormatter.parse(firstSessionDateAfterUpdateStr);
            if (firstSessionDateAfterUpdate != null) {
                long timeSinceFirstSessionAfterUpdate = firstSessionDateAfterUpdate.getTime();
                ps.storeData(editor, PianoAnalytics.PrivacyStorageFeature.LIFECYCLE, new Pair<>(PreferencesKeys.DAYS_SINCE_UPDATE, PianoAnalyticsUtils.convertMillisTo(TimeUnit.DAYS, Math.abs(PianoAnalyticsUtils.currentTimeMillis() - timeSinceFirstSessionAfterUpdate))));
            }
        }
    };
    private final ILifecycleFunction daysSinceLastSession = (preferences, editor, ps) -> {
        String lastSessionDateStr = preferences.getString(PreferencesKeys.LAST_SESSION_DATE, "");
        if (!PianoAnalyticsUtils.isEmptyString(lastSessionDateStr)) {
            Date lastSessionDate = this.dateFormatter.parse(lastSessionDateStr);
            if (lastSessionDate != null) {
                long timeSinceLastUse = lastSessionDate.getTime();
                ps.storeData(editor, PianoAnalytics.PrivacyStorageFeature.LIFECYCLE, new Pair<>(PreferencesKeys.DAYS_SINCE_LAST_SESSION, PianoAnalyticsUtils.convertMillisTo(TimeUnit.DAYS, Math.abs(PianoAnalyticsUtils.currentTimeMillis() - timeSinceLastUse))));
            }
        }
    };
    private final ILifecycleFunction[] computingMetrics = new ILifecycleFunction[]{daysSinceFirstSession, daysSinceLastSession, daysSinceUpdate};

    /// endregion

    /// region Private Methods

    @SuppressLint("CommitPrefEdits")
    private void init() {
        // Not first session
        if (!this.sharedPreferences.getBoolean(PreferencesKeys.FIRST_SESSION, true) || this.sharedPreferences.getBoolean(PreferencesKeys.FIRST_INIT_LIFECYCLE_DONE, false)) {
            newSessionInit();
        } else {
            firstSessionInit();
            privacyStep.storeData(this.sharedPreferences.edit(), PianoAnalytics.PrivacyStorageFeature.LIFECYCLE, new Pair<>(PreferencesKeys.FIRST_INIT_LIFECYCLE_DONE, true));
        }
        privacyStep.storeData(this.sharedPreferences.edit(), PianoAnalytics.PrivacyStorageFeature.LIFECYCLE, new Pair<>(PreferencesKeys.INIT_LIFECYCLE_DONE, true));
    }

    @SuppressLint("CommitPrefEdits")
    private void firstSessionInit() {
        Date now = new Date(PianoAnalyticsUtils.currentTimeMillis());
        this.sessionId = UUID.randomUUID().toString();
        privacyStep.storeData(this.sharedPreferences.edit(), PianoAnalytics.PrivacyStorageFeature.LIFECYCLE,
                new Pair<>(PreferencesKeys.FIRST_SESSION, true),
                new Pair<>(PreferencesKeys.FIRST_SESSION_AFTER_UPDATE, false),
                new Pair<>(PreferencesKeys.SESSION_COUNT, 1),
                new Pair<>(PreferencesKeys.SESSION_COUNT_SINCE_UPDATE, 1),
                new Pair<>(PreferencesKeys.DAYS_SINCE_FIRST_SESSION, 0),
                new Pair<>(PreferencesKeys.DAYS_SINCE_LAST_SESSION, 0),
                new Pair<>(PreferencesKeys.FIRST_SESSION_DATE, this.dateFormatter.format(now)),
                new Pair<>(PreferencesKeys.LAST_SESSION_DATE, this.dateFormatter.format(now)),
                new Pair<>(PreferencesKeys.VERSION_CODE_KEY, this.versionCode));
    }

    private void newSessionInit() {
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        try {
            for (ILifecycleFunction f : this.computingMetrics) {
                f.computeMetric(this.sharedPreferences, editor, privacyStep);
            }

            Date now = new Date(PianoAnalyticsUtils.currentTimeMillis());
            privacyStep.storeData(editor, PianoAnalytics.PrivacyStorageFeature.LIFECYCLE,
                    new Pair<>(PreferencesKeys.FIRST_SESSION, false),
                    new Pair<>(PreferencesKeys.FIRST_SESSION_AFTER_UPDATE, false),
                    new Pair<>(PreferencesKeys.LAST_SESSION_DATE, this.dateFormatter.format(now)),
                    /// sc
                    new Pair<>(PreferencesKeys.SESSION_COUNT, this.sharedPreferences.getInt(PreferencesKeys.SESSION_COUNT, 0) + 1),
                    /// Calcul scsu
                    new Pair<>(PreferencesKeys.SESSION_COUNT_SINCE_UPDATE, this.sharedPreferences.getInt(PreferencesKeys.SESSION_COUNT_SINCE_UPDATE, 0) + 1));

            /// Application version changed
            String savedVersionCode = this.sharedPreferences.getString(PreferencesKeys.VERSION_CODE_KEY, null);
            /// Update detected
            if (!versionCode.equals(savedVersionCode)) {
                privacyStep.storeData(editor, PianoAnalytics.PrivacyStorageFeature.LIFECYCLE,
                        new Pair<>(PreferencesKeys.FIRST_SESSION_DATE_AFTER_UPDATE, this.dateFormatter.format(now)),
                        new Pair<>(PreferencesKeys.VERSION_CODE_KEY, versionCode),
                        new Pair<>(PreferencesKeys.SESSION_COUNT_SINCE_UPDATE, 1),
                        new Pair<>(PreferencesKeys.DAYS_SINCE_UPDATE, 0),
                        new Pair<>(PreferencesKeys.FIRST_SESSION_AFTER_UPDATE, true));
            }
        } catch (ParseException e) {
            PianoAnalytics.InternalLogger.severe("error on LifecycleStep.newSessionInit : " + e.toString());
        }
        sessionId = UUID.randomUUID().toString();
    }

    private Map<String, Object> getProperties() {
        Map<String, Object> m = new HashMap<>();
        if (!this.sharedPreferences.getBoolean(PreferencesKeys.INIT_LIFECYCLE_DONE, false)) {
            init();
        }
        m.put("app_fs", this.sharedPreferences.getBoolean(PreferencesKeys.FIRST_SESSION, false));
        m.put("app_fsau", this.sharedPreferences.getBoolean(PreferencesKeys.FIRST_SESSION_AFTER_UPDATE, false));
        m.put("app_sc", this.sharedPreferences.getInt(PreferencesKeys.SESSION_COUNT, 0));
        m.put("app_dsls", this.sharedPreferences.getInt(PreferencesKeys.DAYS_SINCE_LAST_SESSION, 0));
        m.put("app_dsfs", this.sharedPreferences.getInt(PreferencesKeys.DAYS_SINCE_FIRST_SESSION, 0));
        m.put("app_fsd", Integer.parseInt(this.sharedPreferences.getString(PreferencesKeys.FIRST_SESSION_DATE, this.dateFormatter.format(new Date(PianoAnalyticsUtils.currentTimeMillis())))));
        m.put("app_sessionid", sessionId);

        if (!PianoAnalyticsUtils.isEmptyString(this.sharedPreferences.getString(PreferencesKeys.FIRST_SESSION_DATE_AFTER_UPDATE, ""))) {
            m.put("app_scsu", this.sharedPreferences.getInt(PreferencesKeys.SESSION_COUNT_SINCE_UPDATE, 0));
            m.put("app_fsdau", Integer.parseInt(this.sharedPreferences.getString(PreferencesKeys.FIRST_SESSION_DATE_AFTER_UPDATE, this.dateFormatter.format(new Date(PianoAnalyticsUtils.currentTimeMillis())))));
            m.put("app_dsu", this.sharedPreferences.getInt(PreferencesKeys.DAYS_SINCE_UPDATE, 0));
        }

        return m;
    }

    /// endregion

    /// region Application.ActivityLifecycleCallbacks implementation

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        String activityCanonicalName = activity.getClass().getCanonicalName();
        if (activityCanonicalName == null || !activityCanonicalName.equals(this.savedActivityName) || activity.getTaskId() == this.savedActivityTaskId) {
            this.timestampBackgroundStarted = -1;
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (this.timestampBackgroundStarted > -1 &&
                PianoAnalyticsUtils.convertMillisTo(TimeUnit.SECONDS, Math.abs(PianoAnalyticsUtils.currentTimeMillis() - this.timestampBackgroundStarted)) >= (Math.max(this.sessionBackgroundDuration, DEFAULT_SESSION_BACKGROUND_DURATION))) {
            newSessionInit();
            this.timestampBackgroundStarted = -1;
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        /// Required by interface
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        savedActivityName = activity.getClass().getCanonicalName();
        savedActivityTaskId = activity.getTaskId();
        timestampBackgroundStarted = PianoAnalyticsUtils.currentTimeMillis();
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        /// Required by interface
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        /// Required by interface
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        /// Required by interface
    }

    /// endregion

    /// region WorkingQueue.IStep implementation

    @Override
    public void processSetConfig(Model m) {
        /// REQUIREMENTS
        Configuration configuration = m.getConfiguration();

        String sessionBackgroundDurationConfig = configuration.get(Configuration.ConfigurationKey.SESSION_BACKGROUND_DURATION);
        this.sessionBackgroundDuration = CastUtils.toInt(sessionBackgroundDurationConfig);
    }

    @Override
    public void processGetModel(Context ctx, Model m) {
        m.addContextProperties(this.getProperties());
    }

    @Override
    public boolean processTrackEvents(Context ctx, Model m, PianoAnalytics.OnWorkListener l) {
        m.addContextProperties(this.getProperties());
        return true;
    }

    /// endregion
}
