package io.piano.android.analytics.idproviders

import io.piano.android.analytics.Configuration
import io.piano.android.analytics.PrivacyModesStorage
import io.piano.android.analytics.model.PrivacyMode
import io.piano.android.analytics.model.VisitorIDType

internal class VisitorIdProvider(
    private val configuration: Configuration,
    private val privacyModesStorage: PrivacyModesStorage,
    private val limitedTrackingIdProvider: IdProvider,
    private val idByTypeProvider: (VisitorIDType) -> IdProvider
) : IdProvider {
    override val visitorId: String?
        get() = when (privacyModesStorage.currentMode) {
            PrivacyMode.NO_STORAGE -> NO_STORAGE_ID
            PrivacyMode.NO_CONSENT -> NO_CONSENT_ID
            PrivacyMode.OPTOUT -> OPT_OUT_ID
            else -> {
                val provider = idByTypeProvider(configuration.visitorIDType)
                when {
                    !provider.isLimitAdTrackingEnabled -> provider.visitorId
                    configuration.ignoreLimitedAdTracking -> limitedTrackingIdProvider.visitorId
                    else -> OPT_OUT_ID
                }
            }
        }
    override val isLimitAdTrackingEnabled: Boolean
        get() = idByTypeProvider(configuration.visitorIDType).isLimitAdTrackingEnabled

    companion object {
        internal const val NO_CONSENT_ID = "Consent-NO"
        internal const val NO_STORAGE_ID = "no-storage"
        internal const val OPT_OUT_ID = "opt-out"
    }
}
