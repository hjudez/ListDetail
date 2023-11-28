package com.hexterlabs.listdetail.network

import com.hexterlabs.listdetail.database.DatabaseSearchVenuesResult

/**
 * DataTransferObjects go in this file. These are responsible for parsing responses from the server.
 * You should convert these to database objects before using them.
 */


/**
 * Data class that represents the root object from the response of [FoursquareService.searchVenues] which looks like
 * {
 *   "response": {
 *     "venues": []
 *   }
 * }
 */
data class FoursquareSearchVenuesResponse(
    val response: FoursquareSearchVenuesResponseBody
)

/**
 * List of Venues included in the body of [FoursquareSearchVenuesResponse].
 */
data class FoursquareSearchVenuesResponseBody(
    val venues: List<FoursquareSearchVenue>
)

/**
 * Each item representing a Venue included in [FoursquareSearchVenuesResponseBody].
 */
data class FoursquareSearchVenue(
    val id: String,
    val name: String,
    val location: FoursquareSearchVenueLocation,
    val categories: List<FoursquareSearchVenueCategory>
)

/**
 * Represents a Location of a [FoursquareSearchVenue].
 */
data class FoursquareSearchVenueLocation(
    val address: String? = null,
    val distance: Int = Int.MAX_VALUE
)

/**
 * Represents a Category a [FoursquareSearchVenue] belongs to.
 */
data class FoursquareSearchVenueCategory(
    val id: String,
    val name: String,
    val icon: FoursquareSearchVenueCategoryIcon
)

/**
 * Represents the icon of a [FoursquareSearchVenueCategory].
 */
data class FoursquareSearchVenueCategoryIcon(
    val prefix: String,
    val suffix: String
)

/**
 * Converts a [FoursquareSearchVenue] API result object to a List of [DatabaseSearchVenuesResult] database objects.
 */
fun FoursquareSearchVenuesResponseBody.asDatabaseModel(): List<DatabaseSearchVenuesResult> {
    return venues.map {
        val iconUrl = if (it.categories.isNotEmpty()) {
            val prefix = it.categories[0].icon.prefix
            val suffix = it.categories[0].icon.suffix
            prefix + "bg_64" + suffix
        } else {
            null
        }
        DatabaseSearchVenuesResult(it.id, it.name, it.location.address, it.location.distance, iconUrl)
    }
}