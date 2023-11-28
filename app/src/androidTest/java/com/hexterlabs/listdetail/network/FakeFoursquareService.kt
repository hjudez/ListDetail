package com.hexterlabs.listdetail.network

class FakeFoursquareService : FoursquareService {

    private val foursquareVenueLocation = FoursquareVenueLocation(listOf(VENUE_LOCATION))

    private val foursquareVenueContact = FoursquareVenueContact(VENUE_CONTACT)

    private val foursquareVenue = FoursquareVenue(
        "1234",
        VENUE_NAME,
        VENUE_RATING,
        foursquareVenueContact,
        foursquareVenueLocation,
        VENUE_DESCRIPTION
    )

    private val foursquareVenueResponseBody = FoursquareVenueResponseBody(foursquareVenue)

    private val foursquareVenueResponse = FoursquareVenueResponse(foursquareVenueResponseBody)

    private var venueResponseWithVenue: () -> FoursquareVenueResponse = { foursquareVenueResponse }

    private val locationA = FoursquareSearchVenueLocation(VENUE_A_SEARCH_LOCATION)

    private val locationB = FoursquareSearchVenueLocation(VENUE_B_SEARCH_LOCATION)

    private val venueA = FoursquareSearchVenue(
        "1234",
        VENUE_A_SEARCH_NAME,
        locationA,
        emptyList()
    )

    private val venueB = FoursquareSearchVenue(
        "4321",
        VENUE_B_SEARCH_NAME,
        locationB,
        emptyList()
    )

    private val venuesList = listOf(venueA, venueB)

    private val foursquareSearchVenuesResponseBody = FoursquareSearchVenuesResponseBody(venuesList)

    private val foursquareSearchVenuesResponse = FoursquareSearchVenuesResponse(foursquareSearchVenuesResponseBody)

    private var searchVenuesResponseWithVenues: () -> FoursquareSearchVenuesResponse = { foursquareSearchVenuesResponse }

    private var searchVenuesResponse = searchVenuesResponseWithVenues

    override suspend fun searchVenues(
        query: String,
        ll: String,
        radius: Int,
        limit: Int,
        v: String,
        client_id: String,
        client_secret: String
    ): FoursquareSearchVenuesResponse = searchVenuesResponse.invoke()


    fun setSearchVenuesResponse(searchVenuesResponse: () -> FoursquareSearchVenuesResponse) {
        this.searchVenuesResponse = searchVenuesResponse
    }

    fun resetSearchVenuesResponse() {
        setSearchVenuesResponse(searchVenuesResponseWithVenues)
    }

    private var venuesResponse = venueResponseWithVenue

    override suspend fun venue(
        id: String,
        v: String,
        client_id: String,
        client_secret: String
    ): FoursquareVenueResponse = venuesResponse.invoke()

    fun setVenueResponse(venuesResponse: () -> FoursquareVenueResponse) {
        this.venuesResponse = venuesResponse
    }

    fun resetVenueResponse() {
        setVenueResponse(venueResponseWithVenue)
    }

    companion object {
        const val VENUE_NAME = "a name"
        const val VENUE_RATING = 9.0
        const val VENUE_CONTACT = "+31648390594"
        const val VENUE_LOCATION = "An Address"
        const val VENUE_DESCRIPTION = "A very short description"

        const val VENUE_A_SEARCH_NAME = "venueA"
        const val VENUE_B_SEARCH_NAME = "venueB"
        const val VENUE_A_SEARCH_LOCATION = "location of venue A"
        const val VENUE_B_SEARCH_LOCATION = "location of venue B"
    }
}