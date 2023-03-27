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

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.Display;
import android.view.WindowManager;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

final class InternalContextPropertiesStep implements WorkingQueue.IStep {

    private interface IInternalContextPropertiesFunction {
        Map<String, Object> getProperties(Context ctx);
    }

    /// region Constructors

    private static InternalContextPropertiesStep instance = null;

    static InternalContextPropertiesStep getInstance() {
        if (instance == null) {
            instance = new InternalContextPropertiesStep();
        }
        return instance;
    }

    private InternalContextPropertiesStep() {
    }

    /// endregion

    /// region Constants

    static final String DEVICE_TIMESTAMP_UTC_PROPERTY = "device_timestamp_utc";
    static final String CONNECTION_TYPE_PROPERTY = "connection_type";
    static final String DEVICE_SCREEN_PROPERTIES_FORMAT = "device_screen%s";
    static final String DEVICE_SCREEN_DIAGONAL_PROPERTY = String.format(DEVICE_SCREEN_PROPERTIES_FORMAT, "_diagonal");
    static final String APP_VERSION_PROPERTY = "app_version";
    static final String MANUFACTURER_PROPERTY = "device_manufacturer";
    static final String MODEL_PROPERTY = "device_model";
    static final String OS_PROPERTIES_FORMAT = "os_%s";
    static final String EVENT_COLLECTION_PROPERTIES_FORMAT = "event_collection_%s";
    static final String BROWSER_LANGUAGE_PROPERTIES = "browser_language";
    static final String BROWSER_LANGUAGE_LOCALE_PROPERTIES = "browser_language_local";

    private static final String PLATFORM = "android";

    /// endregion

    /// region IInternalContextPropertiesFunction.getProperties implementations

    /// region Displaying properties

    private boolean displayingCached;
    private final Map<String, Object> displayingProperties = new HashMap<>();

    @SuppressWarnings("JavaReflectionMemberAccess")
    private final IInternalContextPropertiesFunction getDisplayingProperties = (Context ctx) -> {
        if (this.displayingCached) {
            return this.displayingProperties;
        }

        Display d = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);

        int widthPixels;
        int heightPixels;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Point realSize = new Point();
            d.getRealSize(realSize);

            widthPixels = realSize.x;
            heightPixels = realSize.y;
        } else {
            /// Device < API 17 compatibility
            try {
                Object rawWidth = Display.class.getMethod("getRawWidth").invoke(d);
                Object rawHeight = Display.class.getMethod("getRawHeight").invoke(d);

                widthPixels = rawWidth != null ? (Integer) rawWidth : 0;
                heightPixels = rawHeight != null ? (Integer) rawHeight : 0;
            } catch (Exception e) {
                PianoAnalytics.InternalLogger.severe("error on InternalContextPropertiesStep.getDisplayingProperties : " + e.toString());

                widthPixels = displayMetrics.widthPixels;
                heightPixels = displayMetrics.heightPixels;
            }
        }

        double x = Math.pow(widthPixels / displayMetrics.xdpi, 2);
        double y = Math.pow(heightPixels / displayMetrics.ydpi, 2);

        this.displayingProperties.put(String.format(DEVICE_SCREEN_PROPERTIES_FORMAT, "_width"), displayMetrics.widthPixels);
        this.displayingProperties.put(String.format(DEVICE_SCREEN_PROPERTIES_FORMAT, "_height"), displayMetrics.heightPixels);
        this.displayingProperties.put(DEVICE_SCREEN_DIAGONAL_PROPERTY, CastUtils.toDouble(new DecimalFormat("##.#").format(Math.sqrt(x + y))));
        this.displayingCached = true;

        return this.displayingProperties;
    };

    /// endregion

    /// region Application properties

    private boolean applicationCached;
    private final Map<String, Object> applicationProperties = new HashMap<>();
    private final IInternalContextPropertiesFunction getApplicationProperties = (Context ctx) -> {
        if (this.applicationCached) {
            return this.applicationProperties;
        }
        Pair<String, String> appProps = PianoAnalyticsUtils.getApplicationProperties(ctx);
        if (appProps != null) {
            this.applicationProperties.put("app_id", appProps.first);
            this.applicationProperties.put(APP_VERSION_PROPERTY, appProps.second);
            this.applicationCached = true;
        }
        return this.applicationProperties;
    };

    /// endregion

    /// region Hardware properties

    private static final String OS_NAME = String.format("%s %s", PLATFORM, Build.VERSION.RELEASE);
    private boolean hardwareCached;
    private final Map<String, Object> hardwareProperties = new HashMap<>();
    private final IInternalContextPropertiesFunction getHardwareProperties = (Context ctx) -> {
        if (this.hardwareCached) {
            return this.hardwareProperties;
        }

        this.hardwareProperties.put(String.format(OS_PROPERTIES_FORMAT, "group"), PLATFORM);
        this.hardwareProperties.put(String.format(OS_PROPERTIES_FORMAT, "version"), Build.VERSION.RELEASE);
        this.hardwareProperties.put("os", OS_NAME);
        this.hardwareProperties.put(MANUFACTURER_PROPERTY, Build.MANUFACTURER);
        this.hardwareProperties.put(MODEL_PROPERTY, Build.MODEL);
        this.hardwareCached = true;

        return this.hardwareProperties;
    };

    /// endregion

    /// region Locale properties

    private static final String BROWSER_LANGUAGE_LOCAL_FORMAT = "%s-%s";
    private final IInternalContextPropertiesFunction getLocaleProperties = (Context ctx) -> {
        Map<String, Object> m = new HashMap<>();
        Locale localeDefault = Locale.getDefault();

        String localeLanguage = localeDefault.toString();
        if (localeLanguage.contains("_")) {
            localeLanguage = localeLanguage.substring(localeLanguage.indexOf("_")+1);
        }

        m.put(DEVICE_TIMESTAMP_UTC_PROPERTY, PianoAnalyticsUtils.currentTimeMillis() / 1_000);
        m.put(BROWSER_LANGUAGE_PROPERTIES, localeDefault.getLanguage());
        m.put(BROWSER_LANGUAGE_LOCALE_PROPERTIES, localeLanguage);
        m.put(CONNECTION_TYPE_PROPERTY, PianoAnalyticsUtils.getConnection(ctx).stringValue());

        return m;
    };

    /// endregion

    /// region Tag properties

    private static final String EVENT_COLLECTION_VERSION = "3.2.1";
    private boolean tagCached;
    private final Map<String, Object> tagProperties = new HashMap<>();
    private final IInternalContextPropertiesFunction getTagProperties = (Context ctx) -> {
        if (this.tagCached) {
            return this.tagProperties;
        }
        this.tagProperties.put(String.format(EVENT_COLLECTION_PROPERTIES_FORMAT, "platform"), PLATFORM);
        this.tagProperties.put(String.format(EVENT_COLLECTION_PROPERTIES_FORMAT, "version"), EVENT_COLLECTION_VERSION);
        this.tagCached = true;

        return this.tagProperties;
    };

    /// endregion

    private final IInternalContextPropertiesFunction[] propertiesFunctions = new IInternalContextPropertiesFunction[]{
            getDisplayingProperties,
            getApplicationProperties,
            getHardwareProperties,
            getLocaleProperties,
            getTagProperties
    };

    /// endregion

    /// region Private methods

    private Map<String, Object> getProperties(Context ctx) {
        Map<String, Object> m = new HashMap<>();
        for (IInternalContextPropertiesFunction f : propertiesFunctions) {
            m.putAll(f.getProperties(ctx));
        }
        return m;
    }

    /// endregion

    /// region WorkingQueue.IStep implementation

    @Override
    public void processGetModel(Context ctx, Model m) {
        m.addContextProperties(getProperties(ctx));
    }

    @Override
    public boolean processTrackEvents(Context ctx, Model m, PianoAnalytics.OnWorkListener l) {
        m.addContextProperties(getProperties(ctx));
        return true;
    }

    /// endregion
}
