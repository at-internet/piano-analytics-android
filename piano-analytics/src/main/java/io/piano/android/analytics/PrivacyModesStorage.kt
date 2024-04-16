package io.piano.android.analytics

import io.piano.android.analytics.model.PrivacyMode
import io.piano.android.analytics.model.PrivacyMode.Companion.NO_CONSENT
import io.piano.android.analytics.model.PrivacyMode.Companion.toPrivacyMode
import io.piano.android.analytics.model.PrivacyStorageFeature
import io.piano.android.consents.PianoConsents
import io.piano.android.consents.models.Product
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Stores information about privacy modes
 */
@Deprecated("Use `PianoConsents` for managing consents instead")
public class PrivacyModesStorage internal constructor(
    private val configuration: Configuration,
    private val prefsStorage: PrefsStorage,
    private val pianoConsents: PianoConsents?,
) {
    private val consentsEnabled = pianoConsents?.consentConfiguration?.requireConsent == true
    init {
        prefsStorage.privacyStorageFilter = ::isFeatureAllowed
    }
    private fun isFeatureAllowed(privacyStorageFeature: PrivacyStorageFeature): Boolean {
        val isNotForbidden = PrivacyStorageFeature.ALL !in cachedMode.forbiddenStorageFeatures ||
            privacyStorageFeature !in cachedMode.forbiddenStorageFeatures
        val isAllowed = PrivacyStorageFeature.ALL in cachedMode.allowedStorageFeatures ||
            privacyStorageFeature in cachedMode.allowedStorageFeatures
        return isNotForbidden && isAllowed
    }

    /**
     * All registered privacy modes. Add a [PrivacyMode] instance into [allModes] for registering it
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    public val allModes: MutableSet<PrivacyMode> = mutableSetOf(
        PrivacyMode.NO_CONSENT,
        PrivacyMode.NO_STORAGE,
        PrivacyMode.OPTIN,
        PrivacyMode.OPTOUT,
        PrivacyMode.EXEMPT,
        PrivacyMode.CUSTOM
    )

    /**
     * Current privacy visitor mode
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    public var currentMode: PrivacyMode = configuration.defaultPrivacyMode
        get() {
            if (consentsEnabled) {
                return pianoConsents?.let {
                    it.consents[it.productsToPurposesMapping[Product.PA]]?.mode
                }?.toPrivacyMode() ?: NO_CONSENT
            }
            if (field != PrivacyMode.NO_CONSENT && field != PrivacyMode.NO_STORAGE) {
                if (prefsStorage.privacyExpirationTimestamp in 1..System.currentTimeMillis()) {
                    currentMode = configuration.defaultPrivacyMode
                } else {
                    field = allModes.firstOrNull {
                        it.visitorMode == prefsStorage.privacyMode
                    } ?: configuration.defaultPrivacyMode
                }
            }
            cachedMode = field
            return field
        }
        set(value) {
            if (consentsEnabled) {
                Timber.w("Calling deprecated privacy mode setter while consents enabled")
            }
            require(value in allModes) {
                "Privacy mode ${value.visitorMode} is not registered."
            }
            cachedMode = value
            field = value
            updatePrefs(value)
        }

    private var cachedMode: PrivacyMode = currentMode

    // for mocking in tests
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun getNewExpirationTimestamp() =
        System.currentTimeMillis() + TimeUnit.DAYS.toMillis(configuration.privacyStorageLifetime.toLong())

    internal fun updatePrefs(mode: PrivacyMode) {
        if (consentsEnabled) {
            return
        }
        prefsStorage.apply {
            privacyMode = mode.visitorMode
            privacyExpirationTimestamp = getNewExpirationTimestamp()
            privacyVisitorConsent = mode.visitorConsent
            cachedPrivacyStorageFeatures.filter {
                !isFeatureAllowed(it)
            }.forEach {
                cleanStorageFeature(it)
            }
        }
    }

    private companion object {
        @JvmStatic
        private val cachedPrivacyStorageFeatures = enumValues<PrivacyStorageFeature>().filter {
            it != PrivacyStorageFeature.ALL
        }
    }
}
