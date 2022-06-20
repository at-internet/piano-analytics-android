package io.piano.analytics;

import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.LOLLIPOP})
public class ModelTest {

    @Test
    public void configuration() {
        Model m = new Model();
        Configuration config = m.getConfiguration();
        assertNotNull(config);

        assertNotNull(m.Config(TestResources.createConfiguration()).getConfiguration());
        assertNotSame(config, m.getConfiguration());
    }

    @Test
    public void visitorId() {
        Model m = new Model();
        assertNull(m.getVisitorId());

        String visitorId = m.Config(TestResources.createConfiguration()).getVisitorId();
        assertEquals(TestResources.VISITOR_ID, visitorId);

        m.setVisitorId("itsamemario");
        assertNotSame(visitorId, m.getVisitorId());
        assertEquals("itsamemario", m.getVisitorId());
    }

    @Test
    public void events() {
        Model m = new Model();
        List<Event> events = m.getEvents();
        assertNotNull(events);

        List<Event> newEvents = TestResources.createEventsList();
        assertNotNull(m.setEvents(newEvents).getEvents());
        assertNotSame(events, newEvents);
    }

    @Test
    public void contextProperties() {
        Model m = new Model();
        Map<String, Object> contextProperties = m.getContextProperties();
        assertNotNull(contextProperties);

        assertNotNull(m.setContextProperties(TestResources.createCustomerContextProperties()).getContextProperties());
        assertNotSame(contextProperties, m.getContextProperties());
    }

    @Test
    public void privacyModel() {
        Model m = new Model();
        assertNull(m.getPrivacyModel());

        PrivacyModel privacyModel = new PrivacyModel(TestResources.VISITOR_MODE);
        assertSame(privacyModel, m.setPrivacyModel(privacyModel).getPrivacyModel());
    }

    @Test
    public void builtModel() {
        Model m = new Model();
        assertNull(m.getBuiltModel());

        BuiltModel builtModel = new BuiltModel("uri", "body", false);
        assertSame(builtModel, m.setBuiltModel(builtModel).getBuiltModel());
    }

    @Test
    public void userModel() {
        Model m = new Model();
        assertNull(m.getUserModel());

        UserModel userModel = new UserModel(UserModel.UpdateTypeKey.SET, false);
        assertSame(userModel, m.setUserModel(userModel).getUserModel());
    }

    @Test
    public void storage() {
        Model m = new Model();
        Map<String, BuiltModel> storage = m.getStorage();
        assertNotNull(storage);

        assertNotNull(m.setStorage(TestResources.createStorage()).getStorage());
        assertNotSame(storage, m.getStorage());
    }
}