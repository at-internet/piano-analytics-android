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
public class User(
    @Json(name = "id")
    public val id: String,
    @Json(name = "category")
    public val category: String?,
    @Json(ignore = true)
    public val shouldBeStored: Boolean = true,
)
