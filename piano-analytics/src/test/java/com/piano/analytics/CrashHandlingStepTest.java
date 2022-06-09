package com.piano.analytics;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;

import androidx.test.core.app.ApplicationProvider;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.LOLLIPOP})
public class CrashHandlingStepTest {

    private static final Map<String, String> CRASH_PROPERTIES = createCrashProperties();
    private static final SharedPreferences SHARED_PREFERENCES = ApplicationProvider.getApplicationContext().getSharedPreferences(PreferencesKeys.PREFERENCES, Context.MODE_PRIVATE);

    private static Map<String, String> createCrashProperties() {
        Map<String, String> crashProperties = new HashMap<>();
        crashProperties.put(String.format(CrashHandlingStep.APP_CRASH_PROPERTIES_FORMAT, "_screen"), "");
        crashProperties.put(String.format(CrashHandlingStep.APP_CRASH_PROPERTIES_FORMAT, "_class"), "");
        crashProperties.put(String.format(CrashHandlingStep.APP_CRASH_PROPERTIES_FORMAT, ""), "java.lang.NullPointerException");
        return crashProperties;
    }

    @Before
    @After
    public void setup() {
        CrashHandlingStep.getInstance(ApplicationProvider.getApplicationContext(), new PrivacyStep(ApplicationProvider.getApplicationContext(), new Configuration())).setSavedPageProperty("");
        SHARED_PREFERENCES.edit().clear().apply();
    }

    @Test
    public void uncaughtException() {
        CrashHandlingStep crashHandlingStep = CrashHandlingStep.getInstance(ApplicationProvider.getApplicationContext(), new PrivacyStep(ApplicationProvider.getApplicationContext(), new Configuration()));

        assertTrue(SHARED_PREFERENCES.getAll().isEmpty());
        try {
            String s = null;
            s.hashCode();
        } catch (NullPointerException e) {
            crashHandlingStep.uncaughtException(Thread.currentThread(), e);
        }

        assertTrue(SHARED_PREFERENCES.getBoolean(PreferencesKeys.CRASHED, false));
        assertEquals(new JSONObject(CRASH_PROPERTIES).toString(), SHARED_PREFERENCES.getString(PreferencesKeys.CRASH_INFO, null));
    }

    @Test
    public void processSetConfig() {
        CrashHandlingStep crashHandlingStep = CrashHandlingStep.getInstance(ApplicationProvider.getApplicationContext(), new PrivacyStep(ApplicationProvider.getApplicationContext(), new Configuration()));

        assertFalse(Thread.getDefaultUncaughtExceptionHandler() instanceof CrashHandlingStep);

        Model model = new Model()
                .Config(new Configuration.Builder().enableCrashDetection(true).build());
        crashHandlingStep.processSetConfig(model);

        assertTrue(Thread.getDefaultUncaughtExceptionHandler() instanceof CrashHandlingStep);

        model = new Model()
                .Config(new Configuration.Builder().enableCrashDetection(false).build());
        crashHandlingStep.processSetConfig(model);

        assertFalse(Thread.getDefaultUncaughtExceptionHandler() instanceof CrashHandlingStep);
    }

    @Test
    public void processTrackEvents() {
        CrashHandlingStep crashHandlingStep = CrashHandlingStep.getInstance(ApplicationProvider.getApplicationContext(), new PrivacyStep(ApplicationProvider.getApplicationContext(), new Configuration()));

        /// region no crash
        assertEquals("", crashHandlingStep.getSavedPageProperty());

        Model model = new Model()
                .setEvents(TestResources.createEventsList());

        assertTrue(crashHandlingStep.processTrackEvents(ApplicationProvider.getApplicationContext(), model, null));

        assertEquals("myPage2", crashHandlingStep.getSavedPageProperty());
        assertEquals(new HashMap<>(), model.getContextProperties());

        /// endregion

        /// region crash

        try {
            String s = null;
            s.hashCode();
        } catch (NullPointerException e) {
            crashHandlingStep.uncaughtException(Thread.currentThread(), e);
        }

        System.out.println(model.getContextProperties());
        SystemClock.sleep(500);
        assertTrue(crashHandlingStep.processTrackEvents(ApplicationProvider.getApplicationContext(), model, null));
        System.out.println(model.getContextProperties());

        assertEquals("myPage2", crashHandlingStep.getSavedPageProperty());

        HashMap<Object, Object> expected = new HashMap<>();
        expected.put("app_crash", "java.lang.NullPointerException");
        expected.put("app_crash_screen", "myPage2");
        expected.put("app_crash_class", "");
        assertEquals(expected, model.getContextProperties());

        /// endregion
    }
}