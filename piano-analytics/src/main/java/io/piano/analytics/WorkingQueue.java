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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class WorkingQueue {

    /// region ProcessingType enumeration to identify processing step type

    enum ProcessingType {
        /***
         * Send event data
         */
        TRACK_EVENTS,

        /***
         * Update configuration
         */
        SET_CONFIG,

        /***
         * Update internal context
         */
        UPDATE_CONTEXT,

        /***
         * Update internal privacy context
         */
        UPDATE_PRIVACY_CONTEXT,

        /***
         * Send offline data
         */
        SEND_OFFLINE_STORAGE,

        /***
         * Delete offline data
         */
        DELETE_OFFLINE_STORAGE,
    }

    /// endregion

    /// region IStep interface

    interface IStep {

        default boolean processTrackEvents(Context ctx, Model m, PianoAnalytics.OnWorkListener l) {
            return true;
        }

        default boolean processSendOfflineStorage(Model m, PianoAnalytics.OnWorkListener l) {
            return true;
        }

        default void processDeleteOfflineStorage(Model m) {
        }

        default void processSetConfig(Model m) {
        }

        default void processGetConfig(Model m) {
        }

        default void processPrivacyMode(Model m) {
        }

        default void processUpdateContext(Model m) {
        }

        default void processUpdatePrivacyContext(Model m) {
        }

        default void processGetModel(Context ctx, Model m) {
        }
    }

    /// endregion

    private interface IProcessing {
        void process(List<IStep> steps, Context ctx, Model m, PianoAnalytics.OnWorkListener l);
    }

    /// region Constructors

    private final ExecutorService pool = Executors.newSingleThreadExecutor();
    private final List<IStep> steps;
    private final Context appContext;
    private final Map<ProcessingType, IProcessing> processingMap;

    WorkingQueue(Context context, String configFileLocation) {
        this.appContext = context;

        ConfigurationStep configurationStep = ConfigurationStep.getInstance(context, configFileLocation);
        PrivacyStep ps = PrivacyStep.getInstance(context, configurationStep.getConfiguration());
        this.steps = new ArrayList<>(Arrays.asList(configurationStep,
                VisitorIDStep.getInstance(ps),
                CrashHandlingStep.getInstance(context, ps),
                LifecycleStep.getInstance(context, ps),
                InternalContextPropertiesStep.getInstance(),
                CustomerContextPropertiesStep.getInstance(),
                UsersStep.getInstance(context, ps, configurationStep.getConfiguration()),
                OnBeforeBuildCallStep.getInstance(),
                ps,
                BuildStep.getInstance(),
                StorageStep.getInstance(context),
                OnBeforeSendCallStep.getInstance(),
                SendStep.getInstance()
        ));

        this.processingMap = new EnumMap<>(ProcessingType.class);
        this.processingMap.put(ProcessingType.DELETE_OFFLINE_STORAGE, (steps, ctx, m, l) -> {
            for (IStep s : steps) {
                s.processDeleteOfflineStorage(m);
            }
        });
        this.processingMap.put(ProcessingType.SEND_OFFLINE_STORAGE, (steps, ctx, m, l) -> {
            for (IStep s : steps) {
                if (!s.processSendOfflineStorage(m, l)) {
                    return;
                }
            }
        });
        this.processingMap.put(ProcessingType.TRACK_EVENTS, (steps, ctx, m, l) -> {
            for (IStep s : steps) {
                if (!s.processTrackEvents(ctx, m, l)) {
                    return;
                }
            }
        });
        this.processingMap.put(ProcessingType.SET_CONFIG, (steps, ctx, m, l) -> {
            for (IStep s : steps) {
                s.processSetConfig(m);
            }
        });
        this.processingMap.put(ProcessingType.UPDATE_CONTEXT, (steps, ctx, m, l) -> {
            for (IStep s : steps) {
                s.processUpdateContext(m);
            }
        });
        this.processingMap.put(ProcessingType.UPDATE_PRIVACY_CONTEXT, (steps, ctx, m, l) -> {
            for (IStep s : steps) {
                s.processUpdatePrivacyContext(m);
            }
        });
    }

    /// endregion

    /// region Package methods

    void getModelAsync(final PianoAnalytics.OnGetModelListener l) {
        pool.execute(() -> {
            Model m = new Model();
            for (IStep s : this.steps) {
                s.processGetModel(this.appContext, m);
            }
            l.onGetModel(m);
        });
    }

    void getUserAsync(final PianoAnalytics.OnGetUserListener l) {
        pool.execute(() -> {
            Model m = new Model();
            for (IStep s : this.steps) {
                s.processGetModel(this.appContext, m);
            }
            l.onGetUser(m.getStoredUser());
        });
    }

    void getConfigurationAsync(final Configuration.ConfigurationKey key, final PianoAnalytics.OnGetConfigurationListener l) {
        pool.execute(() -> {
            Model m = new Model();
            for (IStep s : this.steps) {
                s.processGetConfig(m);
            }
            l.onGetConfiguration(m.getConfiguration().get(key));
        });
    }

    void getPrivacyModeAsync(final PianoAnalytics.OnGetPrivacyModeListener l) {
        pool.execute(() -> {
            Model m = new Model();
            for (IStep s : this.steps) {
                s.processPrivacyMode(m);
            }
            l.onGetPrivacyMode(m.getPrivacyModel().getVisitorMode());
        });
    }

    void push(ProcessingType pt, final Model m, final PianoAnalytics.OnWorkListener l) {
        pool.execute(() -> {
            IProcessing workingFlowFunction = this.processingMap.get(pt);
            if (workingFlowFunction != null) {
                workingFlowFunction.process(this.steps, this.appContext, m, l);
            }
        });
    }

    /// endregion
}
