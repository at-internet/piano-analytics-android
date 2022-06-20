package io.piano.analytics;

import android.os.Build;

import org.junit.Test;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import io.piano.analytics.Configuration;
import io.piano.analytics.Crypt;

@Config(sdk = {Build.VERSION_CODES.LOLLIPOP_MR1})
public class CryptAPI22Test {

    @Test
    public void encrypt() {
        assertNull(Crypt.encrypt(null, Configuration.EncryptionMode.NONE));
        assertNull(Crypt.encrypt("", Configuration.EncryptionMode.NONE));
        assertEquals("example", Crypt.encrypt("example", Configuration.EncryptionMode.NONE));

        assertNull(Crypt.encrypt(null, Configuration.EncryptionMode.IF_COMPATIBLE));
        assertNull(Crypt.encrypt("", Configuration.EncryptionMode.IF_COMPATIBLE));
        assertEquals("example", Crypt.encrypt("example", Configuration.EncryptionMode.IF_COMPATIBLE));

        assertNull(Crypt.encrypt(null, Configuration.EncryptionMode.FORCE));
        assertNull(Crypt.encrypt("", Configuration.EncryptionMode.FORCE));
        assertNull(Crypt.encrypt("example", Configuration.EncryptionMode.FORCE));
    }

    @Test
    public void decrypt() {
        assertNull(Crypt.decrypt(null));
        assertNull(Crypt.decrypt(""));
        assertEquals("example", Crypt.decrypt("example"));
    }
}