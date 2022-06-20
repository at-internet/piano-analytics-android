package io.piano.analytics;

import android.os.Build;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

import io.piano.analytics.Event;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.LOLLIPOP})
public class EventTest {

    @Test
    public void constructor() {
        Map<String, Object> subData = new HashMap<>();
        subData.put("a", "a");
        subData.put("b", "b");
        subData.put("c", "c");

        Map<String, Object> data = new HashMap<>();
        data.put("1", "1");
        data.put("2", "2");
        data.put("3", "3");
        data.put("subData", subData);

        Event e = new Event("test", data);

        assertEquals("test", e.getName());
        assertEquals("{\"1\":\"1\",\"2\":\"2\",\"3\":\"3\",\"subdata_c\":\"c\",\"subdata_b\":\"b\",\"subdata_a\":\"a\"}", new JSONObject(e.getData()).toString());
    }

    @Test
    public void setName() {
        Map<String, Object> data = new HashMap<>();
        data.put("1", "1");
        data.put("2", "2");
        data.put("3", "3");

        Event e = new Event("test", data);

        assertEquals("test", e.getName());

        e.setName("new");
        assertEquals("new", e.getName());
    }
}