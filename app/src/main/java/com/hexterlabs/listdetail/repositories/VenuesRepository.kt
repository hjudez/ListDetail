package com.hexterlabs.listdetail.repositories

import com.hexterlabs.listdetail.database.SearchVenuesResultDao
import com.hexterlabs.listdetail.database.VenueDao
import com.hexterlabs.listdetail.database.asDomainModel
import com.hexterlabs.listdetail.domain.SearchVenuesResult
import com.hexterlabs.listdetail.domain.Venue
import com.hexterlabs.listdetail.network.FoursquareService
import com.hexterlabs.listdetail.network.asDatabaseModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that isolates the venues data layer from the rest of the app.
 * It mediates between the network source and the database source providing a clean API for data access to the rest of the app.
 */

@Singleton
class VenuesRepository @Inject constructor(
    private val searchVenuesResultDao: SearchVenuesResultDao,
    private val venueDao: VenueDao,
    private val foursquareService: FoursquareService
) {

    /**
     * Flow that emits the last cached "search for venues" result and subsequent updates.
     */
    val searchVenuesResults: Flow<List<SearchVenuesResult>> =
        searchVenuesResultDao.getSearchVenues()
            .map {
                it.asDomainModel()
            }

    /**
     * Method that searches for venues on the network and stores them in the cache.
     * Updates on the cache can be observed by subscribing to the Flow [searchVenuesResults].
     *
     * @param query A search term to be applied against venue names. Could be empty to search for all venues.
     * @return a list with the results of the search action. Could be empty if no venues where found with the passed query.
     */
    suspend fun searchVenues(query: String = ""): List<SearchVenuesResult> {
        Timber.d("searchVenues($query)")
        // fetch the venues from the network.
        val searchVenuesResponse = foursquareService.searchVenues(query).response
        // cache the result in the database.
        val searchVenuesResponseAsDatabaseModel = searchVenuesResponse.asDatabaseModel()
        searchVenuesResultDao.insertAll(searchVenuesResponseAsDatabaseModel)
        return searchVenuesResponseAsDatabaseModel.asDomainModel()
    }

    /**
     * Clears the cache that stores the last search for venues.
     */
    suspend fun clearSearchVenues() {
        Timber.d("clearSearchVenues()")
        searchVenuesResultDao.clear()
    }

    /**
     * Method that loads the details of a venue from the network and stores it in the cache.
     * Updates on the cache can be observed by subscribing to the Flow returned by [getVenue].
     *
     * If the venue is not found in the server, this method will throw an HttpException: HTTP 400.
     *
     * @param id of the venue we want to load from the network.
     * @return the venue identified with the id passed as a parameter.
     */
    suspend fun loadVenue(id: String): Venue {
        Timber.d("loadVenue($id)")
        // fetch the venue from the network.
        val venueResponse = foursquareService.venue(id).response
        // cache the result in the database.
        val venueResponseAsDatabaseModel = venueResponse.asDatabaseModel()
        venueDao.insert(venueResponseAsDatabaseModel)
        return venueResponseAsDatabaseModel.asDomainModel()
    }

    /**
     * @return a Flow that emits the last cached venue identified by the id passed as a parameter, could be null.
     */
    fun getVenue(id: String): Flow<Venue?> {
        return venueDao.getVenue(id).map {
            it?.asDomainModel()
        }
    }

    /**
     * @return whether or not the venue with this ID is stored in the cache.
     *
     * @param id of the venue to check if it is in the cache.
     */
    suspend fun isVenueInCache(id: String): Boolean {
        Timber.d("isVenueInCache($id)")
        return venueDao.countVenue(id) > 0
    }

    /**
     * Clears the cache that stores the details of every venue.
     */
    suspend fun clearVenues() {
        Timber.d("clearVenues()")
        venueDao.clear()
    }
}