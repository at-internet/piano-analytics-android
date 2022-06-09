package com.piano.analytics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.LOLLIPOP})
public class PianoAnalyticsTest {

    private boolean areEqual(Map<String, Set<String>> m1, Map<String, Set<String>> m2) {
        if (m1.size() != m2.size()) {
            return false;
        }

        return m1.entrySet().stream()
                .allMatch(e -> e.getValue().equals(m2.get(e.getKey())));
    }

    private <K, V> Map<K, V> deepCopy(Map<K, V> m) {
        Map<K, V> copy = new HashMap<>(m);
        copy.putAll(m);
        return copy;
    }

    private <K, T> Map<K, Set<T>> mergeMapsOfSets(Map<K, Set<T>> m1, Map<K, Set<T>> m2) {
        Map<K, Set<T>> result = deepCopy(m1);

        for (Map.Entry<K, Set<T>> entry : m2.entrySet()) {
            if (result.containsKey(entry.getKey())) {
                Set<T> adding = result.get(entry.getKey());
                adding.addAll(entry.getValue());
                result.put(entry.getKey(), adding);
            } else {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }

    @Test
    public void getApplicationProperties() {
        Context ctx = ApplicationProvider.getApplicationContext();

        PianoAnalytics pa = PianoAnalytics.getInstance(ctx);
        pa.privacySetMode(PianoAnalytics.PrivacyVisitorMode.OPTOUT.stringValue());

        AsyncTester asyncTester = new AsyncTester(() -> pa.getModel(initModel -> {
            Map<String, Set<String>> initiallyAuthorizedProperties = deepCopy(initModel.getPrivacyModel().getAuthorizedPropertyKeys());

            Map<String, Set<String>> authorizedPropertiesToAdd = new HashMap<>();
            authorizedPropertiesToAdd.put("page.display", new HashSet<>(Arrays.asList("prop1_sub1", "prop2_sub1", "prop2_sub2", "prop3_*", "prop4_sub1_*", "prop4_sub2")));

            for (Map.Entry<String, Set<String>> entry : authorizedPropertiesToAdd.entrySet()) {
                pa.privacyIncludeProperties(entry.getValue().toArray(new String[0]), new String[]{initModel.getPrivacyModel().getVisitorMode()}, new String[]{entry.getKey()});
            }

            AsyncTester asyncTester2 = new AsyncTester(() -> {
                pa.getModel(model -> {
                    boolean result = areEqual(mergeMapsOfSets(initiallyAuthorizedProperties, authorizedPropertiesToAdd), model.getPrivacyModel().getAuthorizedPropertyKeys());
                    System.out.printf("result is %b\n", result);
                    assertTrue(result);
                });
            });
            asyncTester2.start();
            try {
                System.out.println("asyncTester2 start");
                asyncTester2.test();
                System.out.println("asyncTester2 end");
            } catch (InterruptedException e) {
                System.out.println("asyncTester2 fail");
                fail();
            }
        }));

        asyncTester.start();
        try {
            System.out.println("asyncTester start");
            asyncTester.test();
            System.out.println("asyncTester end");
        } catch (InterruptedException e) {
            System.out.println("asyncTester fail");
            fail();
        }
    }
}

class AsyncTester {
    private final Thread thread;
    private AssertionError exc;

    public AsyncTester(final Runnable runnable) {
        thread = new Thread(() -> {
            try {
                runnable.run();
            } catch (AssertionError e) {
                exc = e;
            }
        });
    }

    public void start() {
        thread.start();
    }

    public void test() throws InterruptedException {
        thread.join();
        if (exc != null)
            throw exc;
    }
}