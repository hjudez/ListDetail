package com.hexterlabs.listdetail.domain

/**
 * Represents a result of the search for venues query in the domain of the application.
 */
data class SearchVenuesResult(
    val id: String,
    val name: String,
    val location: String? = null,
    val distance: Int,
    val icon_url: String? = null
)


/**
 * Represents a venue in the domain of the application.
 */
data class Venue(
    val id: String,
    val name: String,
    val image_url: String,
    val rating: Double,
    val contact: String,
    val address: String,
    val description: String
)