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

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

final class StorageStep implements WorkingQueue.IStep {

    /// region Constructors

    private static StorageStep instance = null;

    static StorageStep getInstance(Context ctx) {
        if (instance == null) {
            instance = new StorageStep(ctx);
        }
        return instance;
    }

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSS", Locale.getDefault());
    private final File filesDir;

    private StorageStep(Context ctx) {
        this.filesDir = ctx.getFilesDir();
    }

    /// endregion

    /// region Constants

    private static final String OFFLINE_DATA_FILENAME_PREFIX = "Piano-Analytics-Offline-File_";
    private static final String URI_FIELD = "uri";
    private static final String BODY_FIELD = "body";

    /// endregion

    /// region Private methods

    private boolean storeData(String builtDataJSONStr, Configuration.EncryptionMode encryptionMode) {
        String encryptedData = Crypt.encrypt(builtDataJSONStr, encryptionMode);
        if (encryptedData == null) {
            return false;
        }

        boolean done = false;
        String filename = OFFLINE_DATA_FILENAME_PREFIX + this.dateFormatter.format(new Date());
        try (FileOutputStream stream = new FileOutputStream(new File(this.filesDir, filename))) {
            stream.write(encryptedData.getBytes());
            done = true;
        } catch (IOException e) {
            PianoAnalytics.InternalLogger.severe("StorageStep.storeData : " + e.toString());
        }
        return done;
    }

    @SuppressWarnings("ComparatorCombinators")
    private Map<String, BuiltModel> readData() {
        Map<String, BuiltModel> storedData = new LinkedHashMap<>();
        File[] storedDataFiles = this.filesDir.listFiles((dir, name) -> name.startsWith(OFFLINE_DATA_FILENAME_PREFIX));
        if (storedDataFiles == null) {
            return storedData;
        }

        /// Get ordered files by creation date
        Arrays.sort(storedDataFiles, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));

        for (File f : storedDataFiles) {
            try {
                Map<String, Object> map = MapUtils.fromJSONString(Crypt.decrypt(PianoAnalyticsUtils.getStringFromInputStream(new FileInputStream(f))));
                storedData.put(f.getAbsolutePath(), new BuiltModel(CastUtils.toString(map.get(URI_FIELD)), CastUtils.toString(map.get(BODY_FIELD)), false));
            } catch (FileNotFoundException e) {
                PianoAnalytics.InternalLogger.severe("StorageStep.readData : " + e.toString());
            }
        }
        return storedData;
    }

    /// endregion

    /// region WorkingQueue.IStep implementation

    @Override
    public void processDeleteOfflineStorage(Model m) {
        int storageDaysRemaining = m.getStorageDaysRemaining();
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DATE, -storageDaysRemaining);
        Date storageRemainingDate = c.getTime();

        Map<String, BuiltModel> stored = readData();
        for (String key : stored.keySet()) {
            int index = key.lastIndexOf(OFFLINE_DATA_FILENAME_PREFIX);
            if (index == -1) {
                continue;
            }
            try {
                Date fileDate = dateFormatter.parse(key.substring(index + OFFLINE_DATA_FILENAME_PREFIX.length()));
                if (fileDate == null || fileDate.before(storageRemainingDate)) {
                    if (!new File(key).delete()) {
                        PianoAnalytics.InternalLogger.severe("StorageStep.processDeleteOfflineStorage : could not delete key file");
                    }
                }
            } catch (ParseException e) {
                PianoAnalytics.InternalLogger.severe("StorageStep.processDeleteOfflineStorage : " + e.toString());
            }
        }
    }

    @Override
    public boolean processSendOfflineStorage(Model m, PianoAnalytics.OnWorkListener l) {
        /// Retrieve stored data
        m.setStorage(readData());
        return true;
    }

    @Override
    public boolean processTrackEvents(Context ctx, Model m, PianoAnalytics.OnWorkListener l) {
        /// REQUIREMENTS
        Configuration configuration = m.getConfiguration();
        BuiltModel builtModel = m.getBuiltModel();

        Configuration.EncryptionMode encryptionMode = Configuration.EncryptionMode.fromString(configuration.get(Configuration.ConfigurationKey.ENCRYPTION_MODE));

        if (builtModel.isMustBeSaved()) {
            Map<String, Object> map = new HashMap<>();
            map.put(URI_FIELD, builtModel.getUri());
            map.put(BODY_FIELD, builtModel.getBody());
            if (!storeData(new JSONObject(map).toString(), encryptionMode)) {
                PianoAnalytics.InternalLogger.severe("StorageStep.processTrackEvents : data could not be stored");
            }
            return false;
        }

        /// Retrieve stored data
        m.setStorage(readData());
        return true;
    }

    /// endregion
}
