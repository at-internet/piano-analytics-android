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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Pair;

import java.util.UUID;

final class VisitorIDStep implements WorkingQueue.IStep {

    private interface IVisitorIDStepFunction {
        Pair<Boolean, Pair<Boolean, String>> getInfo(Configuration c, Context ctx, SharedPreferences prefs, PrivacyStep ps);
    }

    /// region Constructors

    private static VisitorIDStep instance = null;

    static VisitorIDStep getInstance(PrivacyStep privacyStep) {
        if (instance == null) {
            instance = new VisitorIDStep(privacyStep);
        }
        return instance;
    }

    private final PrivacyStep privacyStep;

    private VisitorIDStep(PrivacyStep privacyStep) {
        this.privacyStep = privacyStep;
    }

    /// endregion

    /// region Constants

    static final String VISITOR_ID_TYPE_PROPERTY = "visitor_id_type";
    private static final String OPT_OUT = "opt-out";
    private static final String GOOGLE_ADS_SERVICES_CLASS = "com.google.android.gms.ads.identifier.AdvertisingIdClient";
    private static final String HUAWEI_ADS_SERVICES_CLASS = "com.huawei.hms.ads.identifier.AdvertisingIdClient";

    /// endregion

    /// region IVisitorIDStepFunction.getInfo

    private final IVisitorIDStepFunction uuid = (Configuration configuration, Context ctx, SharedPreferences prefs, PrivacyStep ps) -> {
        long now = PianoAnalyticsUtils.currentTimeMillis();
        String uuid = ps.getData(prefs, PianoAnalytics.PrivacyStorageFeature.VISITOR, PreferencesKeys.VISITOR_UUID, null, "");
        String uuidDurationConfig = configuration.get(Configuration.ConfigurationKey.STORAGE_LIFETIME_VISITOR);
        Configuration.VisitorStorageMode uuidExpirationModeConfig = Configuration.VisitorStorageMode.fromString(configuration.get(Configuration.ConfigurationKey.VISITOR_STORAGE_MODE));
        SharedPreferences.Editor editor = prefs.edit();

        int uuidDuration = CastUtils.toInt(uuidDurationConfig);

        // if uuid empty, try get old uuid from AT
        if (uuid == null) {
            uuid = ps.getData(prefs, PianoAnalytics.PrivacyStorageFeature.VISITOR, PreferencesKeys.VISITOR_UUID_AT, null, "");
            ps.storeData(prefs.edit(), PianoAnalytics.PrivacyStorageFeature.VISITOR,
                    new Pair<>(PreferencesKeys.VISITOR_UUID, uuid),
                    new Pair<>(PreferencesKeys.VISITOR_UUID_AT, null));
        }

        if (uuid != null) {
            /// get uuid generation timestamp
            long uuidGenerationTimestampDefault = -1;
            long uuidGenerationTimestamp = ps.getData(prefs, PianoAnalytics.PrivacyStorageFeature.VISITOR, PreferencesKeys.VISITOR_UUID_GENERATION_TIMESTAMP, uuidGenerationTimestampDefault, uuidGenerationTimestampDefault);
            if (uuidGenerationTimestamp == -1) {
                ps.storeData(editor, PianoAnalytics.PrivacyStorageFeature.VISITOR,
                        new Pair<>(PreferencesKeys.VISITOR_UUID_GENERATION_TIMESTAMP, now));
                uuidGenerationTimestamp = now;
            }

            /// uuid expired ?
            long daysSinceGeneration = (now - uuidGenerationTimestamp) / (1000 * 60 * 60 * 24);
            if (daysSinceGeneration >= uuidDuration) {
                uuid = null;
            }
        }

        /// No or expired id
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
            ps.storeData(editor, PianoAnalytics.PrivacyStorageFeature.VISITOR,
                    new Pair<>(PreferencesKeys.VISITOR_UUID, uuid),
                    new Pair<>(PreferencesKeys.VISITOR_UUID_GENERATION_TIMESTAMP, now));
            return new Pair<>(true, new Pair<>(false, uuid));
        }

        /// expiration relative
        if (uuidExpirationModeConfig == Configuration.VisitorStorageMode.RELATIVE) {
            ps.storeData(editor, PianoAnalytics.PrivacyStorageFeature.VISITOR, new Pair<>(PreferencesKeys.VISITOR_UUID_GENERATION_TIMESTAMP, now));
        }

        return new Pair<>(true, new Pair<>(false, uuid));
    };

    @SuppressLint("HardwareIds")
    private final IVisitorIDStepFunction androidID = (Configuration c, Context ctx, SharedPreferences prefs, PrivacyStep ps) -> new Pair<>(true, new Pair<>(false, Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID)));
    private final IVisitorIDStepFunction googleAdvertisingID = (Configuration c, Context ctx, SharedPreferences prefs, PrivacyStep ps) -> {
        try {
            /// Check if class is available to prevent stucking process
            if (PianoAnalyticsUtils.isClassUnavailable(GOOGLE_ADS_SERVICES_CLASS)) {
                throw new IllegalAccessException("Google Ads Services Class not available");
            }

            com.google.android.gms.ads.identifier.AdvertisingIdClient.Info gmsAdInfo = com.google.android.gms.ads.identifier.AdvertisingIdClient.getAdvertisingIdInfo(ctx);
            if (gmsAdInfo == null) {
                throw new IllegalAccessException("Google Advertising ID Info not available");
            }

            return new Pair<>(true, new Pair<>(gmsAdInfo.isLimitAdTrackingEnabled(), gmsAdInfo.getId()));
        } catch (Exception e) {
            PianoAnalytics.InternalLogger.severe("VisitorIDStep.googleAdvertisingID : " + e.toString());
        }
        return new Pair<>(false, new Pair<>(false, null));
    };
    private final IVisitorIDStepFunction huaweiOpenAdvertisingID = (Configuration c, Context ctx, SharedPreferences prefs, PrivacyStep ps) -> {
        try {
            /// Check if class is available to prevent stucking process
            if (PianoAnalyticsUtils.isClassUnavailable(HUAWEI_ADS_SERVICES_CLASS)) {
                throw new IllegalAccessException("Huawei Ads Services Class not available");
            }

            com.huawei.hms.ads.identifier.AdvertisingIdClient.Info hmsAdInfo = com.huawei.hms.ads.identifier.AdvertisingIdClient.getAdvertisingIdInfo(ctx);

            return new Pair<>(true, new Pair<>(hmsAdInfo.isLimitAdTrackingEnabled(), hmsAdInfo.getId()));
        } catch (Exception e) {
            PianoAnalytics.InternalLogger.severe("VisitorIDStep.huaweiOpenAdvertisingID : " + e.toString());
        }
        return new Pair<>(false, new Pair<>(false, null));
    };
    private final IVisitorIDStepFunction customId = (Configuration c, Context ctx, SharedPreferences prefs, PrivacyStep ps) -> new Pair<>(true, new Pair<>(false, c.get(Configuration.ConfigurationKey.VISITOR_ID)));

    /// endregion

    /// region Private methods

    private String getVisitorID(Context ctx, Configuration configuration, Configuration.VisitorIDType visitorIDType) {
        SharedPreferences preferences = ctx.getSharedPreferences(PreferencesKeys.PREFERENCES, Context.MODE_PRIVATE);

        IVisitorIDStepFunction[] iVisitorIDStepFunctions;
        switch (visitorIDType) {
            case ANDROID_ID:
                iVisitorIDStepFunctions = new IVisitorIDStepFunction[]{this.androidID};
                break;
            case ADVERTISING_ID:
                iVisitorIDStepFunctions = new IVisitorIDStepFunction[]{this.googleAdvertisingID, this.huaweiOpenAdvertisingID};
                break;
            case GOOGLE_ADVERTISING_ID:
                /// Google Advertising ID Only
                iVisitorIDStepFunctions = new IVisitorIDStepFunction[]{this.googleAdvertisingID};
                break;
            case HUAWEI_OPEN_ADVERTISING_ID:
                /// Huawei Open Advertising ID Only
                iVisitorIDStepFunctions = new IVisitorIDStepFunction[]{this.huaweiOpenAdvertisingID};
                break;
            case CUSTOM:
                iVisitorIDStepFunctions = new IVisitorIDStepFunction[]{this.customId};
                break;
            default:
                iVisitorIDStepFunctions = new IVisitorIDStepFunction[]{this.uuid};
                break;
        }

        boolean ignoreLimitedAdvertisingTracking = CastUtils.toBool(configuration.get(Configuration.ConfigurationKey.IGNORE_LIMITED_AD_TRACKING));
        String visitorIdValue = null;

        for (IVisitorIDStepFunction function : iVisitorIDStepFunctions) {
            Pair<Boolean, Pair<Boolean, String>> visitorIDInfo = function.getInfo(configuration, ctx, preferences, this.privacyStep);
            boolean retrieveSuccessfully = visitorIDInfo.first;
            boolean trackingLimitedByUser = visitorIDInfo.second.first;
            if (retrieveSuccessfully) {
                if (trackingLimitedByUser) {
                    if (ignoreLimitedAdvertisingTracking) {
                        visitorIdValue = uuid.getInfo(configuration, ctx, preferences, this.privacyStep).second.second;
                    } else {
                        visitorIdValue = OPT_OUT;
                    }
                } else {
                    visitorIdValue = visitorIDInfo.second.second;
                }
                break;
            }
        }
        return visitorIdValue;
    }

    /// endregion

    /// region WorkingQueue.IStep implementation

    @Override
    public void processGetModel(Context ctx, Model m) {
        /// REQUIREMENTS
        Configuration configuration = m.getConfiguration();

        Configuration.VisitorIDType visitorIDType = Configuration.VisitorIDType.fromString(configuration.get(Configuration.ConfigurationKey.VISITOR_ID_TYPE));
        configuration.set(Configuration.ConfigurationKey.VISITOR_ID, getVisitorID(ctx, configuration, visitorIDType));
    }

    @Override
    public boolean processTrackEvents(Context ctx, Model m, PianoAnalytics.OnWorkListener l) {
        /// REQUIREMENTS
        Configuration configuration = m.getConfiguration();

        Configuration.VisitorIDType visitorIDType = Configuration.VisitorIDType.fromString(configuration.get(Configuration.ConfigurationKey.VISITOR_ID_TYPE));

        String customVisitorID = configuration.get(Configuration.ConfigurationKey.VISITOR_ID);
        String customVisitorIDType = configuration.get(Configuration.ConfigurationKey.VISITOR_ID_TYPE);
        if (PianoAnalyticsUtils.isEmptyString(customVisitorID)) {
            configuration.set(Configuration.ConfigurationKey.VISITOR_ID, getVisitorID(ctx, configuration, visitorIDType));
        } else {
            privacyStep.storeData(ctx.getSharedPreferences(PreferencesKeys.PREFERENCES, Context.MODE_PRIVATE).edit(), PianoAnalytics.PrivacyStorageFeature.VISITOR, new Pair<>(PreferencesKeys.VISITOR_UUID, customVisitorID));
        }

        if (!PianoAnalyticsUtils.isEmptyString(customVisitorIDType)) {
            visitorIDType = Configuration.VisitorIDType.fromString(customVisitorIDType);
        }

        m.addContextProperty(VISITOR_ID_TYPE_PROPERTY, visitorIDType.stringValue());
        return true;
    }

    /// endregion
}
