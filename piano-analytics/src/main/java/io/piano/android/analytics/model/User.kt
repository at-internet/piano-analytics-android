package io.piano.android.analytics.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * User information
 *
 * @param id user id
 * @param category user category
 * @param shouldBeStored specifies user persistence
 * @constructor Creates user object
 */
@JsonClass(generateAdapter = true)
class User(
    @Json(name = "id")
    val id: String,
    @Json(name = "category")
    val category: String?,
    @Json(ignore = true)
    val shouldBeStored: Boolean = true,
)
