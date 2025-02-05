package io.piano.android.analytics

import com.squareup.moshi.JsonAdapter
import io.piano.android.analytics.model.User
import java.util.concurrent.TimeUnit

/**
 * Stores user information
 */
public class UserStorage internal constructor(
    private val configuration: Configuration,
    private val prefsStorage: PrefsStorage,
    private val userJsonAdapter: JsonAdapter<User>,
) {
    private var cachedUserPreference = prefsStorage.user
    private var storedUser: User? = cachedUserPreference?.parseUser()
        get() {
            val now = getGenerationTimestamp()
            val generationTimestamp = prefsStorage.userGenerateTimestamp.takeUnless { it == 0L } ?: now
            prefsStorage.userGenerateTimestamp = generationTimestamp
            val expireTimestamp = generationTimestamp + TimeUnit.DAYS.toMillis(
                configuration.userStorageLifetime.toLong(),
            )
            if (expireTimestamp > now) {
                if (cachedUserPreference != prefsStorage.user) {
                    cachedUserPreference = prefsStorage.user
                    field = cachedUserPreference?.parseUser()
                }
            } else {
                userRecognized = false
                saveUser(null)
            }
            return field
        }

    /**
     * Current [User], set null for removing
     */
    public var currentUser: User? = null
        get() = field ?: storedUser
        set(value) {
            userRecognized = false
            if (value == null || value.shouldBeStored) {
                saveUser(value)
            }
        }

    /**
     * Returns true, if [currentUser] was loaded from storage
     */
    public var userRecognized: Boolean = storedUser != null
        private set

    @Suppress("NOTHING_TO_INLINE")
    private inline fun saveUser(value: User?) {
        storedUser = value
        cachedUserPreference = value?.let { userJsonAdapter.toJson(it) }
        prefsStorage.user = cachedUserPreference
        prefsStorage.userGenerateTimestamp = value?.let { getGenerationTimestamp() } ?: 0
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun String.parseUser(): User? = userJsonAdapter.fromJson(this)

    // for mocking in tests
    @Suppress("NOTHING_TO_INLINE")
    internal inline fun getGenerationTimestamp() = System.currentTimeMillis()
}
