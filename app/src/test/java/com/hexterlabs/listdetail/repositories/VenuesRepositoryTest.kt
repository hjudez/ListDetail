package com.hexterlabs.listdetail.repositories

import com.hexterlabs.listdetail.database.DatabaseSearchVenuesResult
import com.hexterlabs.listdetail.database.DatabaseVenue
import com.hexterlabs.listdetail.database.SearchVenuesResultDao
import com.hexterlabs.listdetail.database.VenueDao
import com.hexterlabs.listdetail.database.asDomainModel
import com.hexterlabs.listdetail.network.FoursquareSearchVenue
import com.hexterlabs.listdetail.network.FoursquareSearchVenueLocation
import com.hexterlabs.listdetail.network.FoursquareSearchVenuesResponse
import com.hexterlabs.listdetail.network.FoursquareSearchVenuesResponseBody
import com.hexterlabs.listdetail.network.FoursquareService
import com.hexterlabs.listdetail.network.FoursquareVenue
import com.hexterlabs.listdetail.network.FoursquareVenueResponse
import com.hexterlabs.listdetail.network.FoursquareVenueResponseBody
import com.hexterlabs.listdetail.network.asDatabaseModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VenuesRepositoryTest {

    private val foursquareVenue = FoursquareVenue(
        VENUE_ID,
        "a name"
    )

    private val foursquareVenueResponseBody = FoursquareVenueResponseBody(foursquareVenue)

    private val foursquareVenueResponse = FoursquareVenueResponse(foursquareVenueResponseBody)

    private val locationA = FoursquareSearchVenueLocation()

    private val locationB = FoursquareSearchVenueLocation()

    private val venueAFoursquare = FoursquareSearchVenue(
        "1234",
        "venueA",
        locationA,
        emptyList()
    )

    private val venueBFoursquare = FoursquareSearchVenue(
        "4321",
        "venueB",
        locationB,
        emptyList()
    )

    private val venuesList = listOf(venueAFoursquare, venueBFoursquare)

    private val foursquareSearchVenuesResponseBody = FoursquareSearchVenuesResponseBody(venuesList)

    private val foursquareSearchVenuesResponse = FoursquareSearchVenuesResponse(foursquareSearchVenuesResponseBody)

    private val venueA = DatabaseSearchVenuesResult("1", "abc", "address1")
    private val venueB = DatabaseSearchVenuesResult("2", "bca", "address2")
    private val venueC = DatabaseSearchVenuesResult("3", "cdc", "address3")

    private val venueAA = DatabaseVenue(VENUE_ID, "hello", 5.6)

    private val searchVenuesResultDao: SearchVenuesResultDao = mockk {
        every { getSearchVenues() } returns flowOf(listOf(venueA, venueB, venueC))
        coEvery { insertAll(any()) } answers {}
        coEvery { clear() } answers {}
    }

    private val venueDao: VenueDao = mockk {
        coEvery { insert(any()) } answers {}
        coEvery { clear() } answers {}
        every { getVenue(VENUE_ID) } returns flowOf(venueAA)
    }

    private val foursquareService: FoursquareService = mockk {
        coEvery { searchVenues(A_QUERY) } returns foursquareSearchVenuesResponse
        coEvery { venue(VENUE_ID) } returns foursquareVenueResponse
    }

    private val tested = VenuesRepository(searchVenuesResultDao, venueDao, foursquareService)

    @Test
    fun `get search venues returns the right flow`(): Unit = runTest {
        assertEquals(listOf(venueA, venueB, venueC).asDomainModel(), tested.searchVenuesResults.first())
    }

    @Test
    fun `search venues from the network and cache the results`(): Unit = runTest {
        val result = tested.searchVenues(A_QUERY)
        coVerify {
            foursquareService.searchVenues(A_QUERY)
            searchVenuesResultDao.insertAll(foursquareSearchVenuesResponse.response.asDatabaseModel())
        }
        assertEquals(foursquareSearchVenuesResponse.response.asDatabaseModel().asDomainModel(), result)
    }

    @Test
    fun `test clearing search venues cache`(): Unit = runTest {
        tested.clearSearchVenues()
        coVerify {
            searchVenuesResultDao.clear()
        }
    }

    @Test
    fun `test clearing venue details cache`(): Unit = runTest {
        tested.clearVenues()
        coVerify {
            venueDao.clear()
        }
    }

    @Test
    fun `load venue from the network and cache the result`(): Unit = runTest {
        val result = tested.loadVenue(VENUE_ID)
        coVerify {
            foursquareService.venue(VENUE_ID)
            venueDao.insert(foursquareVenueResponse.response.asDatabaseModel())
        }
        assertEquals(foursquareVenueResponse.response.asDatabaseModel().asDomainModel(), result)
    }

    @Test
    fun `get venue details returns the right flow`(): Unit = runTest {
        assertEquals(venueAA.asDomainModel(), tested.getVenue(VENUE_ID).first())
    }

    companion object {
        const val A_QUERY = "Albert Heijn"
        const val VENUE_ID = "2039dj2093d"
    }
}