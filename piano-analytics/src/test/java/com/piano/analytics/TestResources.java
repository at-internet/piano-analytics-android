package com.piano.analytics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TestResources {

    static final String VISITOR_ID = "visitor";
    static final String VISITOR_MODE = "test";

    private TestResources() {
    }

    static Configuration createConfiguration() {
        return new Configuration.Builder()
                .withCollectDomain("example.com")
                .withPath("/test")
                .withSite(123456)
                .withOfflineStorageMode(Configuration.OfflineStorageMode.NEVER)
                .withVisitorID(VISITOR_ID)
                .enableCrashDetection(true)
                .enableIgnoreLimitedAdTracking(true)
                .enableSendEventWhenOptOut(true)
                .withCustomUserAgent("toto")
                .build();
    }

    static Map<String, Object> createInternalContextProperties() {
        Map<String, Object> internalContextProperties = new HashMap<>();
        internalContextProperties.put("browser_language_local", "US");
        internalContextProperties.put("device_screen_diagonal", 3.6);
        internalContextProperties.put("app_version", null);
        internalContextProperties.put("connection_type", "UNKNOWN");
        internalContextProperties.put("os_version", "5.0.2");
        internalContextProperties.put("os_group", "android");
        internalContextProperties.put("os_name", "android 5.0.2");
        internalContextProperties.put("manufacturer", "unknown");
        internalContextProperties.put("event_collection_version", "3.0.0");
        internalContextProperties.put("device_screen_width", 320);
        internalContextProperties.put("device_screen_height", 470);
        internalContextProperties.put("model", "robolectric");
        internalContextProperties.put("app_id", "com.piano.analytics.test");
        internalContextProperties.put("browser_language", "en");
        internalContextProperties.put("event_collection_platform", "android");
        return internalContextProperties;
    }

    static Map<String, Object> createCustomerContextProperties() {
        Map<String, Object> customerContextProperties = new HashMap<>();
        customerContextProperties.put("prop1", "val1");
        customerContextProperties.put("prop2", "val2");
        customerContextProperties.put("prop1_1", "val1");
        customerContextProperties.put("prop1_2", "val2");
        return customerContextProperties;
    }

    static List<Event> createEventsList() {
        Map<String, Object> subdata = new HashMap<>();
        subdata.put("sd_prop1", "sd1");
        subdata.put("sd_prop2", "sd2");

        Map<String, Object> event1Data = new HashMap<>();
        event1Data.put("page2", "myPage1");
        event1Data.put("ev1prop1", "value 1");
        event1Data.put("eV1proP2", 2);
        event1Data.put("eV1proP3", subdata);

        Map<String, Object> event2Data = new HashMap<>();
        event2Data.put("page1", "myPage2");
        event2Data.put("ev2prop1", "test value");
        return Arrays.asList(new Event("event1", event1Data), new Event("event2", event2Data));
    }

    static Map<String, BuiltModel> createStorage() {
        Map<String, BuiltModel> storage = new HashMap<>();
        storage.put("path/to/fake/stored/file", new BuiltModel("uri", "body", true));
        return storage;
    }
}
