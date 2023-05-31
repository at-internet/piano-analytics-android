package io.piano.android.analytics

import io.piano.android.analytics.model.PrivacyMode
import io.piano.android.analytics.model.PrivacyStorageFeature
import java.util.concurrent.TimeUnit

/**
 * Stores information about privacy modes
 */
class PrivacyModesStorage internal constructor(
    private val configuration: Configuration,
    private val prefsStorage: PrefsStorage
) {
    init {
        prefsStorage.privacyStorageFilter = { privacyStorageFeature ->
            val isNotForbidden = PrivacyStorageFeature.ALL !in currentMode.forbiddenStorageFeatures ||
                privacyStorageFeature !in currentMode.forbiddenStorageFeatures
            val isAllowed = PrivacyStorageFeature.ALL in currentMode.allowedStorageFeatures ||
                privacyStorageFeature in currentMode.allowedStorageFeatures
            isNotForbidden && isAllowed
        }
    }

    /**
     * Current privacy visitor mode
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    var currentMode: PrivacyMode = configuration.defaultPrivacyMode
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
            return field
        }
        set(value) {
            require(value in allModes) {
                "Privacy mode ${value.visitorMode} is not registered."
            }
            field = value
            updatePrefs(value)
        }

    /**
     * All registered privacy modes. Add a [PrivacyMode] instance into [allModes] for registering it
     */
    @Suppress("unused", "MemberVisibilityCanBePrivate") // Public API.
    val allModes = mutableSetOf(
        PrivacyMode.NO_CONSENT,
        PrivacyMode.NO_STORAGE,
        PrivacyMode.OPTIN,
        PrivacyMode.OPTOUT,
        PrivacyMode.EXEMPT,
    )

    // for mocking in tests
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun getNewExpirationTimestamp() =
        System.currentTimeMillis() + TimeUnit.DAYS.toMillis(configuration.privacyStorageLifetime.toLong())

    internal fun updatePrefs(mode: PrivacyMode) {
        cachedPrivacyStorageFeatures.filter {
            it in mode.forbiddenStorageFeatures || it !in mode.allowedStorageFeatures
        }.forEach {
            prefsStorage.cleanStorageFeature(it)
        }
        prefsStorage.apply {
            privacyMode = mode.visitorMode
            privacyExpirationTimestamp = getNewExpirationTimestamp()
            privacyVisitorConsent = mode.visitorConsent
        }
    }

    companion object {
        @JvmStatic
        private val cachedPrivacyStorageFeatures = enumValues<PrivacyStorageFeature>().filter {
            it != PrivacyStorageFeature.ALL
        }
    }
}
