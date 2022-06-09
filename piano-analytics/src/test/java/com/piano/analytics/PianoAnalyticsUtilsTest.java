package com.piano.analytics;

import android.content.Context;
import android.os.Build;
import android.util.Pair;

import androidx.test.core.app.ApplicationProvider;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.LOLLIPOP})
public class PianoAnalyticsUtilsTest {

    @Test
    public void isEmptyString() {
        assertTrue(PianoAnalyticsUtils.isEmptyString(null));
        assertTrue(PianoAnalyticsUtils.isEmptyString(""));
        assertFalse(PianoAnalyticsUtils.isEmptyString("test"));
    }

    @Test
    public void getApplicationProperties() {
        Context ctx = ApplicationProvider.getApplicationContext();
        Pair<String, String> v = PianoAnalyticsUtils.getApplicationProperties(ctx);

        assertNotNull(v);
        assertEquals("com.piano.analytics.test", v.first);
        assertNull(v.second);
    }

    @Test
    public void isClassUnavailable() {
        assertTrue(PianoAnalyticsUtils.isClassUnavailable(null));
        assertTrue(PianoAnalyticsUtils.isClassUnavailable(""));
        assertTrue(PianoAnalyticsUtils.isClassUnavailable("com.piano.analytics.UnknownClass"));
        assertFalse(PianoAnalyticsUtils.isClassUnavailable("com.piano.analytics.PianoAnalytics"));
    }

    @Test
    public void parseString() {
        assertNull(CastUtils.toString(null));
        assertEquals("", CastUtils.toString(""));
        assertEquals("test", CastUtils.toString("test"));
        assertEquals("10", CastUtils.toString(10));
        assertEquals("true", CastUtils.toString(true));
    }

    @Test
    public void parseDouble() {
        assertEquals(0., CastUtils.toDouble(null), 0);
        assertEquals(0, CastUtils.toDouble(true), 0);
        assertEquals(0, CastUtils.toDouble("test"), 0);
        assertEquals(0, CastUtils.toDouble(0), 0);
        assertEquals(54.65445, CastUtils.toDouble(54.65445), 0);
        assertEquals(54, CastUtils.toDouble(54), 0);
        assertEquals(654.67, CastUtils.toDouble("654.67"), 0);
        assertEquals(65, CastUtils.toDouble("65"), 0);
    }

    @Test
    public void parseInt() {
        assertEquals(0, CastUtils.toInt(null));
        assertEquals(0, CastUtils.toInt(true));
        assertEquals(0, CastUtils.toInt("test"));
        assertEquals(15, CastUtils.toInt(15));
        assertEquals(15, CastUtils.toInt(15.3654));
        assertEquals(15, CastUtils.toInt("15"));
        assertEquals(15, CastUtils.toInt("15.3654"));
    }

    @Test
    public void parseBool() {
        assertFalse(CastUtils.toBool(null));
        assertFalse(CastUtils.toBool("test"));
        assertFalse(CastUtils.toBool(1));
        assertTrue(CastUtils.toBool(true));
        assertTrue(CastUtils.toBool("true"));
    }

    @Test
    public void getStringFromInputStream() {
        assertNull(PianoAnalyticsUtils.getStringFromInputStream(null));
        assertEquals("", PianoAnalyticsUtils.getStringFromInputStream(new ByteArrayInputStream("".getBytes())));
        assertEquals("test", PianoAnalyticsUtils.getStringFromInputStream(new ByteArrayInputStream("test".getBytes())));
    }

    @Test
    public void convertMillisTo() {
        int oneDayInMillis = 1000 * 60 * 60 * 24;
        assertEquals(1, PianoAnalyticsUtils.convertMillisTo(TimeUnit.DAYS, oneDayInMillis));
        assertEquals(24, PianoAnalyticsUtils.convertMillisTo(TimeUnit.HOURS, oneDayInMillis));
        assertEquals(1_440, PianoAnalyticsUtils.convertMillisTo(TimeUnit.MINUTES, oneDayInMillis));
        assertEquals(86_400, PianoAnalyticsUtils.convertMillisTo(TimeUnit.SECONDS, oneDayInMillis));
    }

    @Test
    public void jsonStringToMap() {
        assertEquals(new HashMap<>(), MapUtils.fromJSONString(null));
        assertEquals(new HashMap<>(), MapUtils.fromJSONString(""));
        assertEquals(new HashMap<>(), MapUtils.fromJSONString("test"));

        Map<String, Object> expected = new HashMap<>();
        expected.put("source", "");
        expected.put("length", "Long-Form");
        expected.put("category", "Content");
        expected.put("name", "Fire");
        assertEquals(expected, MapUtils.fromJSONString("{\"source\":\"\",\"length\":\"Long-Form\",\"category\":\"Content\",\"name\":\"Fire\"}"));
    }

    @Test
    public void toFlatten() {
        assertEquals("{}", new JSONObject(MapUtils.toFlatten(null)).toString());
        assertEquals("{}", new JSONObject(MapUtils.toFlatten(new HashMap<>())).toString());

        Map<String, Object> subData3 = new HashMap<>();
        subData3.put("string", "string");
        subData3.put("int", 12);
        subData3.put("double", 12.4);
        subData3.put("bool", true);
        subData3.put("array", Arrays.asList("a", "b", "c"));

        Map<String, Object> subData2 = new HashMap<>();
        subData2.put("value", "value");
        subData2.put("l2", subData3);

        Map<String, Object> subData1 = new HashMap<>();
        subData1.put("value", "value");
        subData1.put("l1", subData2);

        Map<String, Object> data = new HashMap<>();
        data.put("data", subData1);

        assertEquals("{" +
                        "\"data_l1_l2_bool\":true," +
                        "\"data_value\":\"value\"," +
                        "\"data_l1_l2_array\":[\"a\",\"b\",\"c\"]," +
                        "\"data_l1_l2_string\":\"string\"," +
                        "\"data_l1_l2_int\":12," +
                        "\"data_l1_l2_double\":12.4," +
                        "\"data_l1_value\":\"value\"" +
                        "}",
                new JSONObject(MapUtils.toFlatten(data)).toString());
    }
}