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
@Config(sdk = {Build.VERSION_CODES.LOLLIPOP})
public class InternalContextPropertiesStepTest {

    @Test
    public void processTrackEvents() {
        Map<String, Object> testInternalContextProperties = TestResources.createInternalContextProperties();
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
        InternalContextPropertiesStep internalContextPropertiesStep = InternalContextPropertiesStep.getInstance();

        Model model = new Model();
        internalContextPropertiesStep.processGetModel(ApplicationProvider.getApplicationContext(), model);

        Map<String, Object> internalProperties = model.getContextProperties();
        assertNotNull(internalProperties);

        /// DYNAMIC
        assertTrue(internalProperties.containsKey("device_timestamp_utc"));
        internalProperties.remove("device_timestamp_utc");

        assertEquals(TestResources.createInternalContextProperties(), internalProperties);
    }
}