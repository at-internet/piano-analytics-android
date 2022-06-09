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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

final class SendStep implements WorkingQueue.IStep {

    /// region Constructors

    private static SendStep instance = null;

    static SendStep getInstance() {
        if (instance == null) {
            instance = new SendStep();
        }
        return instance;
    }

    private SendStep() {
    }

    /// endregion

    /// region Constants

    private static final int TIMEOUT_MS = 10_000;
    private static final int MAX_RETRY = 3;
    private static final int SLEEP_TIME_MS = 400;

    /// endregion

    /// region Private methods

    private void sendStoredData(Map<String, BuiltModel> stored, String userAgent) {
        for (Map.Entry<String, BuiltModel> entry : stored.entrySet()) {
            this.send(entry.getValue(), userAgent);
            String key = entry.getKey();
            if (!new File(key).delete()) {
                PianoAnalytics.InternalLogger.severe(String.format("SendStep.sendStoredData : cannot delete file '%s'", key));
            }
        }
    }

    @SuppressWarnings({"EmptyTryBlock", "CharsetObjectCanBeUsed"})
    private void send(BuiltModel builtModel, String userAgent) {
        HttpURLConnection connection = null;
        boolean success = false;
        int count = 0;
        do {
            count++;
            try {
                URL url = new URL(builtModel.getUri());
                connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(TIMEOUT_MS);
                connection.setConnectTimeout(TIMEOUT_MS);
                connection.setRequestProperty("User-Agent", userAgent);

                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                try (OutputStream os = connection.getOutputStream(); OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8")) {
                    osw.write(builtModel.getBody());
                    osw.flush();
                }
                connection.connect();

                int statusCode = connection.getResponseCode();
                success = statusCode >= 200 && statusCode <= 399;

            } catch (IOException e) {
                PianoAnalytics.InternalLogger.severe("SendStep.send : " + e.toString());
            } finally {
                if (connection != null) {
                    try (InputStream ignored = success ? connection.getInputStream() : connection.getErrorStream()) {
                        /// Nothing to do
                    } catch (IOException e) {
                        PianoAnalytics.InternalLogger.severe("SendStep.send : " + e.toString());
                    }
                    connection.disconnect();
                }
            }
            if (!success) {
                PianoAnalyticsUtils.sleep(SLEEP_TIME_MS);
            }
        } while (!success && count < MAX_RETRY);

        if (!success) {
            PianoAnalytics.InternalLogger.severe("SendStep.send : unable to send hit after retries -> data lost");
        }
    }

    /// endregion

    /// region WorkingQueue.IStep implementation

    @Override
    public boolean processSendOfflineStorage(Model m, PianoAnalytics.OnWorkListener l) {
        /// REQUIREMENTS
        Configuration configuration = m.getConfiguration();
        Map<String, BuiltModel> stored = m.getStorage();

        String userAgent = configuration.get(Configuration.ConfigurationKey.CUSTOM_USER_AGENT);
        this.sendStoredData(stored, userAgent);
        return true;
    }

    @Override
    public boolean processTrackEvents(Context ctx, Model m, PianoAnalytics.OnWorkListener l) {
        /// REQUIREMENTS
        Configuration configuration = m.getConfiguration();
        Map<String, BuiltModel> stored = m.getStorage();
        BuiltModel built = m.getBuiltModel();

        /// Sent offline hits before new built hit
        String userAgent = configuration.get(Configuration.ConfigurationKey.CUSTOM_USER_AGENT);
        this.sendStoredData(stored, userAgent);

        this.send(built, userAgent);

        return true;
    }

    /// endregion
}
