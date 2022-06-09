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
package com.piano.analytics;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class CrashHandlingStep implements WorkingQueue.IStep, Thread.UncaughtExceptionHandler {
    /// region Constructors

    private static CrashHandlingStep instance = null;

    static CrashHandlingStep getInstance(Context ctx, PrivacyStep privacyStep) {
        if (instance == null) {
            instance = new CrashHandlingStep(ctx, privacyStep);
        }
        return instance;
    }

    private final SharedPreferences sharedPreferences;
    private final Thread.UncaughtExceptionHandler defaultHandler;
    private final String packageName;
    private final PrivacyStep privacyStep;
    private boolean isCrashHandlingRegistered;
    private String savedPageProperty = "";

    private CrashHandlingStep(Context ctx, PrivacyStep privacyStep) {
        this.packageName = ctx.getPackageName();
        this.sharedPreferences = ctx.getSharedPreferences(PreferencesKeys.PREFERENCES, Context.MODE_PRIVATE);
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        this.privacyStep = privacyStep;
    }

    /// endregion

    /// region Constants

    static final String APP_CRASH_PROPERTIES_FORMAT = "app_crash%s";
    private static final String PAGE_PROPERTY_KEY = "page";

    /// endregion

    /// region Package methods

    String getSavedPageProperty(){
        return this.savedPageProperty;
    }

    void setSavedPageProperty(String savedPageProperty) {
        this.savedPageProperty = savedPageProperty;
    }

    /// endregion

    /// region Private methods

    private Map<String, Object> getProperties(Context ctx) {
        Map<String, Object> result = new HashMap<>();
        SharedPreferences preferences = ctx.getSharedPreferences(PreferencesKeys.PREFERENCES, Context.MODE_PRIVATE);

        boolean crashed = preferences.getBoolean(PreferencesKeys.CRASHED, false);
        if (!crashed) {
            return result;
        }
        preferences.edit().remove(PreferencesKeys.CRASHED).apply();

        String crashInfo = preferences.getString(PreferencesKeys.CRASH_INFO, null);
        Map<String, Object> objectMap = MapUtils.fromJSONString(crashInfo);
        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            String key = entry.getKey();
            result.put(key, entry.getValue());
        }

        return result;
    }

    private String getClassNameException(Throwable t) {
        for (StackTraceElement element : t.getStackTrace()) {
            if (element.getClassName().contains(this.packageName)) {
                return element.getClassName();
            }
        }
        return "";
    }

    /// endregion

    /// region Thread.UncaughtExceptionHandler implementation

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        String className;
        String exceptionName;

        Throwable cause = e.getCause();
        if (cause != null) {
            className = getClassNameException(cause);
            exceptionName = cause.getClass().getName();
        } else {
            className = getClassNameException(e);
            exceptionName = e.getClass().getName();
        }

        Map<String, String> m = new HashMap<>();
        m.put(String.format(APP_CRASH_PROPERTIES_FORMAT, "_screen"), this.savedPageProperty);
        m.put(String.format(APP_CRASH_PROPERTIES_FORMAT, "_class"), className);
        m.put(String.format(APP_CRASH_PROPERTIES_FORMAT, ""), exceptionName);
        JSONObject jObject = new JSONObject(m);
        this.privacyStep.storeData(this.sharedPreferences.edit(), PianoAnalytics.PrivacyStorageFeature.CRASH,
                new Pair<>(PreferencesKeys.CRASHED, true),
                new Pair<>(PreferencesKeys.CRASH_INFO, jObject.toString()));

        if (this.defaultHandler != null) {
            this.defaultHandler.uncaughtException(t, e);
        }
    }

    /// endregion

    /// region WorkingQueue.IStep implementation

    @Override
    public void processSetConfig(Model m) {
        /// REQUIREMENTS
        Configuration configuration = m.getConfiguration();

        boolean crashDetectionEnabled = CastUtils.toBool(configuration.get(Configuration.ConfigurationKey.CRASH_DETECTION));
        boolean shouldBeRegistered = crashDetectionEnabled && !this.isCrashHandlingRegistered;
        boolean shouldNotBeRegistered = !crashDetectionEnabled && this.isCrashHandlingRegistered;

        if (shouldBeRegistered) {
            this.isCrashHandlingRegistered = true;
            Thread.setDefaultUncaughtExceptionHandler(this);
        } else if (shouldNotBeRegistered) {
            this.isCrashHandlingRegistered = false;
            Thread.setDefaultUncaughtExceptionHandler(this.defaultHandler);
        }
    }

    @Override
    public boolean processTrackEvents(Context ctx, Model m, PianoAnalytics.OnWorkListener l) {
        /// REQUIREMENTS
        List<Event> events = m.getEvents();
        Event lastEvent = events.get(events.size() - 1);

        if (lastEvent != null) {
            Object pageProperty = lastEvent.getData().get(PAGE_PROPERTY_KEY);
            if (pageProperty != null) {
                this.savedPageProperty = CastUtils.toString(pageProperty);
            }
        }

        m.addContextProperties(getProperties(ctx));
        return true;
    }

    /// endregion
}
