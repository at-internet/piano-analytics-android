package io.piano.android.analytics

import io.piano.android.analytics.model.PrivacyMode
import io.piano.android.analytics.model.PrivacyStorageFeature
import java.util.concurrent.TimeUnit

/**
 * Stores information about privacy modes
 */
public class PrivacyModesStorage internal constructor(
    private val configuration: Configuration,
    private val prefsStorage: PrefsStorage,
) {
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
        PrivacyMode.EXEMPT
    )

    /**
     * Current privacy visitor mode
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    public var currentMode: PrivacyMode = configuration.defaultPrivacyMode
        get() {
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
