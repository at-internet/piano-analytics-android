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

final class PreferencesKeys {

    /// region Constructors

    private PreferencesKeys() {
    }

    /// endregion

    /// region Namespace

    static final String PREFERENCES = "PAPreferencesKey";

    /// endregion

    /// region Lifecycle

    static final String FIRST_INIT_LIFECYCLE_DONE = "PAFirstInitLifecycleDone";
    static final String INIT_LIFECYCLE_DONE = "PAInitLifecycleDone";
    static final String VERSION_CODE_KEY = "PAVersionCode";
    static final String FIRST_SESSION = "PAFirstLaunch";
    static final String FIRST_SESSION_AFTER_UPDATE = "PAFirstLaunchAfterUpdate";
    static final String FIRST_SESSION_DATE = "PAFirstLaunchDate";
    static final String FIRST_SESSION_DATE_AFTER_UPDATE = "PAFirstLaunchDateAfterUpdate";
    static final String LAST_SESSION_DATE = "PALastLaunchDate";
    static final String SESSION_COUNT = "PALaunchCount";
    static final String SESSION_COUNT_SINCE_UPDATE = "PALaunchCountSinceUpdate";
    static final String DAYS_SINCE_FIRST_SESSION = "PADaysSinceFirstLaunch";
    static final String DAYS_SINCE_UPDATE = "PADaysSinceFirstLaunchAfterUpdate";
    static final String DAYS_SINCE_LAST_SESSION = "PADaysSinceLastUse";

    /// endregion

    /// region Visitor ID

    static final String VISITOR_UUID_AT = "ATIdclientUUID";
    static final String VISITOR_UUID = "PAIdclientUUID";
    static final String VISITOR_UUID_GENERATION_TIMESTAMP = "PAIdclientUUIDGenerationTimestamp";

    //// endregion

    /// region Privacy

    static final String PRIVACY_MODE = "PAPrivacyMode";
    static final String PRIVACY_MODE_EXPIRATION_TIMESTAMP = "PAPrivacyModeExpirationTimestamp";
    static final String PRIVACY_VISITOR_CONSENT = "PAPrivacyVisitorConsent";
    static final String PRIVACY_VISITOR_ID = "PAPrivacyUserId";

    /// endregion

    /// region Crash

    static final String CRASHED = "PACrashed";
    static final String CRASH_INFO = "PACrashInfo";

    /// endregion

    /// region Users

    static final String USER = "PAUser";
    static final String USER_GENERATION_TIMESTAMP = "PAUserGenerationTimestamp";

    //// endregion
}