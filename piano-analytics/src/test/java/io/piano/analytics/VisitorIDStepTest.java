package io.piano.analytics;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.LOLLIPOP})
public class VisitorIDStepTest {

    private final Context ctx = ApplicationProvider.getApplicationContext();
    private static final SharedPreferences SHARED_PREFERENCES = ApplicationProvider.getApplicationContext().getSharedPreferences(PreferencesKeys.PREFERENCES, Context.MODE_PRIVATE);

    @Before
    @After
    public void setup() {
        SHARED_PREFERENCES.edit()
                .clear()
                .putString(PreferencesKeys.PRIVACY_MODE, PianoAnalytics.PrivacyVisitorMode.EXEMPT.stringValue())
                .apply();
    }

    @Test
    public void processTrackEventsWithUUID() {
        int uuidDuration = 3;

        VisitorIDStep visitorIDStep = VisitorIDStep.getInstance(new PrivacyStep(this.ctx, new Configuration()));
        Model model = new Model()
                .Config(new Configuration.Builder()
                        .withVisitorIDType(Configuration.VisitorIDType.UUID)
                        .withVisitorStorageMode(Configuration.VisitorStorageMode.FIXED)
                        .withStorageLifetimeVisitor(uuidDuration)
                        .build());

        /// New
        long savedGenerationTimestamp = SHARED_PREFERENCES.getLong(PreferencesKeys.VISITOR_UUID_GENERATION_TIMESTAMP, -1);
        assertTrue(visitorIDStep.processTrackEvents(this.ctx, model, null));
        String visitorId = model.getVisitorId();
        assertEquals(Configuration.VisitorIDType.UUID.stringValue(), model.getContextProperties().get(VisitorIDStep.VISITOR_ID_TYPE_PROPERTY));
        assertFalse(visitorId.isEmpty());
        assertNotEquals(savedGenerationTimestamp, SHARED_PREFERENCES.getLong(PreferencesKeys.VISITOR_UUID_GENERATION_TIMESTAMP, -1));

        /// Existing
        savedGenerationTimestamp = SHARED_PREFERENCES.getLong(PreferencesKeys.VISITOR_UUID_GENERATION_TIMESTAMP, -1);
        assertTrue(visitorIDStep.processTrackEvents(this.ctx, model, null));
        assertEquals(Configuration.VisitorIDType.UUID.stringValue(), model.getContextProperties().get(VisitorIDStep.VISITOR_ID_TYPE_PROPERTY));
        assertEquals(visitorId, model.getVisitorId());
        assertEquals(savedGenerationTimestamp, SHARED_PREFERENCES.getLong(PreferencesKeys.VISITOR_UUID_GENERATION_TIMESTAMP, 0));

        /// Expired
        long generationTimestamp = new Date().getTime() - (86_400_000 * (uuidDuration + 1));
        SHARED_PREFERENCES.edit().putLong(PreferencesKeys.VISITOR_UUID_GENERATION_TIMESTAMP, generationTimestamp).apply();

        assertTrue(visitorIDStep.processTrackEvents(this.ctx, model, null));
        assertEquals(Configuration.VisitorIDType.UUID.stringValue(), model.getContextProperties().get(VisitorIDStep.VISITOR_ID_TYPE_PROPERTY));
        assertNotEquals(visitorId, model.getVisitorId());

        visitorId = model.getVisitorId();
        assertFalse(visitorId.isEmpty());
        assertNotEquals(generationTimestamp, SHARED_PREFERENCES.getLong(PreferencesKeys.VISITOR_UUID_GENERATION_TIMESTAMP, generationTimestamp));

        /// Existing with relative mode
        model.getConfiguration().set(Configuration.ConfigurationKey.VISITOR_STORAGE_MODE, Configuration.VisitorStorageMode.RELATIVE.stringValue());
        generationTimestamp = new Date().getTime() - (86_400_000 * (uuidDuration - 1));
        SHARED_PREFERENCES.edit().putLong(PreferencesKeys.VISITOR_UUID_GENERATION_TIMESTAMP, generationTimestamp).apply();

        assertTrue(visitorIDStep.processTrackEvents(this.ctx, model, null));
        assertEquals(Configuration.VisitorIDType.UUID.stringValue(), model.getContextProperties().get(VisitorIDStep.VISITOR_ID_TYPE_PROPERTY));
        assertEquals(visitorId, model.getVisitorId());
        assertNotEquals(generationTimestamp, SHARED_PREFERENCES.getLong(PreferencesKeys.VISITOR_UUID_GENERATION_TIMESTAMP, -1));
    }

    @Test
    public void processTrackEventsWithAndroidID() {
        VisitorIDStep visitorIDStep = VisitorIDStep.getInstance(new PrivacyStep(this.ctx, new Configuration()));
        Model model = new Model()
                .Config(new Configuration.Builder()
                        .withVisitorIDType(Configuration.VisitorIDType.ANDROID_ID)
                        .build());

        assertTrue(visitorIDStep.processTrackEvents(this.ctx, model, null));
        assertEquals(Configuration.VisitorIDType.ANDROID_ID.stringValue(), model.getContextProperties().get(VisitorIDStep.VISITOR_ID_TYPE_PROPERTY));
        assertNull(model.getVisitorId());
    }

    @Test
    public void processTrackEventsWithAdvertisingID() {
        VisitorIDStep visitorIDStep = VisitorIDStep.getInstance(new PrivacyStep(this.ctx, new Configuration()));
        Model model = new Model()
                .Config(new Configuration.Builder()
                        .withVisitorIDType(Configuration.VisitorIDType.ADVERTISING_ID)
                        .build());

        assertTrue(visitorIDStep.processTrackEvents(this.ctx, model, null));
        assertEquals(Configuration.VisitorIDType.ADVERTISING_ID.stringValue(), model.getContextProperties().get(VisitorIDStep.VISITOR_ID_TYPE_PROPERTY));
        assertNull(model.getVisitorId());

        model.getConfiguration().set(Configuration.ConfigurationKey.VISITOR_ID_TYPE, Configuration.VisitorIDType.GOOGLE_ADVERTISING_ID.stringValue());
        assertTrue(visitorIDStep.processTrackEvents(this.ctx, model, null));
        assertEquals(Configuration.VisitorIDType.GOOGLE_ADVERTISING_ID.stringValue(), model.getContextProperties().get(VisitorIDStep.VISITOR_ID_TYPE_PROPERTY));
        assertNull(model.getVisitorId());

        model.getConfiguration().set(Configuration.ConfigurationKey.VISITOR_ID_TYPE, Configuration.VisitorIDType.HUAWEI_OPEN_ADVERTISING_ID.stringValue());
        assertTrue(visitorIDStep.processTrackEvents(this.ctx, model, null));
        assertEquals(Configuration.VisitorIDType.HUAWEI_OPEN_ADVERTISING_ID.stringValue(), model.getContextProperties().get(VisitorIDStep.VISITOR_ID_TYPE_PROPERTY));
        assertNull(model.getVisitorId());
    }

    @Test
    public void processTrackEventsWithCustomID() {
        VisitorIDStep visitorIDStep = VisitorIDStep.getInstance(new PrivacyStep(this.ctx, new Configuration()));
        Model model = new Model()
                .Config(new Configuration.Builder()
                        .withVisitorIDType(Configuration.VisitorIDType.CUSTOM)
                        .withVisitorID(TestResources.VISITOR_ID)
                        .build());

        assertTrue(visitorIDStep.processTrackEvents(this.ctx, model, null));
        assertEquals(Configuration.VisitorIDType.CUSTOM.stringValue(), model.getContextProperties().get(VisitorIDStep.VISITOR_ID_TYPE_PROPERTY));
        assertEquals(TestResources.VISITOR_ID, model.getVisitorId());
    }
}