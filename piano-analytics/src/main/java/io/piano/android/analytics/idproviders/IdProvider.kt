package io.piano.android.analytics.idproviders

interface IdProvider {
    val visitorId: String?
    val isLimitAdTrackingEnabled: Boolean
}
