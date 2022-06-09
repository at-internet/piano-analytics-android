package com.piano.analytics;

import android.content.Context;
import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.LOLLIPOP})
public class OnBeforeSendCallStepTest {

    private final Context ctx = ApplicationProvider.getApplicationContext();

    @Test
    public void processTrackEvents() {
        OnBeforeSendCallStep onBeforeSendCallStep = OnBeforeSendCallStep.getInstance();
        Model m = new Model();

        assertTrue(onBeforeSendCallStep.processTrackEvents(ctx, m, null));
        assertTrue(onBeforeSendCallStep.processTrackEvents(ctx, m, new PianoAnalytics.OnWorkListener() {
            @Override
            public boolean onBeforeSend(BuiltModel built, Map<String, BuiltModel> stored) {
                return true;
            }
        }));
        assertFalse(onBeforeSendCallStep.processTrackEvents(ctx, m, new PianoAnalytics.OnWorkListener() {
            @Override
            public boolean onBeforeSend(BuiltModel built, Map<String, BuiltModel> stored) {
                return false;
            }
        }));
    }

    @Test
    public void processSendOfflineStorage() {
        OnBeforeSendCallStep onBeforeSendCallStep = OnBeforeSendCallStep.getInstance();
        Model m = new Model();

        assertTrue(onBeforeSendCallStep.processSendOfflineStorage(m, null));
        assertTrue(onBeforeSendCallStep.processSendOfflineStorage(m, new PianoAnalytics.OnWorkListener() {
            @Override
            public boolean onBeforeSend(BuiltModel built, Map<String, BuiltModel> stored) {
                return true;
            }
        }));
        assertFalse(onBeforeSendCallStep.processSendOfflineStorage(m, new PianoAnalytics.OnWorkListener() {
            @Override
            public boolean onBeforeSend(BuiltModel built, Map<String, BuiltModel> stored) {
                return false;
            }
        }));
    }
}