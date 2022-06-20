package io.piano.analytics;

import android.content.Context;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.LOLLIPOP})
public class OnBeforeBuildCallStepTest {

    private final Context ctx = ApplicationProvider.getApplicationContext();

    @Test
    public void processTrackEvents() {
        Map<String, Object> testInternalContextProperties = TestResources.createInternalContextProperties();
        OnBeforeBuildCallStep onBeforeBuildCallStep = OnBeforeBuildCallStep.getInstance();

        Model model = new Model()
                .addContextProperties(testInternalContextProperties);

        assertTrue(onBeforeBuildCallStep.processTrackEvents(this.ctx, model, null));
        assertEquals(testInternalContextProperties, model.getContextProperties());

        assertTrue(onBeforeBuildCallStep.processTrackEvents(this.ctx, model, new PianoAnalytics.OnWorkListener() {
            @Override
            public boolean onBeforeBuild(Model model) {
                return true;
            }
        }));

        assertFalse(onBeforeBuildCallStep.processTrackEvents(this.ctx, model, new PianoAnalytics.OnWorkListener() {
            @Override
            public boolean onBeforeBuild(Model model) {
                return false;
            }
        }));

        assertTrue(model.getContextProperties().containsKey(OnBeforeBuildCallStep.CALLBACK_USED_PROPERTY));
        model.getContextProperties().remove(OnBeforeBuildCallStep.CALLBACK_USED_PROPERTY);

        assertEquals(testInternalContextProperties, model.getContextProperties());
    }
}