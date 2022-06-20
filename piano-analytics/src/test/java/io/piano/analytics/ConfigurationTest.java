package io.piano.analytics;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ConfigurationTest {

    @Test
    public void constructor() {
        Configuration config = new Configuration(null);
        assertTrue(config.isEmpty());

        Map<String, Object> newConfig = new HashMap<>();
        newConfig.put("key", "value");
        config = new Configuration(newConfig);
        assertFalse(config.isEmpty());
    }

    @Test
    public void configurationKeyEnumFromString() {
        assertEquals(Configuration.ConfigurationKey.COLLECT_DOMAIN, Configuration.ConfigurationKey.fromString("CollectDomain"));
        assertEquals(Configuration.ConfigurationKey.SITE, Configuration.ConfigurationKey.fromString("Site"));
        assertEquals(Configuration.ConfigurationKey.PATH, Configuration.ConfigurationKey.fromString("Path"));
        assertNull(Configuration.ConfigurationKey.fromString(null));
        assertNull(Configuration.ConfigurationKey.fromString(""));
        assertNull(Configuration.ConfigurationKey.fromString("undefined"));
    }

    @Test
    public void encryptionModeEnumFromString() {
        assertEquals(Configuration.EncryptionMode.FORCE, Configuration.EncryptionMode.fromString("Force"));
        assertEquals(Configuration.EncryptionMode.IF_COMPATIBLE, Configuration.EncryptionMode.fromString("IfCompatible"));
        assertEquals(Configuration.EncryptionMode.NONE, Configuration.EncryptionMode.fromString("None"));

        assertEquals(Configuration.EncryptionMode.IF_COMPATIBLE, Configuration.EncryptionMode.fromString(null));
        assertEquals(Configuration.EncryptionMode.IF_COMPATIBLE, Configuration.EncryptionMode.fromString(""));
        assertEquals(Configuration.EncryptionMode.IF_COMPATIBLE, Configuration.EncryptionMode.fromString("undefined"));
    }

    @Test
    public void offlineModeEnumFromString() {
        assertEquals(Configuration.OfflineStorageMode.ALWAYS, Configuration.OfflineStorageMode.fromString("Always"));
        assertEquals(Configuration.OfflineStorageMode.REQUIRED, Configuration.OfflineStorageMode.fromString("Required"));
        assertEquals(Configuration.OfflineStorageMode.NEVER, Configuration.OfflineStorageMode.fromString("Never"));

        assertEquals(Configuration.OfflineStorageMode.NEVER, Configuration.OfflineStorageMode.fromString(null));
        assertEquals(Configuration.OfflineStorageMode.NEVER, Configuration.OfflineStorageMode.fromString(""));
        assertEquals(Configuration.OfflineStorageMode.NEVER, Configuration.OfflineStorageMode.fromString("undefined"));
    }

    @Test
    public void visitorIDTypeEnumFromString() {
        assertEquals(Configuration.VisitorIDType.ADVERTISING_ID, Configuration.VisitorIDType.fromString("advertisingID"));
        assertEquals(Configuration.VisitorIDType.ANDROID_ID, Configuration.VisitorIDType.fromString("AndroidId"));
        assertEquals(Configuration.VisitorIDType.GOOGLE_ADVERTISING_ID, Configuration.VisitorIDType.fromString("googleAdvertisingId"));
        assertEquals(Configuration.VisitorIDType.HUAWEI_OPEN_ADVERTISING_ID, Configuration.VisitorIDType.fromString("huaweiOpenAdvertisingID"));
        assertEquals(Configuration.VisitorIDType.UUID, Configuration.VisitorIDType.fromString("UUID"));
        assertEquals(Configuration.VisitorIDType.CUSTOM, Configuration.VisitorIDType.fromString("Custom"));

        assertEquals(Configuration.VisitorIDType.UUID, Configuration.VisitorIDType.fromString(null));
        assertEquals(Configuration.VisitorIDType.UUID, Configuration.VisitorIDType.fromString(""));
        assertEquals(Configuration.VisitorIDType.UUID, Configuration.VisitorIDType.fromString("undefined"));
    }

    @Test
    public void UUIDExpirationModeEnumFromString() {
        assertEquals(Configuration.VisitorStorageMode.FIXED, Configuration.VisitorStorageMode.fromString("fixed"));
        assertEquals(Configuration.VisitorStorageMode.RELATIVE, Configuration.VisitorStorageMode.fromString("relative"));

        assertEquals(Configuration.VisitorStorageMode.FIXED, Configuration.VisitorStorageMode.fromString(null));
        assertEquals(Configuration.VisitorStorageMode.FIXED, Configuration.VisitorStorageMode.fromString(""));
        assertEquals(Configuration.VisitorStorageMode.FIXED, Configuration.VisitorStorageMode.fromString("undefined"));
    }

    @Test
    public void getRootConfiguration() {
        Configuration rootConfiguration = TestResources.createConfiguration().getRootConfiguration();

        assertEquals(6, rootConfiguration.size());
        assertTrue(rootConfiguration.containsKey(Configuration.ConfigurationKey.COLLECT_DOMAIN));
        assertTrue(rootConfiguration.containsKey(Configuration.ConfigurationKey.PATH));
        assertTrue(rootConfiguration.containsKey(Configuration.ConfigurationKey.SITE));
        assertTrue(rootConfiguration.containsKey(Configuration.ConfigurationKey.VISITOR_ID));
        assertTrue(rootConfiguration.containsKey(Configuration.ConfigurationKey.VISITOR_ID_TYPE));
        assertTrue(rootConfiguration.containsKey(Configuration.ConfigurationKey.CUSTOM_USER_AGENT));
    }

    @Test
    public void withCollectEndpoint() {
        Configuration.ConfigurationKey key = Configuration.ConfigurationKey.COLLECT_DOMAIN;
        assertNull(getDefaultValue(key));
        assertEquals("custom_endpoint", new Configuration.Builder().withCollectDomain("custom_endpoint").build().get(key));
    }

    @Test
    public void withSiteID() {
        Configuration.ConfigurationKey key = Configuration.ConfigurationKey.SITE;
        assertNull(getDefaultValue(key));
        assertEquals("1", new Configuration.Builder().withSite(1).build().get(key));
    }

    @Test
    public void withPixelPath() {
        Configuration.ConfigurationKey key = Configuration.ConfigurationKey.PATH;
        assertNull(getDefaultValue(key));
        assertEquals("/custom_path", new Configuration.Builder().withPath("/custom_path").build().get(key));
    }

    @Test
    public void withVisitorIDType() {
        Configuration.ConfigurationKey key = Configuration.ConfigurationKey.VISITOR_ID_TYPE;
        assertNull(getDefaultValue(key));
        assertEquals(Configuration.VisitorIDType.ADVERTISING_ID.stringValue(), new Configuration.Builder().withVisitorIDType(Configuration.VisitorIDType.ADVERTISING_ID).build().get(key));
    }

    @Test
    public void withOfflineMode() {
        Configuration.ConfigurationKey key = Configuration.ConfigurationKey.OFFLINE_STORAGE_MODE;
        assertNull(getDefaultValue(key));
        assertEquals(Configuration.OfflineStorageMode.REQUIRED.stringValue(), new Configuration.Builder().withOfflineStorageMode(Configuration.OfflineStorageMode.REQUIRED).build().get(key));
    }

    @Test
    public void enableIgnoreLimitedAdvertisingTracking() {
        Configuration.ConfigurationKey key = Configuration.ConfigurationKey.IGNORE_LIMITED_AD_TRACKING;
        assertNull(getDefaultValue(key));
        assertEquals("true", new Configuration.Builder().enableIgnoreLimitedAdTracking(true).build().get(key));
    }

    @Test
    public void enableCrashDetection() {
        Configuration.ConfigurationKey key = Configuration.ConfigurationKey.CRASH_DETECTION;
        assertNull(getDefaultValue(key));
        assertEquals("true", new Configuration.Builder().enableCrashDetection(true).build().get(key));
    }

    @Test
    public void withUUIDExpirationMode() {
        Configuration.ConfigurationKey key = Configuration.ConfigurationKey.VISITOR_STORAGE_MODE;
        assertNull(getDefaultValue(key));
        assertEquals(Configuration.VisitorStorageMode.RELATIVE.stringValue(), new Configuration.Builder().withVisitorStorageMode(Configuration.VisitorStorageMode.RELATIVE).build().get(key));
    }

    @Test
    public void withUUIDDuration() {
        Configuration.ConfigurationKey key = Configuration.ConfigurationKey.STORAGE_LIFETIME_VISITOR;
        assertNull(getDefaultValue(key));
        assertEquals("10", new Configuration.Builder().withStorageLifetimeVisitor(10).build().get(key));
    }

    @Test
    public void withEncryptionMode() {
        Configuration.ConfigurationKey key = Configuration.ConfigurationKey.ENCRYPTION_MODE;
        assertNull(getDefaultValue(key));
        assertEquals(Configuration.EncryptionMode.IF_COMPATIBLE.stringValue(), new Configuration.Builder().withEncryptionMode(Configuration.EncryptionMode.IF_COMPATIBLE).build().get(key));
    }

    @Test
    public void withSessionBackgroundDuration() {
        Configuration.ConfigurationKey key = Configuration.ConfigurationKey.SESSION_BACKGROUND_DURATION;
        assertNull(getDefaultValue(key));
        assertEquals("10", new Configuration.Builder().withSessionBackgroundDuration(10).build().get(key));
    }

    @Test
    public void withVisitorID() {
        Configuration.ConfigurationKey key = Configuration.ConfigurationKey.VISITOR_ID;
        assertNull(getDefaultValue(key));
        assertEquals("custom", new Configuration.Builder().withVisitorID("custom").build().get(key));
    }

    @Test
    public void withUserAgent() {
        Configuration.ConfigurationKey key = Configuration.ConfigurationKey.CUSTOM_USER_AGENT;
        assertNull(getDefaultValue(key));
        assertEquals("custom", new Configuration.Builder().withCustomUserAgent("custom").build().get(key));
    }

    @Test
    public void sendWhenOptOut() {
        Configuration.ConfigurationKey key = Configuration.ConfigurationKey.SEND_EVENT_WHEN_OPT_OUT;
        assertNull(getDefaultValue(key));
        assertEquals("true", new Configuration.Builder().enableSendEventWhenOptOut(true).build().get(key));
    }

    private String getDefaultValue(Configuration.ConfigurationKey key) {
        return new Configuration.Builder().build().get(key);
    }
}