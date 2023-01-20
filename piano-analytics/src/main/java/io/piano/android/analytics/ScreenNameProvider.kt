package io.piano.android.analytics

import android.app.Activity
import android.app.Application
import android.os.Bundle

internal class ScreenNameProvider : Application.ActivityLifecycleCallbacks {
    private var currentActivityName: String = ""
    internal var customScreenName: String? = null
    internal val screenName: String
        get() = customScreenName ?: currentActivityName

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivityName = activity.localClassName
    }

    override fun onActivityPaused(activity: Activity) {
        TODO("Not yet implemented")
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }
}
