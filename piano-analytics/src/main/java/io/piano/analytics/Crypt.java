package io.piano.analytics;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

final class Crypt {

    /// region Constructors

    private Crypt() {
    }

    /// endregion

    /// region Constants

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String PROVIDER = "AndroidKeyStore";
    private static final String ALIAS = "pa.encryption.key";
    private static final int IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    /// endregion

    /// region Private Method

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static SecretKey getKey() throws KeyStoreException,
            CertificateException,
            NoSuchAlgorithmException,
            IOException,
            UnrecoverableEntryException,
            NoSuchProviderException,
            InvalidAlgorithmParameterException {

        KeyStore keyStore = KeyStore.getInstance(PROVIDER);
        keyStore.load(null);

        if (keyStore.containsAlias(ALIAS)) {
            KeyStore.Entry keyEntry = keyStore.getEntry(ALIAS, null);
            if (keyEntry instanceof KeyStore.SecretKeyEntry) {
                return ((KeyStore.SecretKeyEntry) keyEntry).getSecretKey();
            }
        }

        KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER);
        keyGenerator.init(new KeyGenParameterSpec.Builder(ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build());

        return keyGenerator.generateKey();
    }

    /// endregion

    /// region Package methods

    static String encrypt(String data, Configuration.EncryptionMode encryptionMode) {
        if (PianoAnalyticsUtils.isEmptyString(data)) {
            return null;
        }

        if (encryptionMode == Configuration.EncryptionMode.NONE) {
            return data;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Cipher cipher = Cipher.getInstance(TRANSFORMATION);
                cipher.init(Cipher.ENCRYPT_MODE, getKey());

                byte[] iv = cipher.getIV();
                byte[] cipherText = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

                byte[] encrypted = new byte[iv.length + cipherText.length];
                System.arraycopy(iv, 0, encrypted, 0, iv.length);
                System.arraycopy(cipherText, 0, encrypted, iv.length, cipherText.length);

                return Base64.encodeToString(encrypted, Base64.DEFAULT);
            } catch (Exception e) {
                PianoAnalytics.InternalLogger.warning("error on Crypt.encrypt : " + e.toString());
            }
        }

        /// if force, we don't use original data
        return encryptionMode == Configuration.EncryptionMode.IF_COMPATIBLE ? data : null;
    }

    static String decrypt(String data) {
        if (PianoAnalyticsUtils.isEmptyString(data)) {
            return null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Cipher cipher = Cipher.getInstance(TRANSFORMATION);
                byte[] encryptedData = Base64.decode(data, Base64.DEFAULT);

                cipher.init(Cipher.DECRYPT_MODE, getKey(), new GCMParameterSpec(GCM_TAG_LENGTH, encryptedData, 0, IV_LENGTH));
                return new String(cipher.doFinal(encryptedData, IV_LENGTH, encryptedData.length - IV_LENGTH), StandardCharsets.UTF_8);
            } catch (Exception e) {
                PianoAnalytics.InternalLogger.warning("error on Crypt.decrypt : " + e.toString());
            }
        }
        return data;
    }

    /// endregion
}
