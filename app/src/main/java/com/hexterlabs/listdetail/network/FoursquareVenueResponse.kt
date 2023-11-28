package com.hexterlabs.listdetail.network

import com.hexterlabs.listdetail.database.DatabaseVenue

/**
 * DataTransferObjects go in this file. These are responsible for parsing responses from the server.
 * You should convert these to database objects before using them.
 */


/**
 * Data class that represents the root object from the response of [FoursquareService.venue] which looks like
 * {
 *   "response": {
 *     "venue": {}
 *   }
 * }
 */
data class FoursquareVenueResponse(
    val response: FoursquareVenueResponseBody
)

/**
 * Venue included in the body of [FoursquareVenueResponse].
 */
data class FoursquareVenueResponseBody(
    val venue: FoursquareVenue
)

/**
 * Represents the Venue included in [FoursquareVenueResponse].
 */
data class FoursquareVenue(
    val id: String,
    val name: String,
    val rating: Double = Double.MAX_VALUE,
    val contact: FoursquareVenueContact? = null,
    val location: FoursquareVenueLocation? = null,
    val description: String? = null,
    val bestPhoto: FoursquareVenueBestPhoto? = null
)

/**
 * Represents the best photo of a [FoursquareVenue].
 */
data class FoursquareVenueBestPhoto(
    val prefix: String,
    val suffix: String
)

/**
 * Represents the contact information of a [FoursquareVenue].
 */
data class FoursquareVenueContact(
    val formattedPhone: String? = null
)

/**
 * Represents the location of a [FoursquareVenue].
 */
data class FoursquareVenueLocation(
    val formattedAddress: List<String>?
)

/**
 * Converts a [FoursquareVenue] API result object to a [DatabaseVenue] database objects.
 */
fun FoursquareVenueResponseBody.asDatabaseModel(): DatabaseVenue {
    val imageUrl = venue.bestPhoto?.let { venue.bestPhoto.prefix + "500x300" + venue.bestPhoto.suffix }
    return DatabaseVenue(
        venue.id,
        venue.name,
        venue.rating,
        venue.contact?.formattedPhone,
        venue.location?.formattedAddress?.joinToString("\n"),
        venue.description,
        imageUrl
    )
}