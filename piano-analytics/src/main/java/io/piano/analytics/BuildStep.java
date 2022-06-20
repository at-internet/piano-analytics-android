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

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class BuildStep implements WorkingQueue.IStep {

    /// region Constructors

    private static BuildStep instance = null;

    static BuildStep getInstance() {
        if (instance == null) {
            instance = new BuildStep();
        }
        return instance;
    }

    private BuildStep() {
    }

    /// endregion

    /// region Constants

    private static final String REQUEST_URI_DOMAIN_FORMAT = "https://%s";
    private static final String REQUEST_URI_QUERY_FORMAT = "%s?s=%s&idclient=%s";
    private static final String EVENTS_FIELD = "events";
    private static final String CONTEXT_FIELD = "context";

    /// endregion

    /// region WorkingQueue.IStep implementation

    @Override
    public boolean processTrackEvents(Context ctx, Model m, PianoAnalytics.OnWorkListener l) {
        /// REQUIREMENTS
        Configuration configuration = m.getConfiguration();
        Map<String, Object> contextProperties = m.getContextProperties();
        List<Event> events = m.getEvents();

        Configuration.OfflineStorageMode offlineMode = Configuration.OfflineStorageMode.fromString(configuration.get(Configuration.ConfigurationKey.OFFLINE_STORAGE_MODE));

        boolean mustBeSaved = false;
        if (offlineMode == Configuration.OfflineStorageMode.ALWAYS ||
                (offlineMode == Configuration.OfflineStorageMode.REQUIRED && PianoAnalyticsUtils.getConnection(ctx) == PianoAnalyticsUtils.ConnectionType.OFFLINE)) {

            for (Event event : events) {
                event.getData().put(InternalContextPropertiesStep.CONNECTION_TYPE_PROPERTY, PianoAnalyticsUtils.ConnectionType.OFFLINE.stringValue());
            }
            mustBeSaved = true;
        }

        /// Body
        JSONArray serializedEvents = new JSONArray();
        for (Event e : events) {
            serializedEvents.put(new JSONObject(e.toMap()));
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put(EVENTS_FIELD, serializedEvents);

        /// URI
        String visitorId = configuration.get(Configuration.ConfigurationKey.VISITOR_ID);
        try {
            URL domain = new URL(String.format(REQUEST_URI_DOMAIN_FORMAT,
                    configuration.get(Configuration.ConfigurationKey.COLLECT_DOMAIN)));
            URL url = new URL(domain, String.format(REQUEST_URI_QUERY_FORMAT,
                    configuration.get(Configuration.ConfigurationKey.PATH),
                    configuration.get(Configuration.ConfigurationKey.SITE),
                    (PianoAnalyticsUtils.isEmptyString(visitorId) ? "" : visitorId)));
            m.setBuiltModel(new BuiltModel(url.toString(), new JSONObject(body).toString(), mustBeSaved));
        } catch (MalformedURLException e) {
            PianoAnalytics.InternalLogger.severe("error on build step processTrackEvents: " + e.toString());
            return false;
        }

        return true;
    }

    /// endregion
}
