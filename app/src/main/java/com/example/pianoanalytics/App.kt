package com.example.pianoanalytics

import android.app.Application
import io.piano.android.analytics.Configuration
import io.piano.android.analytics.PianoAnalytics
import io.piano.android.analytics.model.VisitorIDType
import timber.log.Timber

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        val configuration = Configuration.Builder(
            collectDomain = "logs.xiti.com",
            site = 552987,
            visitorIDType = VisitorIDType.ADVERTISING_ID
        ).ignoreLimitedAdTracking(true).build()
        PianoAnalytics.init(applicationContext, configuration)
    }
}
