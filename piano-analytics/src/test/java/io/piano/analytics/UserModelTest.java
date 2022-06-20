package io.piano.analytics;

import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import io.piano.analytics.User;
import io.piano.analytics.UserModel;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.LOLLIPOP})
public class UserModelTest {

    @Test
    public void getUpdateRequestKey() {
        assertNull(new UserModel(null, false).getUpdateRequestKey());
        assertEquals(UserModel.UpdateTypeKey.SET, new UserModel(UserModel.UpdateTypeKey.SET, false).getUpdateRequestKey());
    }

    @Test
    public void getStorageSiteId() {
        assertEquals(123456, new UserModel(null, false).getEnableStorage());
    }

    @Test
    public void user() {
        User user = new User("id");
        assertSame(user, new UserModel(null, false).setUser(user).getUser());
    }
}