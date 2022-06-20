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

import java.util.HashMap;

final class OnBeforeBuildCallStep implements WorkingQueue.IStep {

    /// region Constructors

    private static OnBeforeBuildCallStep instance = null;

    static OnBeforeBuildCallStep getInstance() {
        if (instance == null) {
            instance = new OnBeforeBuildCallStep();
        }
        return instance;
    }

    private OnBeforeBuildCallStep() {
    }

    /// endregion

    /// region Constants

    static final String CALLBACK_USED_PROPERTY = "_callback_used";

    /// endregion

    /// region WorkingQueue.IStep implementation

    @Override
    public boolean processTrackEvents(Context ctx, Model m, PianoAnalytics.OnWorkListener l) {
        if (l == null) {
            return true;
        }
        /// REQUIREMENTS
        Configuration configuration = new Configuration(m.getConfiguration());
        m.getConfiguration().clear();

        if (l.onBeforeBuild(m)) {
            m.Config(configuration)
                    .addContextProperty(CALLBACK_USED_PROPERTY, true);
            return true;
        }
        return false;
    }

    /// endregion
}

final class OnBeforeSendCallStep implements WorkingQueue.IStep {

    /// region Constructors

    private static OnBeforeSendCallStep instance = null;

    static OnBeforeSendCallStep getInstance() {
        if (instance == null) {
            instance = new OnBeforeSendCallStep();
        }
        return instance;
    }

    private OnBeforeSendCallStep() {
    }

    /// endregion

    /// region WorkingQueue.IStep implementation

    @Override
    public boolean processSendOfflineStorage(Model m, PianoAnalytics.OnWorkListener l) {
        return (l == null || l.onBeforeSend(null, new HashMap<>(m.getStorage())));
    }

    @Override
    public boolean processTrackEvents(Context ctx, Model m, PianoAnalytics.OnWorkListener l) {
        return (l == null || l.onBeforeSend(m.getBuiltModel(), new HashMap<>(m.getStorage())));
    }

    /// endregion
}