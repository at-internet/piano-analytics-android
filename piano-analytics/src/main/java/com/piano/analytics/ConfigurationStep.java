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
import android.content.pm.ApplicationInfo;
import android.util.Pair;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

final class ConfigurationStep implements WorkingQueue.IStep {

    /// region Constructors

    private static ConfigurationStep instance = null;

    static ConfigurationStep getInstance(Context ctx, String configFileLocation) {
        if (instance == null) {
            instance = new ConfigurationStep(ctx, configFileLocation);
        }
        return instance;
    }

    private Configuration configuration;

    private ConfigurationStep(Context ctx, String configFileLocation) {
        this.configuration = new Configuration.Builder()
                .withCollectDomain(Configuration.DEFAULT_COLLECT_DOMAIN)
                .withPath(Configuration.DEFAULT_PATH)
                .withPrivacyDefaultMode(Configuration.PRIVACY_DEFAULT_MODE)
                .withSite(Configuration.DEFAULT_SITE)
                .withOfflineStorageMode(Configuration.DEFAULT_OFFLINE_STORAGE_MODE)
                .enableIgnoreLimitedAdTracking(Configuration.DEFAULT_IGNORE_LIMITED_ADVERTISING_TRACKING)
                .enableCrashDetection(Configuration.DEFAULT_CRASH_DETECTION)
                .withVisitorIDType(Configuration.DEFAULT_VISITOR_ID_TYPE)
                .withVisitorStorageMode(Configuration.DEFAULT_VISITOR_STORAGE_MODE)
                .withStorageLifetimePrivacy(Configuration.DEFAULT_STORAGE_LIFETIME_PRIVACY)
                .withStorageLifetimeVisitor(Configuration.DEFAULT_STORAGE_LIFETIME_VISITOR)
                .withStorageLifetimeUser(Configuration.DEFAULT_STORAGE_LIFETIME_USER)
                .enableSendEventWhenOptOut(Configuration.DEFAULT_SEND_WHEN_OPT_OUT)
                .withEncryptionMode(Configuration.DEFAULT_ENCRYPTION_MODE)
                .withSessionBackgroundDuration(Configuration.DEFAULT_SESSION_BACKGROUND_DURATION)
                .withCustomUserAgent(getDefaultUserAgent(ctx))
                .build();

        if (!PianoAnalyticsUtils.isEmptyString(configFileLocation)) {
            loadConfigurationFromLocalFile(ctx, configFileLocation);
        }
    }

    /// endregion

    /// region Constants

    private static final String USER_AGENT_SYSTEM_PROPERTY = "http.agent";
    private static final String USER_AGENT_FORMAT = "%s %s/%s";

    /// endregion

    /// region Package methods

    Configuration getConfiguration() {
        return configuration;
    }

    ConfigurationStep setConfiguration(Configuration configuration) {
        this.configuration = new Configuration(configuration);
        return this;
    }

    /// endregion

    /// region Private methods

    private String getDefaultUserAgent(Context appContext) {
        ApplicationInfo applicationInfo = appContext.getApplicationInfo();
        Pair<String, String> appProps = PianoAnalyticsUtils.getApplicationProperties(appContext);
        String appName;
        if (applicationInfo.labelRes == 0) {
            appName = applicationInfo.nonLocalizedLabel != null ? applicationInfo.nonLocalizedLabel.toString() : null;
        } else {
            appName = appContext.getString(applicationInfo.labelRes);
        }

        return String.format(USER_AGENT_FORMAT, System.getProperty(USER_AGENT_SYSTEM_PROPERTY), appName, appProps != null ? appProps.second : null);
    }

    void loadConfigurationFromLocalFile(Context ctx, String configFileLocation) {
        Map<String, Object> objectMap;
        try {
            objectMap = MapUtils.fromJSONString(PianoAnalyticsUtils.getStringFromInputStream(ctx.getAssets().open(configFileLocation)));
        } catch (IOException e) {
            PianoAnalytics.InternalLogger.severe("error on ConfigStep.loadConfigurationFromLocalFile: " + e.toString());
            objectMap = new HashMap<>();
        }

        for (Map.Entry<String, Object> entry : objectMap.entrySet()) {
            String key = entry.getKey();
            Configuration.ConfigurationKey enumKey = Configuration.ConfigurationKey.fromString(key);
            if (enumKey != null) {
                this.configuration.put(enumKey.stringValue(), entry.getValue());
            }
        }
    }

    private Configuration merge(Configuration customConf) {
        Configuration merged = new Configuration(this.configuration);
        merged.putAll(customConf);
        return merged;
    }

    /// endregion

    /// region WorkingQueue.IStep implementation

    @Override
    public void processGetModel(Context ctx, Model m) {
        m.Config(new Configuration(this.configuration));
    }

    @Override
    public void processUpdateContext(Model m) {
        m.Config(new Configuration(this.configuration));
    }

    @Override
    public void processSetConfig(Model m) {
        /// REQUIREMENTS
        Configuration conf = m.getConfiguration();

        this.configuration.putAll(conf);
        m.Config(new Configuration(this.configuration));
    }

    @Override
    public void processGetConfig(Model m) {
        /// REQUIREMENTS

        m.Config(new Configuration(this.configuration));
    }

    @Override
    public boolean processSendOfflineStorage(Model m, PianoAnalytics.OnWorkListener l) {
        /// REQUIREMENTS
        Configuration customConf = m.getConfiguration();

        m.Config(merge(customConf));
        return true;
    }

    @Override
    public boolean processTrackEvents(Context ctx, Model m, PianoAnalytics.OnWorkListener l) {
        /// REQUIREMENTS
        Configuration customConf = m.getConfiguration();

        m.Config(merge(customConf));
        return true;
    }

    /// endregion
}
