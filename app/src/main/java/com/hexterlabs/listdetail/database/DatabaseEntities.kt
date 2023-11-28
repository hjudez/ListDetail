package com.hexterlabs.listdetail.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hexterlabs.listdetail.domain.SearchVenuesResult
import com.hexterlabs.listdetail.domain.Venue

/**
 * Represents a result of the search for venues query in the database.
 */
@Entity
data class DatabaseSearchVenuesResult(
    @PrimaryKey val id: String,
    val name: String,
    val location: String? = null,
    val distance: Int = Int.MAX_VALUE,
    val icon_url: String? = null
)

/**
 * Represents the venue details in the database.
 */
@Entity
data class DatabaseVenue(
    @PrimaryKey val id: String,
    val name: String,
    val rating: Double,
    val contact: String? = null,
    val address: String? = null,
    val description: String? = null,
    val image_url: String? = null
)

/**
 * Converts a List of [DatabaseSearchVenuesResult] Database result objects to a List of [SearchVenuesResult] domain objects.
 */
fun List<DatabaseSearchVenuesResult>.asDomainModel(): List<SearchVenuesResult> {
    return map {
        SearchVenuesResult(it.id, it.name, it.location, it.distance, it.icon_url)
    }
}

/**
 * Converts a [DatabaseVenue] Database result objects to a [Venue] domain objects.
 */
fun DatabaseVenue.asDomainModel(): Venue {
    return Venue(id, name, image_url ?: "", rating, contact ?: "", address ?: "", description ?: "")
}