package com.piano.analytics;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CustomerContextPropertiesStepTest {

    @Test
    public void processUpdateContext() {
        Map<String, Object> testCustomerContextProperties = TestResources.createCustomerContextProperties();
        CustomerContextPropertiesStep customerContextPropertiesStep = CustomerContextPropertiesStep.getInstance()
                .setProperties(new HashMap<>());

        /// Update with add
        Model model = new Model()
                .setCustomerContextModel(new CustomerContextModel(CustomerContextModel.UpdateTypeKey.ADD, testCustomerContextProperties));

        customerContextPropertiesStep.processUpdateContext(model);
        assertEquals(testCustomerContextProperties, customerContextPropertiesStep.getProperties());

        Map<String, Object> customerContextProperties = new HashMap<>();
        customerContextProperties.put("prop1_2", "val2");

        /// Update with delete one property
        model.setCustomerContextModel(new CustomerContextModel(CustomerContextModel.UpdateTypeKey.DELETE, customerContextProperties));
        customerContextPropertiesStep.processUpdateContext(model);

        Map<String, Object> expected = new HashMap<>();
        expected.put("prop1", "val1");
        expected.put("prop2", "val2");
        expected.put("prop1_1", "val1");
        assertEquals(expected, customerContextPropertiesStep.getProperties());

        /// Update with delete all
        customerContextPropertiesStep.processUpdateContext(new Model().setCustomerContextModel(new CustomerContextModel(CustomerContextModel.UpdateTypeKey.DELETE, null)));
        assertEquals(new HashMap<>(), customerContextPropertiesStep.getProperties());

        /// Update with context null
        customerContextPropertiesStep.processUpdateContext(new Model());
        assertTrue(customerContextPropertiesStep.getProperties().isEmpty());
    }

    @Test
    public void processTrackEvents() {
        CustomerContextPropertiesStep customerContextPropertiesStep = CustomerContextPropertiesStep.getInstance()
                .setProperties(new HashMap<>());

        /// Empty context
        Model model = new Model();
        assertTrue(customerContextPropertiesStep.processTrackEvents(null, model, null));
        assertTrue(model.getContextProperties().isEmpty());

        /// Context filled
        Map<String, Object> testCustomerContextProperties = TestResources.createCustomerContextProperties();
        customerContextPropertiesStep.setProperties(testCustomerContextProperties);
        assertTrue(customerContextPropertiesStep.processTrackEvents(null, model, null));
        assertEquals(testCustomerContextProperties, model.getContextProperties());
    }

    @Test
    public void processGetModel() {
        CustomerContextPropertiesStep customerContextPropertiesStep = CustomerContextPropertiesStep.getInstance()
                .setProperties(new HashMap<>());

        /// Empty
        Model model = new Model();
        customerContextPropertiesStep.processGetModel(null, model);
        assertTrue(model.getContextProperties().isEmpty());

        Map<String, Object> testCustomerContextProperties = TestResources.createCustomerContextProperties();
        customerContextPropertiesStep.setProperties(testCustomerContextProperties);
        customerContextPropertiesStep.processGetModel(null, model);
        assertEquals(testCustomerContextProperties, model.getContextProperties());
    }
}