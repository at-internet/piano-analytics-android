package io.piano.analytics;

import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.LOLLIPOP})
public class BuildStepTest {

    @Test
    public void processTrackEvents() throws JSONException {
        BuildStep buildStep = BuildStep.getInstance();

        /// Online
        Model model = new Model()
                .Config(TestResources.createConfiguration())
                .addContextProperties(TestResources.createInternalContextProperties())
                .addContextProperties(TestResources.createCustomerContextProperties())
                .setEvents(TestResources.createEventsList());

        assertTrue(buildStep.processTrackEvents(ApplicationProvider.getApplicationContext(), model, null));
        BuiltModel built = model.getBuiltModel();

        assertNotNull(built);
        assertFalse(built.isMustBeSaved());
        assertEquals("https://example.com/test?s=123456&idclient=visitor", built.getUri());
        assertEquals(new JSONObject("{\"context\":[{\"data\":{\"browser_language_local\":\"US\",\"device_screen_diagonal\":3.6,\"app_version\":null,\"connection_type\":\"UNKNOWN\",\"os_version\":\"5.0.2\",\"os_group\":\"android\",\"manufacturer\":\"unknown\",\"prop2\":\"val2\",\"prop1\":\"val1\",\"event_collection_version\":\"3.0.0\",\"prop1_1\":\"val1\",\"prop1_2\":\"val2\",\"device_screen_width\":320,\"os_name\":\"android 5.0.2\",\"device_screen_height\":470,\"model\":\"robolectric\",\"app_id\":\"io.piano.analytics.test\",\"browser_language\":\"en\",\"event_collection_platform\":\"android\"}}],\"events\":[{\"data\":{\"ev1prop3_sd_prop1\":\"sd1\",\"page\":\"myPage1\",\"ev1prop2\":2,\"ev1prop1\":\"value 1\",\"ev1prop3_sd_prop2\":\"sd2\"},\"name\":\"event1\"},{\"data\":{\"ev2prop1\":\"test value\",\"page\":\"myPage2\"},\"name\":\"event2\"}]}").toString(),
                built.getBody());

        /// Without VisitorId
        model = new Model()
                .Config(TestResources.createConfiguration())
                .addContextProperties(TestResources.createInternalContextProperties())
                .addContextProperties(TestResources.createCustomerContextProperties())
                .setEvents(TestResources.createEventsList());
        model.getConfiguration().set(Configuration.ConfigurationKey.VISITOR_ID, "");

        assertTrue(buildStep.processTrackEvents(ApplicationProvider.getApplicationContext(), model, null));

        built = model.getBuiltModel();

        assertNotNull(built);
        assertFalse(built.isMustBeSaved());
        assertEquals("https://example.com/test?s=123456&idclient=", built.getUri());
        assertEquals(new JSONObject("{\"context\":[{\"data\":{\"browser_language_local\":\"US\",\"device_screen_diagonal\":3.6,\"app_version\":null,\"connection_type\":\"UNKNOWN\",\"os_version\":\"5.0.2\",\"os_group\":\"android\",\"manufacturer\":\"unknown\",\"prop2\":\"val2\",\"prop1\":\"val1\",\"event_collection_version\":\"3.0.0\",\"prop1_1\":\"val1\",\"prop1_2\":\"val2\",\"device_screen_width\":320,\"os_name\":\"android 5.0.2\",\"device_screen_height\":470,\"model\":\"robolectric\",\"app_id\":\"io.piano.analytics.test\",\"browser_language\":\"en\",\"event_collection_platform\":\"android\"}}],\"events\":[{\"data\":{\"ev1prop3_sd_prop1\":\"sd1\",\"page\":\"myPage1\",\"ev1prop2\":2,\"ev1prop1\":\"value 1\",\"ev1prop3_sd_prop2\":\"sd2\"},\"name\":\"event1\"},{\"data\":{\"ev2prop1\":\"test value\",\"page\":\"myPage2\"},\"name\":\"event2\"}]}").toString(),
                built.getBody());

        /// Offline
        model = new Model()
                .Config(TestResources.createConfiguration())
                .addContextProperties(TestResources.createInternalContextProperties())
                .addContextProperties(TestResources.createCustomerContextProperties())
                .setEvents(TestResources.createEventsList());
        model.getConfiguration().set(Configuration.ConfigurationKey.OFFLINE_STORAGE_MODE, Configuration.OfflineStorageMode.ALWAYS.stringValue());
        assertTrue(buildStep.processTrackEvents(ApplicationProvider.getApplicationContext(), model, null));
        built = model.getBuiltModel();

        assertNotNull(built);
        assertTrue(built.isMustBeSaved());
        assertEquals("https://example.com/test?s=123456&idclient=visitor", built.getUri());
        assertEquals(new JSONObject("{\"context\":[{\"data\":{\"browser_language_local\":\"US\",\"device_screen_diagonal\":3.6,\"app_version\":null,\"connection_type\":\"OFFLINE\",\"os_version\":\"5.0.2\",\"os_group\":\"android\",\"manufacturer\":\"unknown\",\"prop2\":\"val2\",\"prop1\":\"val1\",\"event_collection_version\":\"3.0.0\",\"prop1_1\":\"val1\",\"prop1_2\":\"val2\",\"device_screen_width\":320,\"os_name\":\"android 5.0.2\",\"device_screen_height\":470,\"model\":\"robolectric\",\"app_id\":\"io.piano.analytics.test\",\"browser_language\":\"en\",\"event_collection_platform\":\"android\"}}],\"events\":[{\"data\":{\"ev1prop3_sd_prop1\":\"sd1\",\"page\":\"myPage1\",\"ev1prop2\":2,\"ev1prop1\":\"value 1\",\"ev1prop3_sd_prop2\":\"sd2\"},\"name\":\"event1\"},{\"data\":{\"ev2prop1\":\"test value\",\"page\":\"myPage2\"},\"name\":\"event2\"}]}").toString(),
                built.getBody());
    }

    @Test
    public void processTrackEventsWithoutVisitorId() throws JSONException {
        BuildStep buildStep = BuildStep.getInstance();

        Configuration configuration = TestResources.createConfiguration();
        configuration.set(Configuration.ConfigurationKey.VISITOR_ID, "");

        Model model = new Model()
                .Config(configuration)
                .addContextProperties(TestResources.createInternalContextProperties())
                .addContextProperties(TestResources.createCustomerContextProperties())
                .setEvents(TestResources.createEventsList());

        assertTrue(buildStep.processTrackEvents(ApplicationProvider.getApplicationContext(), model, null));

        BuiltModel built = model.getBuiltModel();

        assertNotNull(built);
        assertFalse(built.isMustBeSaved());
        assertEquals("https://example.com/test?s=123456&idclient=", built.getUri());
        assertEquals(new JSONObject("{\"context\":[{\"data\":{\"browser_language_local\":\"US\",\"device_screen_diagonal\":3.6,\"app_version\":null,\"connection_type\":\"UNKNOWN\",\"os_version\":\"5.0.2\",\"os_group\":\"android\",\"manufacturer\":\"unknown\",\"prop2\":\"val2\",\"prop1\":\"val1\",\"event_collection_version\":\"3.0.0\",\"prop1_1\":\"val1\",\"prop1_2\":\"val2\",\"device_screen_width\":320,\"os_name\":\"android 5.0.2\",\"device_screen_height\":470,\"model\":\"robolectric\",\"app_id\":\"io.piano.analytics.test\",\"browser_language\":\"en\",\"event_collection_platform\":\"android\"}}],\"events\":[{\"data\":{\"ev1prop3_sd_prop1\":\"sd1\",\"page\":\"myPage1\",\"ev1prop2\":2,\"ev1prop1\":\"value 1\",\"ev1prop3_sd_prop2\":\"sd2\"},\"name\":\"event1\"},{\"data\":{\"ev2prop1\":\"test value\",\"page\":\"myPage2\"},\"name\":\"event2\"}]}").toString(),
                built.getBody());
    }
}