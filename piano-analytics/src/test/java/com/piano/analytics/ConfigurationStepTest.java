package com.piano.analytics;

import android.os.Build;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {Build.VERSION_CODES.LOLLIPOP})
public class ConfigurationStepTest {

    @Test
    public void loadConfigurationFromLocalFile() {
        ConfigurationStep configurationStep = ConfigurationStep.getInstance(ApplicationProvider.getApplicationContext(), null);
        Configuration defaultConfig = configurationStep.getConfiguration();

        configurationStep.loadConfigurationFromLocalFile(ApplicationProvider.getApplicationContext(), "/path/to/config/file");
        assertEquals(defaultConfig, configurationStep.getConfiguration());
    }

    @Test
    public void processSetConfig() {
        Configuration testConfiguration = TestResources.createConfiguration();
        ConfigurationStep configurationStep = ConfigurationStep.getInstance(ApplicationProvider.getApplicationContext(), null)
                .setConfiguration(testConfiguration);

        String endpoint = "override.com";
        String pixelPath = "custom";
        String siteId = "11111";

        Model model = new Model()
                .Config(new Configuration.Builder()
                        .withCollectDomain(endpoint)
                        .withPath(pixelPath)
                        .withSite(Integer.parseInt(siteId))
                        .build());

        configurationStep.processSetConfig(model);

        Configuration configuration = model.getConfiguration();
        assertEquals(testConfiguration.size(), configuration.size());
        assertEquals(endpoint, configuration.get(Configuration.ConfigurationKey.COLLECT_DOMAIN));
        assertEquals(pixelPath, configuration.get(Configuration.ConfigurationKey.PATH));
        assertEquals(siteId, configuration.get(Configuration.ConfigurationKey.SITE));
    }

    @Test
    public void processSendOfflineStorageWithGlobalConfig() {
        Configuration testConfiguration = TestResources.createConfiguration();
        ConfigurationStep configurationStep = ConfigurationStep.getInstance(ApplicationProvider.getApplicationContext(), null)
                .setConfiguration(testConfiguration);

        Model model = new Model();

        assertTrue(configurationStep.processSendOfflineStorage(model, null));

        Configuration configuration = model.getConfiguration();
        assertNotNull(configuration);
        assertNotSame(configuration, testConfiguration);
        assertEquals(testConfiguration, configuration);
    }

    @Test
    public void processSendOfflineStorageWithSpecificConfig() {
        Configuration testConfiguration = TestResources.createConfiguration();
        ConfigurationStep configurationStep = ConfigurationStep.getInstance(ApplicationProvider.getApplicationContext(), null)
                .setConfiguration(testConfiguration);
        String globalSiteId = testConfiguration.get(Configuration.ConfigurationKey.SITE);
        String specificSiteId = "999999";

        Model model = new Model()
                .Config(new Configuration.Builder().withSite(Integer.parseInt(specificSiteId)).build());

        assertTrue(configurationStep.processSendOfflineStorage(model, null));

        Configuration configuration = model.getConfiguration();
        assertNotNull(configuration);
        assertNotSame(configuration, testConfiguration);
        assertEquals(specificSiteId, configuration.get(Configuration.ConfigurationKey.SITE));

        configuration.remove(Configuration.ConfigurationKey.SITE.stringValue());
        testConfiguration.remove(Configuration.ConfigurationKey.SITE.stringValue());

        assertEquals(testConfiguration, configuration);

        testConfiguration.set(Configuration.ConfigurationKey.SITE, globalSiteId);
    }

    @Test
    public void processTrackEventsWithGlobalConfig() {
        Configuration testConfiguration = TestResources.createConfiguration();
        ConfigurationStep configurationStep = ConfigurationStep.getInstance(ApplicationProvider.getApplicationContext(), null)
                .setConfiguration(testConfiguration);

        Model model = new Model();

        assertTrue(configurationStep.processTrackEvents(ApplicationProvider.getApplicationContext(), model, null));

        Configuration configuration = model.getConfiguration();
        assertNotNull(configuration);
        assertNotSame(configuration, testConfiguration);
        assertEquals(testConfiguration, configuration);
    }

    @Test
    public void processTrackEventsWithSpecificConfig() {
        Configuration testConfiguration = TestResources.createConfiguration();
        ConfigurationStep configurationStep = ConfigurationStep.getInstance(ApplicationProvider.getApplicationContext(), null)
                .setConfiguration(testConfiguration);
        String globalSiteId = testConfiguration.get(Configuration.ConfigurationKey.SITE);
        String specificSiteId = "999999";

        Model model = new Model()
                .Config(new Configuration.Builder().withSite(Integer.parseInt(specificSiteId)).build());

        assertTrue(configurationStep.processTrackEvents(ApplicationProvider.getApplicationContext(), model, null));

        Configuration configuration = model.getConfiguration();
        assertNotNull(configuration);
        assertNotSame(configuration, testConfiguration);
        assertEquals(specificSiteId, configuration.get(Configuration.ConfigurationKey.SITE));

        configuration.remove(Configuration.ConfigurationKey.SITE.stringValue());
        testConfiguration.remove(Configuration.ConfigurationKey.SITE.stringValue());

        assertEquals(testConfiguration, configuration);

        testConfiguration.set(Configuration.ConfigurationKey.SITE, globalSiteId);
    }

    @Test
    public void processUpdateContext() {
        Configuration testConfiguration = TestResources.createConfiguration();
        ConfigurationStep configurationStep = ConfigurationStep.getInstance(ApplicationProvider.getApplicationContext(), null)
                .setConfiguration(testConfiguration);

        Model model = new Model();
        configurationStep.processUpdateContext(model);

        Configuration configuration = model.getConfiguration();
        assertNotNull(configuration);
        assertNotSame(configuration, testConfiguration);
        assertEquals(testConfiguration, configuration);
    }

    @Test
    public void processGetModel() {
        Configuration testConfiguration = TestResources.createConfiguration();
        ConfigurationStep configurationStep = ConfigurationStep.getInstance(ApplicationProvider.getApplicationContext(), null)
                .setConfiguration(testConfiguration);

        Model model = new Model();
        configurationStep.processGetModel(ApplicationProvider.getApplicationContext(), model);

        Configuration configuration = model.getConfiguration();
        assertNotNull(configuration);
        assertNotSame(configuration, testConfiguration);
        assertEquals(testConfiguration, configuration);
    }
}