package io.piano.analytics;

import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import io.piano.analytics.User;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.LOLLIPOP})
public class UserTest {

    @Test
    public void constructor() {
        User user = new User("id");
        assertEquals("id", user.getId());
        assertNull(user.getCategory());
        assertEquals("cat", user.setCategory("cat").getCategory());
    }

    @Test
    public void constructorFromJSONObject() {
        User user = null;
        JSONObject obj = new JSONObject();
        try {
            user = new User(obj);
        } catch (JSONException ignored) {
        } finally {
            assertNull(user);
        }

        try {
            obj.put(User.ID_FIELD, "id");
            obj.put(User.CATEGORY_FIELD, "cat");
            user = new User(obj);
        } catch (JSONException ignored) {
        } finally {
            assertEquals("id", user.getId());
            assertEquals("cat", user.getCategory());
        }
    }

    @Test
    public void toMap() {
        User user = new User("id");
        Map<String, String> map = user.toMap();
        assertEquals(1, map.size());
        assertEquals("id", map.get(User.ID_FIELD));
        assertNull(map.get(User.CATEGORY_FIELD));

        map = user.setCategory("cat").toMap();
        assertEquals(2, map.size());
        assertEquals("id", map.get(User.ID_FIELD));
        assertEquals("cat", map.get(User.CATEGORY_FIELD));
    }
}