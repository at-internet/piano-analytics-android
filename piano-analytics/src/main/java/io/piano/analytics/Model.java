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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnusedReturnValue")
public final class Model {

    /// region PUBLIC SECTION

    public Configuration getConfiguration() {
        return configuration;
    }

    public List<Event> getEvents() {
        return events;
    }

    public Map<String, Object> getContextProperties() {
        return contextProperties;
    }

    public Map<String, BuiltModel> getStorage() {
        return storage;
    }

    public PrivacyModel getPrivacyModel() {
        return privacyModel;
    }

    public BuiltModel getBuiltModel() {
        return builtModel;
    }

    public String getVisitorId() {
        return this.configuration.get(Configuration.ConfigurationKey.VISITOR_ID);
    }

    public User getStoredUser() {
        return user;
    }

    /// endregion

    /// region Constructors

    private Configuration configuration = new Configuration();
    private List<Event> events = new ArrayList<>();
    private Map<String, Object> contextProperties = new HashMap<>();
    private Map<String, BuiltModel> storage = new HashMap<>();
    private int storageDaysRemaining;
    private User user = null;
    private CustomerContextModel customerContextModel;
    private UserModel userModel;
    private PrivacyModel privacyModel;
    private BuiltModel builtModel;

    Model() {

    }

    /// endregion

    /// region Package methods

    Model Config(Configuration configuration) {
        this.configuration = configuration;
        return this;
    }

    Model setVisitorId(String visitorID) {
        this.configuration.set(Configuration.ConfigurationKey.VISITOR_ID, visitorID);
        return this;
    }

    Model setEvents(List<Event> events) {
        this.events = events;
        return this;
    }

    Model addContextProperty(String key, Object value) {
        this.contextProperties.put(key, value);
        return this;
    }

    Model addContextProperties(Map<String, Object> properties) {
        this.contextProperties.putAll(properties);
        return this;
    }

    Model setContextProperties(Map<String, Object> contextProperties) {
        this.contextProperties = contextProperties;
        return this;
    }

    CustomerContextModel getCustomerContextModel() {
        return customerContextModel;
    }

    Model setCustomerContextModel(CustomerContextModel customerContextModel) {
        this.customerContextModel = customerContextModel;
        return this;
    }

    int getStorageDaysRemaining(){
        return storageDaysRemaining;
    }

    Model setStorageDaysRemaining(int storageDaysRemaining){
        this.storageDaysRemaining = storageDaysRemaining;
        return this;
    }

    UserModel getUserModel() {
        return userModel;
    }

    Model setUserModel(UserModel userModel) {
        this.userModel = userModel;
        return this;
    }

    Model setUser(User user) {
        this.user = user;
        return this;
    }

    Model setPrivacyModel(PrivacyModel privacyModel) {
        this.privacyModel = privacyModel;
        return this;
    }

    Model setStorage(Map<String, BuiltModel> storage) {
        this.storage = storage;
        return this;
    }

    Model setBuiltModel(BuiltModel builtModel) {
        this.builtModel = builtModel;
        return this;
    }

    /// endregion
}
