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

import static io.piano.analytics.PrivacyStep.WILDCARD;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

final class PianoAnalyticsUtils {
    /// region Enum

    enum ConnectionType {
        GPRS("GPRS"),
        EDGE("EDGE"),
        TWOG("2G"),
        THREEG("3G"),
        THREEGPLUS("3G+"),
        FOURG("4G"),
        FIVEG("5G"),
        WIFI("WIFI"),
        OFFLINE("OFFLINE"),
        UNKNOWN("UNKNOWN");

        private final String str;

        ConnectionType(String val) {
            str = val;
        }

        String stringValue() {
            return str;
        }
    }

    /// endregion

    /// region Constructors

    private PianoAnalyticsUtils() {
    }

    /// endregion

    /// region Hardware methods

    @SuppressLint({"MissingPermission", "SwitchIntDef"})
    static ConnectionType getConnection(Context ctx) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo == null) {
            return ConnectionType.OFFLINE;
        }

        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return ConnectionType.WIFI;
        }

        TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager == null) {
            return ConnectionType.UNKNOWN;
        }

        try {
            switch (telephonyManager.getNetworkType()) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return ConnectionType.GPRS;
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return ConnectionType.EDGE;
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return ConnectionType.TWOG;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    return ConnectionType.THREEG;
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return ConnectionType.THREEGPLUS;
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return ConnectionType.FOURG;
                case TelephonyManager.NETWORK_TYPE_NR:
                    return ConnectionType.FIVEG;
                default:
                    return ConnectionType.UNKNOWN;
            }
        } catch (SecurityException e) {
            PianoAnalytics.InternalLogger.severe("Utils.getConnection : " + e.toString());
            return ConnectionType.UNKNOWN;
        }
    }

    /// endregion

    /// region Application properties methods

    static Pair<String, String> getApplicationProperties(Context ctx) {
        try {
            PackageManager pkgManager = ctx.getPackageManager();
            if (pkgManager == null) {
                throw new IllegalStateException("Invalid Package Manager");
            }
            String pkgName = ctx.getPackageName();
            if (pkgName == null) {
                throw new IllegalStateException("Invalid Package Name");
            }
            PackageInfo pkgInfo = pkgManager.getPackageInfo(pkgName, 0);
            if (pkgInfo == null) {
                throw new IllegalStateException("Invalid Package Info");
            }
            return new Pair<>(pkgName, pkgInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            PianoAnalytics.InternalLogger.severe("Utils.getApplicationProperties : " + e.toString());
        }
        return null;
    }

    static boolean isClassUnavailable(String className) {
        if (isEmptyString(className)) {
            return true;
        }
        try {
            Class.forName(className);
            return false;
        } catch (ClassNotFoundException ce) {
            return true;
        }
    }

    /// endregion

    /// region Parsing methods

    static boolean isEmptyString(String s) {
        return s == null || s.length() == 0;
    }

    public static boolean isEmptyInteger(Integer integer) {
        return integer == null || integer == 0;
    }

    public static boolean isEmptyUnsignedInteger(Integer unsignedInteger) {
        return unsignedInteger == null || unsignedInteger <= 0;
    }

    public static boolean isEmptyLong(Long nb) {
        return nb == null || nb == 0;
    }

    public static boolean isEmptyUnsignedLong(Long unsignedLong) {
        return unsignedLong == null || unsignedLong <= 0;
    }

    static boolean isEmptyArray(String[] arr) {
        return arr == null || arr.length == 0;
    }

    /// endregion

    /// region Time methods

    static long currentTimeMillis() {
        long timeMillis;
        int year;
        int retry = 3;
        do {
            retry--;
            timeMillis = System.currentTimeMillis();

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timeMillis);
            year = cal.get(Calendar.YEAR);
            if (year < 2000) {
                sleep(100);
            }
        } while (year < 2000 && retry > 0);

        //return timeMillis;
        return timeMillis;
    }

    static int convertMillisTo(TimeUnit unit, long t) {
        return (int) unit.convert(t, TimeUnit.MILLISECONDS);
    }

    static void sleep(int time) {
        try {
            TimeUnit.MILLISECONDS.sleep(time);
        } catch (InterruptedException e) {
            PianoAnalytics.InternalLogger.severe("PianoAnalyticsUtils.sleep : " + e.toString());
            Thread.currentThread().interrupt();
        }
    }

    /// endregion

    /// region Data formatting methods

    static String getStringFromInputStream(InputStream is) {
        if (is == null) {
            return null;
        }
        byte[] buffer;
        try {
            buffer = new byte[is.available()];
            if (is.read(buffer) == -1) {
                throw new IOException("bad data read from input stream");
            }

        } catch (IOException e) {
            PianoAnalytics.InternalLogger.severe("PianoAnalyticsUtils.getStringFromInputStream : " + e.toString());
            buffer = new byte[]{};
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                PianoAnalytics.InternalLogger.severe("PianoAnalyticsUtils.getStringFromInputStream : " + e.toString());
            }
        }

        return new String(buffer);
    }

    static Map<PianoAnalytics.PrivacyVisitorMode, Map<String, Object>> getPrivacyConfig(String s) {
        if (isEmptyString(s)) {
            return new HashMap<>();
        }

        Map<PianoAnalytics.PrivacyVisitorMode, Map<String, Object>> map = new HashMap<>();

        try {
            JSONObject jsonObject = new JSONObject(s);

            Iterator<String> keysItr = jsonObject.keys();
            while (keysItr.hasNext()) {
                PianoAnalytics.PrivacyVisitorMode key = PianoAnalytics.PrivacyVisitorMode.fromString(keysItr.next());
                Object value = jsonObject.get(key.stringValue());

                if (!(value instanceof JSONObject)) {
                    throw new JSONException("type unexpected");
                }

                map.put(key, MapUtils.fromJSONObject((JSONObject) value));
            }
        } catch (JSONException e) {
            return new HashMap<>();
        }

        return map;
    }

    public static <T> boolean contains(T[] arr, T value) {
        for (T t : arr) {
            if (t == value) {
                return true;
            }
        }
        return false;
    }

    public static boolean simpleWildcardCompare(String[] arr, String str) {
        for (String s : arr) {
            int wildcardIndex = s.indexOf(WILDCARD);

            if (wildcardIndex == 0 ||
                    (wildcardIndex == -1 && str.equals(s)) ||
                    (wildcardIndex != -1 && str.startsWith(s.substring(0, wildcardIndex)))) {
                return true;
            }

        }

        return false;
    }

    /// endregion
}

final class SetUtils {
    static <T> Set<T> merge(Set<T> s1, Set<T> s2) {
        Set<T> result = new HashSet<>();

        if (s1 != null) {
            result.addAll(s1);
        }
        if (s2 != null) {
            result.addAll(s2);
        }

        return result;
    }

    static <T> Set<T> getSingletonIfContains(Set<T> s, T element) {
        return s.contains(element) ? new HashSet<>(Collections.singletonList(element)) : s;
    }

    static <T> Set<T> copy(Set<T> s) {
        if (s == null) {
            return null;
        }
        return new HashSet<>(s);
    }
}

final class ListUtils {
    static List<Object> fromJSONArray(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = fromJSONArray((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = MapUtils.fromJSONObject((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

    static <T> Boolean isEmpty(List<T> l) {
        return l == null || l.isEmpty();
    }
}

final class MapUtils {
    /// region Constants

    private static final String FLAT_SEPARATOR = "_";

    /// endregion

    static Map<String, Object> fromJSONString(String s) {
        if (PianoAnalyticsUtils.isEmptyString(s)) {
            return new HashMap<>();
        }
        try {
            return fromJSONObject(new JSONObject(s));
        } catch (JSONException e) {
            return new HashMap<>();
        }
    }

    static <K, T> Map<K, Set<T>> mergeSets(Map<K, Set<T>> m1, Map<K, Set<T>> m2) {
        Map<K, Set<T>> result = new HashMap<>();

        if (m1 != null) {
            result.putAll(m1);
        }
        if (m2 == null) {
            return result;
        }

        for (Map.Entry<K, Set<T>> entry : m2.entrySet()) {
            result.put(entry.getKey(), SetUtils.merge(result.get(entry.getKey()), entry.getValue()));
        }

        return result;
    }

    static <K, T> void minimizeSet(Map<K, Set<T>> m, T element) {
        for (Map.Entry<K, Set<T>> entry : m.entrySet()) {
            m.put(entry.getKey(), SetUtils.getSingletonIfContains(entry.getValue(), element));
        }
    }

    static <K, V> V getValueOrPutDefault(Map<K, V> m, K key, V defaultValue) {
        if (!m.containsKey(key) || m.get(key) == null) {
            m.put(key, defaultValue);
        }
        return m.get(key);
    }

    static Map<String, Object> fromJSONObject(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<>();

        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if (value instanceof JSONArray) {
                value = ListUtils.fromJSONArray((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = fromJSONObject((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    static Map<String, Object> toFlatten(Map<String, Object> src) {
        Map<String, Object> dst = new HashMap<>();
        if (src != null) {
            doFlatten(src, "", dst);
        }
        return dst;
    }

    private static void doFlatten(Map<String, Object> src, String prefix, Map<String, Object> dst) {
        for (Map.Entry<String, Object> e : src.entrySet()) {
            Object value = e.getValue();
            String completeKey = PianoAnalyticsUtils.isEmptyString(prefix) ? e.getKey() : prefix + FLAT_SEPARATOR + e.getKey();
            if (value instanceof Map) {
                doFlatten(CastUtils.toMap(String.class, Object.class, (Map<?, ?>) value), completeKey, dst);
            } else {
                dst.put(completeKey.toLowerCase(Locale.getDefault()), value);
            }
        }
    }

    static <K, V> Map<K, V> copy(Map<K, V> m) {
        if (m == null) {
            return null;
        }

        Map<K, V> cpy = new HashMap<>();

        for (Map.Entry<K, V> entry : m.entrySet()) {
            cpy.put(entry.getKey(), entry.getValue());
        }

        return cpy;
    }

    static <V> V getFirstValue(Map<String,V> m, String... keys) {
        for (String key : keys) {
            if (m.containsKey(key)) {
                return m.get(key);
            }
        }
        return null;
    }
}

final class CastUtils {

    static int toInt(Object o) {
        if (o == null) {
            return 0;
        }
        if (o instanceof Integer) {
            return (int) o;
        }
        if (o instanceof Double) {
            return ((Double) o).intValue();
        }
        try {
            return Double.valueOf(toString(o)).intValue();
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    static String toString(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof String) {
            return (String) o;
        }
        return String.valueOf(o);
    }

    static double toDouble(Object o) {
        if (o == null) {
            return 0.;
        }
        if (o instanceof Double) {
            return (double) o;
        }
        try {
            return Double.parseDouble(toString(o));
        } catch (NumberFormatException ignored) {
            return 0.;
        }
    }

    static boolean toBool(Object o) {
        if (o instanceof Boolean) {
            return (boolean) o;
        }
        return Boolean.parseBoolean(toString(o));
    }

    public static <K, V> Map<K, V> toMap(Class<? extends K> keyClass, Class<? extends V> valueClass, Map<?, ?> rawMap) {
        Map<K, V> result = new HashMap<>();

        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            try {
                result.put(keyClass.cast(entry.getKey()), valueClass.cast(entry.getValue()));
            } catch (ClassCastException ignored) {
                // pass
            }
        }

        return result;
    }
}

