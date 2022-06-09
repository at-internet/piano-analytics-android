package com.piano.analytics;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Pair;

import androidx.test.core.app.ApplicationProvider;

import org.bouncycastle.math.raw.Mod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.LOLLIPOP})
public class PrivacyStepTest {

    private static final SharedPreferences SHARED_PREFERENCES = ApplicationProvider.getApplicationContext().getSharedPreferences(PreferencesKeys.PREFERENCES, Context.MODE_PRIVATE);

    @Before
    @After
    public void setup() {
        SHARED_PREFERENCES.edit().clear().apply();
        PrivacyStep.instance = null;
        PrivacyStep privacyStep = PrivacyStep.getInstance(ApplicationProvider.getApplicationContext(), new Configuration());
        privacyStep.setInNoConsent(false);
    }

    @Test
    public void storeDataWithoutAuthorizedStorage() {
        PrivacyStep privacyStep = PrivacyStep.getInstance(ApplicationProvider.getApplicationContext(), new Configuration());
        privacyStep.processUpdatePrivacyContext(new Model().setPrivacyModel(new PrivacyModel(TestResources.VISITOR_MODE, PrivacyModel.UpdateDataKey.NEW_VISITOR_MODE)));
        SHARED_PREFERENCES.edit().putString(PreferencesKeys.PRIVACY_MODE, TestResources.VISITOR_MODE).apply();

        assertEquals(1, SHARED_PREFERENCES.getAll().size());
        privacyStep.storeData(SHARED_PREFERENCES.edit(), PianoAnalytics.PrivacyStorageFeature.LIFECYCLE, new Pair<>("key", "value"));
        assertEquals(1, SHARED_PREFERENCES.getAll().size());
    }

    @Test
    public void storeData() {
        PrivacyStep privacyStep = PrivacyStep.getInstance(ApplicationProvider.getApplicationContext(), new Configuration());
        privacyStep.processUpdatePrivacyContext(new Model().setPrivacyModel(new PrivacyModel(TestResources.VISITOR_MODE, PrivacyModel.UpdateDataKey.NEW_VISITOR_MODE)));

        privacyStep.getVisitorModeAuthorizedStorageFeature(TestResources.VISITOR_MODE).add(PianoAnalytics.PrivacyStorageFeature.PRIVACY);
        assertEquals(1, SHARED_PREFERENCES.getAll().size());
        privacyStep.storeData(SHARED_PREFERENCES.edit(), PianoAnalytics.PrivacyStorageFeature.PRIVACY,
                new Pair<>("todelete", null),
                new Pair<>("key", "value"),
                new Pair<>("bool", true),
                new Pair<>("long", 14L),
                new Pair<>("int", 13));

        assertEquals(5, SHARED_PREFERENCES.getAll().size());
        assertNull(SHARED_PREFERENCES.getString("todelete", null));
        assertTrue(SHARED_PREFERENCES.getBoolean("bool", false));
        assertEquals("value", SHARED_PREFERENCES.getString("key", null));
        assertEquals(14L, SHARED_PREFERENCES.getLong("long", 0));
        assertEquals(13, SHARED_PREFERENCES.getInt("int", 13));
    }

    @Test
    public void isAuthorizedEvent() {
        PrivacyStep privacyStep = PrivacyStep.getInstance(ApplicationProvider.getApplicationContext(), new Configuration());
        String eventName = "test.event";
        Set<String> authorizedEvents = new HashSet<>();
        Set<String> forbiddenEvents = new HashSet<>();

        assertFalse(privacyStep.isAuthorizedEventName(eventName, authorizedEvents, forbiddenEvents));

        authorizedEvents = new HashSet<>(Collections.singletonList("test"));
        assertFalse(privacyStep.isAuthorizedEventName(eventName, authorizedEvents, forbiddenEvents));

        authorizedEvents = new HashSet<>(Collections.singletonList("*"));
        assertTrue(privacyStep.isAuthorizedEventName(eventName, authorizedEvents, forbiddenEvents));

        authorizedEvents = new HashSet<>(Collections.singletonList("test.*"));
        assertTrue(privacyStep.isAuthorizedEventName(eventName, authorizedEvents, forbiddenEvents));

        authorizedEvents = new HashSet<>(Collections.singletonList("test.event"));
        assertTrue(privacyStep.isAuthorizedEventName(eventName, authorizedEvents, forbiddenEvents));


        forbiddenEvents = new HashSet<>(Collections.singletonList("test"));
        assertTrue(privacyStep.isAuthorizedEventName(eventName, authorizedEvents, forbiddenEvents));

        forbiddenEvents = new HashSet<>(Collections.singletonList("*"));
        assertFalse(privacyStep.isAuthorizedEventName(eventName, authorizedEvents, forbiddenEvents));

        forbiddenEvents = new HashSet<>(Collections.singletonList("test.*"));
        assertFalse(privacyStep.isAuthorizedEventName(eventName, authorizedEvents, forbiddenEvents));

        forbiddenEvents = new HashSet<>(Collections.singletonList("test.event"));
        assertFalse(privacyStep.isAuthorizedEventName(eventName, authorizedEvents, forbiddenEvents));
    }

    @Test
    public void getPropertiesFromEventName() {
        PrivacyStep privacyStep = PrivacyStep.getInstance(ApplicationProvider.getApplicationContext(), new Configuration());

        Map<String, Set<String>> propertiesKeysAuthorizedByEventNames = new HashMap<>();
        propertiesKeysAuthorizedByEventNames.put("page.display", new HashSet<>(Arrays.asList("prop1", "prop2")));

        Set<String> propertiesOfEvent = privacyStep.getPropertiesFromEventName(propertiesKeysAuthorizedByEventNames, "page.display");
        assertEquals(2, propertiesOfEvent.size());
        assertTrue(propertiesOfEvent.contains("prop1"));
        assertTrue(propertiesOfEvent.contains("prop2"));

        propertiesOfEvent = privacyStep.getPropertiesFromEventName(propertiesKeysAuthorizedByEventNames, "cart.show");
        assertEquals(0, propertiesOfEvent.size());
    }

    @Test
    public void applyAuthorizedPropertiesPrivacyRules() {
        PrivacyStep privacyStep = PrivacyStep.getInstance(ApplicationProvider.getApplicationContext(), new Configuration());

        Map<String, Object> data = TestResources.createCustomerContextProperties();
        Set<String> authorizedProperties = new HashSet<>();

        assertEquals(0, privacyStep.applyAuthorizedPropertiesPrivacyRules(data, authorizedProperties).size());

        authorizedProperties = new HashSet<>(Collections.singletonList("*"));
        assertEquals(data.size(), privacyStep.applyAuthorizedPropertiesPrivacyRules(data, authorizedProperties).size());

        authorizedProperties = new HashSet<>(Collections.singletonList("prop1*"));
        assertEquals(3, privacyStep.applyAuthorizedPropertiesPrivacyRules(data, authorizedProperties).size());

        authorizedProperties = new HashSet<>(Collections.singletonList("prop1"));
        assertEquals(1, privacyStep.applyAuthorizedPropertiesPrivacyRules(data, authorizedProperties).size());

        authorizedProperties = new HashSet<>(Collections.singletonList("p"));
        assertEquals(0, privacyStep.applyAuthorizedPropertiesPrivacyRules(data, authorizedProperties).size());
    }

    @Test
    public void applyForbiddenPropertiesPrivacyRules() {
        PrivacyStep privacyStep = PrivacyStep.getInstance(ApplicationProvider.getApplicationContext(), new Configuration());

        Map<String, Object> data = TestResources.createCustomerContextProperties();
        Set<String> forbiddenProperties = new HashSet<>();

        assertEquals(data.size(), privacyStep.applyForbiddenPropertiesPrivacyRules(data, forbiddenProperties).size());

        forbiddenProperties = new HashSet<>(Collections.singletonList("*"));
        assertEquals(0, privacyStep.applyForbiddenPropertiesPrivacyRules(data, forbiddenProperties).size());

        forbiddenProperties = new HashSet<>(Collections.singletonList("prop1*"));
        assertEquals(1, privacyStep.applyForbiddenPropertiesPrivacyRules(data, forbiddenProperties).size());

        forbiddenProperties = new HashSet<>(Collections.singletonList("prop1"));
        assertEquals(3, privacyStep.applyForbiddenPropertiesPrivacyRules(data, forbiddenProperties).size());

        forbiddenProperties = new HashSet<>(Collections.singletonList("p"));
        assertEquals(data.size(), privacyStep.applyForbiddenPropertiesPrivacyRules(data, forbiddenProperties).size());
    }

    @Test
    public void processUpdatePrivacyContextStorage() {
        PrivacyStep privacyStep = PrivacyStep.getInstance(ApplicationProvider.getApplicationContext(), new Configuration());

        assertTrue(privacyStep.getVisitorModeAuthorizedStorageFeature(TestResources.VISITOR_MODE).isEmpty());

        Model model = new Model()
                .setPrivacyModel(new PrivacyModel(TestResources.VISITOR_MODE, PrivacyModel.UpdateDataKey.STORAGE)
                        .setAuthorizedStorageFeatures(new HashSet<>(Collections.singleton(PianoAnalytics.PrivacyStorageFeature.LIFECYCLE))));
        privacyStep.processUpdatePrivacyContext(model);

        assertEquals(1, privacyStep.getVisitorModeAuthorizedStorageFeature(TestResources.VISITOR_MODE).size());
        assertTrue(privacyStep.getVisitorModeAuthorizedStorageFeature(TestResources.VISITOR_MODE).contains(PianoAnalytics.PrivacyStorageFeature.LIFECYCLE));
    }

    @Test
    public void processUpdatePrivacyContextAuthorizedProperties() {
        PrivacyStep privacyStep = PrivacyStep.getInstance(ApplicationProvider.getApplicationContext(), new Configuration());

        assertTrue(privacyStep.getVisitorModeAuthorizedProperties(TestResources.VISITOR_MODE).isEmpty());

        Map<String, Set<String>> propertiesByEventName = new HashMap<>();
        propertiesByEventName.put(PrivacyStep.WILDCARD, new HashSet<>(Arrays.asList("PROP1", "prop2")));

        Model model = new Model()
                .setPrivacyModel(new PrivacyModel(TestResources.VISITOR_MODE, PrivacyModel.UpdateDataKey.PROPERTIES)
                        .setAuthorizedPropertyKeys(propertiesByEventName));
        privacyStep.processUpdatePrivacyContext(model);

        Map<String, Set<String>> authorizedPropertiesByEventNames = privacyStep.getVisitorModeAuthorizedProperties(TestResources.VISITOR_MODE);

        assertEquals(1, authorizedPropertiesByEventNames.keySet().size());
        assertNotNull(authorizedPropertiesByEventNames.get(PrivacyStep.WILDCARD));
        assertEquals(2, authorizedPropertiesByEventNames.get(PrivacyStep.WILDCARD).size());
        assertTrue(authorizedPropertiesByEventNames.get(PrivacyStep.WILDCARD).contains("prop1"));
        assertTrue(authorizedPropertiesByEventNames.get(PrivacyStep.WILDCARD).contains("prop2"));
    }

    @Test
    public void processUpdatePrivacyContextForbiddenProperties() {
        PrivacyStep privacyStep = PrivacyStep.getInstance(ApplicationProvider.getApplicationContext(), new Configuration());

        assertTrue(privacyStep.getVisitorModeForbiddenProperties(TestResources.VISITOR_MODE).isEmpty());

        Map<String, Set<String>> propertiesByEventName = new HashMap<>();
        propertiesByEventName.put(PrivacyStep.WILDCARD, new HashSet<>(Arrays.asList("PROP1", "prop2")));

        Model model = new Model()
                .setPrivacyModel(new PrivacyModel(TestResources.VISITOR_MODE, PrivacyModel.UpdateDataKey.PROPERTIES)
                        .setForbiddenPropertyKeys(propertiesByEventName));
        privacyStep.processUpdatePrivacyContext(model);

        Map<String, Set<String>> forbiddenPropertiesByEventNames = privacyStep.getVisitorModeForbiddenProperties(TestResources.VISITOR_MODE);

        assertEquals(1, forbiddenPropertiesByEventNames.keySet().size());
        assertNotNull(forbiddenPropertiesByEventNames.get(PrivacyStep.WILDCARD));
        assertEquals(2, forbiddenPropertiesByEventNames.get(PrivacyStep.WILDCARD).size());
        assertTrue(forbiddenPropertiesByEventNames.get(PrivacyStep.WILDCARD).contains("prop1"));
        assertTrue(forbiddenPropertiesByEventNames.get(PrivacyStep.WILDCARD).contains("prop2"));
    }

    @Test
    public void processUpdatePrivacyContextVisitorMode() {
        PrivacyStep privacyStep = PrivacyStep.getInstance(ApplicationProvider.getApplicationContext(), new Configuration());

        assertEquals(PianoAnalytics.PrivacyVisitorMode.OPTIN.stringValue(), privacyStep.getVisitorMode());

        Model model = new Model()
                .setPrivacyModel(new PrivacyModel(TestResources.VISITOR_MODE, PrivacyModel.UpdateDataKey.VISITOR_MODE)
                        .setDuration(15)
                        .setCustomVisitorId("id")
                        .setVisitorConsent(true));
        privacyStep.processUpdatePrivacyContext(model);

        assertEquals(TestResources.VISITOR_MODE, SHARED_PREFERENCES.getString(PreferencesKeys.PRIVACY_MODE, null));
        assertTrue(SHARED_PREFERENCES.getBoolean(PreferencesKeys.PRIVACY_VISITOR_CONSENT, false));
        assertEquals("id", SHARED_PREFERENCES.getString(PreferencesKeys.PRIVACY_VISITOR_ID, null));
    }

    @Test
    public void processUpdatePrivacyContextVisitorModeNoConsent() {
        PrivacyStep privacyStep = PrivacyStep.getInstance(ApplicationProvider.getApplicationContext(), new Configuration());

        assertEquals(PianoAnalytics.PrivacyVisitorMode.OPTIN.stringValue(), privacyStep.getVisitorMode());

        Model model = new Model()
                .setPrivacyModel(new PrivacyModel(PianoAnalytics.PrivacyVisitorMode.NO_CONSENT.stringValue(), PrivacyModel.UpdateDataKey.VISITOR_MODE));
        privacyStep.processUpdatePrivacyContext(model);

        assertNull(SHARED_PREFERENCES.getString(PreferencesKeys.PRIVACY_MODE, null));
        assertEquals(PianoAnalytics.PrivacyVisitorMode.NO_CONSENT.stringValue(), privacyStep.getVisitorMode());
    }


    @Test
    public void processTrackEventsWithCustomMode() {
        PrivacyStep privacyStep = PrivacyStep.getInstance(ApplicationProvider.getApplicationContext(), new Configuration());
        privacyStep.processUpdatePrivacyContext(new Model().setPrivacyModel(new PrivacyModel(TestResources.VISITOR_MODE, PrivacyModel.UpdateDataKey.NEW_VISITOR_MODE)));

        SHARED_PREFERENCES.edit()
                .putString(PreferencesKeys.PRIVACY_MODE, TestResources.VISITOR_MODE)
                .putString(PreferencesKeys.PRIVACY_VISITOR_ID, "custom")
                .putBoolean(PreferencesKeys.PRIVACY_VISITOR_CONSENT, false)
                .apply();

        Map<String, Set<String>> authorizedPropertiesByEventName = new HashMap<>();
        authorizedPropertiesByEventName.put(TestResources.VISITOR_MODE, new HashSet<>(Arrays.asList("connection_type", "device_screen_diagonal", "app_version")));
        Map<String, Set<String>> forbiddenPropertiesByEventName = new HashMap<>();
        forbiddenPropertiesByEventName.put(TestResources.VISITOR_MODE, new HashSet<>(Collections.singletonList("app_version")));
        Set<String> authorizedEventNames = new HashSet<>();
        authorizedEventNames.add("*");

        Model model = new Model()
                .setPrivacyModel(new PrivacyModel(TestResources.VISITOR_MODE, PrivacyModel.UpdateDataKey.PROPERTIES)
                        .setAuthorizedPropertyKeys(authorizedPropertiesByEventName)
                        .setForbiddenPropertyKeys(forbiddenPropertiesByEventName));
        privacyStep.processUpdatePrivacyContext(model);

        model.setPrivacyModel(new PrivacyModel(TestResources.VISITOR_MODE, PrivacyModel.UpdateDataKey.EVENTS_NAME)
                .setAuthorizedEventsName(authorizedEventNames));
        privacyStep.processUpdatePrivacyContext(model);

        model.addContextProperties(TestResources.createInternalContextProperties())
                .setEvents(TestResources.createEventsList());

        assertTrue(privacyStep.processTrackEvents(ApplicationProvider.getApplicationContext(), model, null));

        Map<String, Object> m = model.getContextProperties();
        assertEquals(10, model.getContextProperties().size());
        assertTrue(model.getContextProperties().containsKey("connection_type"));
        assertTrue(model.getContextProperties().containsKey("device_screen_diagonal"));
        assertEquals(TestResources.VISITOR_MODE, model.getContextProperties().get(PrivacyStep.VISITOR_PRIVACY_MODE_PROPERTY));
        assertFalse((Boolean) model.getContextProperties().get(PrivacyStep.VISITOR_PRIVACY_CONSENT_PROPERTY));

        for (Event e : model.getEvents()) {
            assertFalse(e.getName().isEmpty());
            assertTrue(e.getData().isEmpty());
        }

        assertEquals("custom", model.getVisitorId());
    }

    @Test
    public void processTrackEventsWithNoConsentMode() {
        PrivacyStep privacyStep = PrivacyStep.getInstance(ApplicationProvider.getApplicationContext(), new Configuration());
        privacyStep.setInNoConsent(true);
        SHARED_PREFERENCES.edit()
                .putString(PreferencesKeys.PRIVACY_MODE, PianoAnalytics.PrivacyVisitorMode.NO_CONSENT.stringValue())
                .putString(PreferencesKeys.PRIVACY_VISITOR_ID, "custom")
                .apply();

        Model model = new Model()
                .addContextProperties(TestResources.createInternalContextProperties())
                .setEvents(TestResources.createEventsList());

        assertTrue(privacyStep.processTrackEvents(ApplicationProvider.getApplicationContext(), model, null));

        assertEquals(3, model.getContextProperties().size());
        assertEquals("UNKNOWN", model.getContextProperties().get("connection_type"));
        assertEquals("no-consent", model.getContextProperties().get(PrivacyStep.VISITOR_PRIVACY_MODE_PROPERTY));
        assertFalse((Boolean) model.getContextProperties().get(PrivacyStep.VISITOR_PRIVACY_CONSENT_PROPERTY));

        for (Event e : model.getEvents()) {
            assertFalse(e.getName().isEmpty());
            assertTrue(e.getData().isEmpty());
        }

        assertEquals("Consent-NO", model.getVisitorId());
    }

    @Test
    public void processTrackEventsWithOptOutMode() {
        PrivacyStep privacyStep = PrivacyStep.getInstance(ApplicationProvider.getApplicationContext(), new Configuration());
        SHARED_PREFERENCES.edit()
                .putString(PreferencesKeys.PRIVACY_MODE, PianoAnalytics.PrivacyVisitorMode.OPTOUT.stringValue())
                .putString(PreferencesKeys.PRIVACY_VISITOR_ID, "custom")
                .apply();

        Model model = new Model()
                .Config(new Configuration.Builder().enableSendEventWhenOptOut(true).build())
                .addContextProperties(TestResources.createInternalContextProperties())
                .setEvents(TestResources.createEventsList());

        assertTrue(privacyStep.processTrackEvents(ApplicationProvider.getApplicationContext(), model, null));

        assertEquals(3, model.getContextProperties().size());
        assertEquals("UNKNOWN", model.getContextProperties().get("connection_type"));
        assertEquals("optout", model.getContextProperties().get(PrivacyStep.VISITOR_PRIVACY_MODE_PROPERTY));
        assertFalse((Boolean) model.getContextProperties().get(PrivacyStep.VISITOR_PRIVACY_CONSENT_PROPERTY));

        for (Event e : model.getEvents()) {
            assertFalse(e.getName().isEmpty());
            assertTrue(e.getData().isEmpty());
        }

        assertEquals("opt-out", model.getVisitorId());
    }

    @Test
    public void processTrackEventsWithExemptMode() {
        PrivacyStep privacyStep = PrivacyStep.getInstance(ApplicationProvider.getApplicationContext(), new Configuration());
        SHARED_PREFERENCES.edit()
                .putString(PreferencesKeys.PRIVACY_MODE, PianoAnalytics.PrivacyVisitorMode.EXEMPT.stringValue())
                .putString(PreferencesKeys.PRIVACY_VISITOR_ID, "custom")
                .apply();
        Map<String, Object> event1Data = new HashMap<>();
        event1Data.put("pages", "myPage2");
        event1Data.put("ev2prop1", "test value");
        Map<String, Object> event2Data = new HashMap<>();
        event2Data.put("pages", "myPage2");
        event2Data.put("ev2prop1", "test value");
        List<Event> events = Arrays.asList(new Event("page.display", event1Data), new Event("click.action", event2Data));

        Model model = new Model()
                .addContextProperties(TestResources.createInternalContextProperties())
                .setEvents(events);

        assertTrue(privacyStep.processTrackEvents(ApplicationProvider.getApplicationContext(), model, null));

        assertEquals(10, model.getContextProperties().size());
        assertEquals("UNKNOWN", model.getContextProperties().get("connection_type"));
        assertEquals("exempt", model.getContextProperties().get(PrivacyStep.VISITOR_PRIVACY_MODE_PROPERTY));
        assertFalse((Boolean) model.getContextProperties().get(PrivacyStep.VISITOR_PRIVACY_CONSENT_PROPERTY));

        for (Event e : model.getEvents()) {
            assertFalse(e.getName().isEmpty());
            assertTrue(e.getData().isEmpty());
        }

        assertNull(model.getVisitorId());
    }

    @Test
    public void processTrackEventsWithOptInMode() {
        PrivacyStep privacyStep = PrivacyStep.getInstance(ApplicationProvider.getApplicationContext(), new Configuration());
        SHARED_PREFERENCES.edit()
                .putString(PreferencesKeys.PRIVACY_MODE, PianoAnalytics.PrivacyVisitorMode.OPTIN.stringValue())
                .putString(PreferencesKeys.PRIVACY_VISITOR_ID, "custom")
                .apply();
        Model model = new Model()
                .addContextProperties(TestResources.createInternalContextProperties())
                .setEvents(TestResources.createEventsList());

        assertTrue(privacyStep.processTrackEvents(ApplicationProvider.getApplicationContext(), model, null));

        assertEquals(17, model.getContextProperties().size());
        assertEquals("optin", model.getContextProperties().remove(PrivacyStep.VISITOR_PRIVACY_MODE_PROPERTY));
        assertTrue((Boolean) model.getContextProperties().remove(PrivacyStep.VISITOR_PRIVACY_CONSENT_PROPERTY));
        assertEquals(TestResources.createInternalContextProperties(), model.getContextProperties());

        for (Event e : model.getEvents()) {
            assertFalse(e.getName().isEmpty());
            assertFalse(e.getData().isEmpty());
        }

        assertNull(model.getVisitorId());
    }

    @Test
    public void processTrackEventsWithoutSendInOptOutMode() {
        PrivacyStep privacyStep = PrivacyStep.getInstance(ApplicationProvider.getApplicationContext(), new Configuration());

        SHARED_PREFERENCES.edit().putString(PreferencesKeys.PRIVACY_MODE, PianoAnalytics.PrivacyVisitorMode.OPTOUT.stringValue()).apply();
        Model model = new Model()
                .Config(new Configuration.Builder().enableSendEventWhenOptOut(false).build());

        assertFalse(privacyStep.processTrackEvents(ApplicationProvider.getApplicationContext(), model, null));
    }

    @Test
    public void processGetModel() {
        Model model = new Model();

        PrivacyStep.getInstance(ApplicationProvider.getApplicationContext(), new Configuration())
                .processGetModel(ApplicationProvider.getApplicationContext(), model);

        assertNotNull(model.getPrivacyModel());
    }
}
