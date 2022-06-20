package io.piano.analytics;

import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.JELLY_BEAN})
public class InternalContextPropertiesStepAPI16Test {

    @Test
    public void processTrackEvents() {
        Map<String, Object> testInternalContextProperties = TestResources.createInternalContextProperties();
        /// API 17
        testInternalContextProperties.put("device_screen_diagonal", 0.0);
        testInternalContextProperties.put("os_version", "4.1.2");
        testInternalContextProperties.put("os_name", "android 4.1.2");

        InternalContextPropertiesStep internalContextPropertiesStep = InternalContextPropertiesStep.getInstance();

        /// To check if values cached
        for (int i = 0; i < 3; i++) {
            Model model = new Model();

            assertTrue(internalContextPropertiesStep.processTrackEvents(ApplicationProvider.getApplicationContext(), model, null));

            Map<String, Object> internalProperties = model.getContextProperties();
            assertNotNull(internalProperties);

            /// DYNAMIC
            assertTrue(internalProperties.containsKey("device_timestamp_utc"));
            internalProperties.remove("device_timestamp_utc");

            assertEquals(testInternalContextProperties, internalProperties);
        }
    }

    @Test
    public void processGetModel() {
        Map<String, Object> testInternalContextProperties = TestResources.createInternalContextProperties();
        /// API 17
        testInternalContextProperties.put("device_screen_diagonal", 0.0);
        testInternalContextProperties.put("os_version", "4.1.2");
        testInternalContextProperties.put("os_name", "android 4.1.2");

        InternalContextPropertiesStep internalContextPropertiesStep = InternalContextPropertiesStep.getInstance();

        Model model = new Model();
        internalContextPropertiesStep.processGetModel(ApplicationProvider.getApplicationContext(), model);

        Map<String, Object> internalProperties = model.getContextProperties();
        assertNotNull(internalProperties);

        /// DYNAMIC
        assertTrue(internalProperties.containsKey("device_timestamp_utc"));
        internalProperties.remove("device_timestamp_utc");

        assertEquals(testInternalContextProperties, internalProperties);
    }
}