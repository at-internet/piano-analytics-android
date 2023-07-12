package io.piano.android.analytics.idproviders

internal class CustomIdProvider(
    override var visitorId: String? = null,
) : IdProvider {
    override val isLimitAdTrackingEnabled: Boolean = false
}
