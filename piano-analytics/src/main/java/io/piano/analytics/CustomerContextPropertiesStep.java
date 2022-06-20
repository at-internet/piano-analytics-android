/*
 * This SDK is licensed under the MIT license (MIT)
 * Copyright (c) 2015- Applied Technologies Internet SAS (registration number B 403 261 258 - Trade and Companies Register of Bordeaux – France)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.piano.analytics;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class CustomerContextPropertiesStep implements WorkingQueue.IStep {

    /// region Constructors

    private static CustomerContextPropertiesStep instance = null;

    private final static String allEventsNameWildcard = "*";

    static CustomerContextPropertiesStep getInstance() {
        if (instance == null) {
            instance = new CustomerContextPropertiesStep();
        }
        return instance;
    }

    private final List<ContextProperties> properties = new ArrayList<>();

    private CustomerContextPropertiesStep() {
    }

    /// endregion

    /// region Package methods

    CustomerContextPropertiesStep setProperties(Map<String, Object> properties) {
        return this.setProperties(properties, null);
    }

    CustomerContextPropertiesStep setProperties(Map<String, Object> properties, String[] events) {
        if (events == null || events.length == 0) {
            events = new String[]{allEventsNameWildcard};
        }

        this.properties.add(new ContextProperties(properties, true, events));

        return this;
    }

    List<ContextProperties> getProperties() {
        return properties;
    }

    /// endregion

    /// region WorkingQueue.IStep implementation

    @Override
    public void processUpdateContext(Model m) {
        /// REQUIREMENTS
        CustomerContextModel customerContextModel = m.getCustomerContextModel();
        if (customerContextModel == null) {
            return;
        }

        Map<String, Object> props = customerContextModel.getProperties();
        String[] events = customerContextModel.getEvents();
        if (events == null || events.length == 0) {
            events = new String[]{allEventsNameWildcard};
        }
        Boolean persistent = customerContextModel.getPersistent();

        switch (customerContextModel.getUpdateRequestKey()) {
            case ADD:
                this.properties.add(new ContextProperties(props, persistent, events));
                break;
            case DELETE:
                if (props == null) {
                    this.properties.clear();
                } else {
                    for (String key : props.keySet()) {
                        for (ContextProperties contextProperties : this.properties) {
                            contextProperties.data.remove(key);
                        }
                    }
                }
                break;
            default:
                PianoAnalytics.InternalLogger.warning("CustomerContextPropertiesStep.processUpdateContext : unknown update request key case");
                break;
        }
    }

    @Override
    public void processGetModel(Context ctx, Model m) {
        Map<String, Object> context = new HashMap<>();

        for (ContextProperties contextProperties : this.properties) {
            if (!Arrays.asList(contextProperties.events).contains(allEventsNameWildcard)) {
                continue;
            }

            for (Map.Entry<String, Object> entry : contextProperties.data.entrySet()) {
                if (!context.containsKey(entry.getKey())) {
                    context.put(entry.getKey(), entry.getValue());
                }
            }
        }

        m.addContextProperties(context);
    }

    List<Event> addPropertiesFromContext(List<Event> events, ContextProperties contextProperties) {
        List<Event> newEvents = new ArrayList<>(events.size());

        for (Event event : events) {
            Map<String, Object> newEventData = MapUtils.copy((Map<String, Object>) (event.toMap().get("data")));

            if (PianoAnalyticsUtils.simpleWildcardCompare(contextProperties.events, event.getName()))
                for (Map.Entry<String, Object> entry : contextProperties.data.entrySet())
                    if (!newEventData.containsKey(entry.getKey()))
                        newEventData.put(entry.getKey(), entry.getValue());

            newEvents.add(new Event(event.getName(), newEventData));
        }

        return newEvents;
    }

    @Override
    public boolean processTrackEvents(Context ctx, Model m, PianoAnalytics.OnWorkListener l) {
        List<ContextProperties> persistentProperties = new ArrayList<>();

        for (ContextProperties contextProperties : this.properties) {
            if (PianoAnalyticsUtils.contains(contextProperties.events, allEventsNameWildcard)) {
                m.addContextProperties(contextProperties.data);
            } else {
                m.setEvents(this.addPropertiesFromContext(m.getEvents(), contextProperties));
            }

            if (contextProperties.persistent) {
                persistentProperties.add(contextProperties);
            }
        }

        this.properties.clear();
        this.properties.addAll(persistentProperties);

        return true;
    }

    /// endregion

    /// region properties with options

    class ContextProperties {
        Map<String, Object> data;
        Boolean persistent;
        String[] events;

        ContextProperties(Map<String, Object> data, Boolean persistent, String[] events) {
            this.data = data;
            this.persistent = persistent;
            this.events = events;
        }
    }

    /// endregion
}
