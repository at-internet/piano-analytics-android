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

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import io.piano.analytics.CastUtils;
import io.piano.analytics.Configuration;
import io.piano.analytics.Model;
import io.piano.analytics.PianoAnalytics;
import io.piano.analytics.PreferencesKeys;
import io.piano.analytics.PrivacyStep;
import io.piano.analytics.User;
import io.piano.analytics.UserModel;
import io.piano.analytics.UsersStep;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.LOLLIPOP})
public class UsersStepTest {

    private static final SharedPreferences SHARED_PREFERENCES = ApplicationProvider.getApplicationContext().getSharedPreferences(PreferencesKeys.PREFERENCES, Context.MODE_PRIVATE);

    @Before
    @After
    public void setup() {
        SHARED_PREFERENCES.edit()
                .clear()
                .apply();
        UsersStep.instance = null;
        PrivacyStep.instance = null;
    }

    @Test
    public void processUpdateContextNull() {
        UsersStep usersStep = UsersStep.getInstance(ApplicationProvider.getApplicationContext(), new PrivacyStep(ApplicationProvider.getApplicationContext(), new Configuration()), new Configuration());
        usersStep.processUpdateContext(new Model());
        assertNull(usersStep.getUser());
    }

    @Test
    public void processUpdateContextWithStorageAndConfigurationSiteId() {
        int siteId = 654321;
        UsersStep usersStep = UsersStep.getInstance(ApplicationProvider.getApplicationContext(), new PrivacyStep(ApplicationProvider.getApplicationContext(), new Configuration()), new Configuration());

        /// Set
        Model model = new Model()
                .Config(new Configuration.Builder().withSite(siteId).build())
                .setUserModel(new UserModel(UserModel.UpdateTypeKey.SET, true).setUser(new User("id")));

        usersStep.processUpdateContext(model);

        /// Active
        User u = usersStep.getUser();
        assertEquals("id", u.getId());
        assertNull(u.getCategory());

        assertFalse(usersStep.getUserRecognition());

        /// Delete
        model = new Model()
                .Config(new Configuration.Builder().withSite(siteId).build())
                .setUserModel(new UserModel(UserModel.UpdateTypeKey.DELETE, true));

        usersStep.processUpdateContext(model);

        /// Active
        u = usersStep.getUser();
        assertNull(u);

        assertFalse(usersStep.getUserRecognition());
    }

    @Test
    public void processUpdateContextWithStorageAndSpecificSiteId() {
        int storageSiteId = 123456;
        String storageSiteIdStr = CastUtils.toString(storageSiteId);
        UsersStep usersStep = UsersStep.getInstance(ApplicationProvider.getApplicationContext(), new PrivacyStep(ApplicationProvider.getApplicationContext(), new Configuration()), new Configuration());

        /// Set
        Model model = new Model()
                .Config(new Configuration.Builder().build())
                .setUserModel(new UserModel(UserModel.UpdateTypeKey.SET, true).setUser(new User("id")));

        usersStep.processUpdateContext(model);

        /// Active
        User u = usersStep.getUser();
        assertEquals("id", u.getId());
        assertNull(u.getCategory());

        assertFalse(usersStep.getUserRecognition());

        /// Delete
        model = new Model()
                .Config(new Configuration.Builder().build())
                .setUserModel(new UserModel(UserModel.UpdateTypeKey.DELETE, true));

        usersStep.processUpdateContext(model);

        /// Active
        u = usersStep.getUser();
        assertNull(u);

        assertFalse(usersStep.getUserRecognition());
    }

    @Test
    public void processUpdateContextWithoutStorage() {
        int storageSiteId = 123456;
        String storageSiteIdStr = CastUtils.toString(storageSiteId);
        UsersStep usersStep = UsersStep.getInstance(ApplicationProvider.getApplicationContext(), new PrivacyStep(ApplicationProvider.getApplicationContext(), new Configuration()), new Configuration());

        /// Set
        Model model = new Model()
                .setUserModel(new UserModel(UserModel.UpdateTypeKey.SET, false).setUser(new User("id")));

        usersStep.processUpdateContext(model);

        /// Active
        User u = usersStep.getUser();
        assertEquals("id", u.getId());
        assertNull(u.getCategory());

        assertFalse(usersStep.getUserRecognition());

        /// Delete
        model = new Model()
                .setUserModel(new UserModel(UserModel.UpdateTypeKey.DELETE, false));

        usersStep.processUpdateContext(model);

        /// Active
        u = usersStep.getUser();
        assertNull(u);

        assertFalse(usersStep.getUserRecognition());
    }

    @Test
    public void processUpdateContextWithHybridStorage() {
        int storageSiteId = 123456;
        String storageSiteIdStr = CastUtils.toString(storageSiteId);
        UsersStep usersStep = UsersStep.getInstance(ApplicationProvider.getApplicationContext(), new PrivacyStep(ApplicationProvider.getApplicationContext(), new Configuration()), new Configuration());

        /// Set
        Model model = new Model()
                .Config(new Configuration.Builder().build())
                .setUserModel(new UserModel(UserModel.UpdateTypeKey.SET, false).setUser(new User("id")));

        usersStep.processUpdateContext(model);

        /// Active
        User u = usersStep.getUser();
        assertEquals("id", u.getId());
        assertNull(u.getCategory());

        assertFalse(usersStep.getUserRecognition());

        /// Delete
        model = new Model()
                .setUserModel(new UserModel(UserModel.UpdateTypeKey.DELETE, false));

        usersStep.processUpdateContext(model);

        /// Active
        u = usersStep.getUser();
        assertNull(u);

        assertFalse(usersStep.getUserRecognition());
    }

    @Test
    public void processGetModel() {
        SHARED_PREFERENCES.edit().putString(PreferencesKeys.USER, new User("id").toJSONString()).apply();

        PrivacyStep ps = PrivacyStep.getInstance(ApplicationProvider.getApplicationContext(), new Configuration());
        String userStr = ps.getData(SHARED_PREFERENCES, PianoAnalytics.PrivacyStorageFeature.USER, PreferencesKeys.USER, "", "");

        UsersStep usersStep = UsersStep.getInstance(ApplicationProvider.getApplicationContext(), new PrivacyStep(ApplicationProvider.getApplicationContext(), new Configuration()), new Configuration());
        usersStep.getUser();

        Model model = new Model();
        usersStep.processGetModel(ApplicationProvider.getApplicationContext(), model);

        /// Active
        User activeUsers = model.getStoredUser();
        assertNotNull(activeUsers);
        assertEquals("id", activeUsers.getId());
        assertNull(activeUsers.getCategory());
    }

    @Test
    public void processTrackEventsFromActiveUser() {
        int siteId = 123456;

        UsersStep usersStep = UsersStep.getInstance(ApplicationProvider.getApplicationContext(), new PrivacyStep(ApplicationProvider.getApplicationContext(), new Configuration()), new Configuration());

        User user = new User("id").setCategory("cat");
        Model model = new Model().setUser(user)
                .Config(new Configuration.Builder().withSite(siteId).build()).setUserModel(new UserModel(UserModel.UpdateTypeKey.SET, true).setUser(user));
        usersStep.processUpdateContext(model);
        usersStep.processTrackEvents(ApplicationProvider.getApplicationContext(), model, null);

        /// Active
        User activeUsers = model.getStoredUser();
        assertNotNull(activeUsers);
        assertEquals("id", activeUsers.getId());
        assertEquals("cat", activeUsers.getCategory());

        /// Properties
        Map<String, Object> contextProperties = model.getContextProperties();
        assertEquals(3, contextProperties.size());
        assertEquals("id", contextProperties.get("user_id"));
        assertEquals("cat", contextProperties.get("user_category"));
        assertEquals(false, contextProperties.get("user_recognition"));
    }

    @Test
    public void processTrackEventsFromStoredUser() {
        int siteId = 123456;

        SHARED_PREFERENCES.edit().putString(PreferencesKeys.USER, new User("id").toJSONString()).apply();

        UsersStep usersStep = UsersStep.getInstance(ApplicationProvider.getApplicationContext(), new PrivacyStep(ApplicationProvider.getApplicationContext(), new Configuration()), new Configuration());

        Model model = new Model()
                .Config(new Configuration.Builder().withSite(siteId).build());
        usersStep.processTrackEvents(ApplicationProvider.getApplicationContext(), model, null);

        /// Active
        assertNotNull(model.getStoredUser());

        /// Properties
        Map<String, Object> contextProperties = model.getContextProperties();
        assertEquals(2, contextProperties.size());
        assertEquals("id", contextProperties.get("user_id"));
        assertTrue((Boolean) contextProperties.get("user_recognition"));
    }
}
