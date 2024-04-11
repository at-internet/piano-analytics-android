package io.piano.android.analytics.idproviders

internal interface IdProvider {
    val visitorId: String?
    val isLimitAdTrackingEnabled: Boolean
}
