package com.hexterlabs.listdetail.ui.searchvenues

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.hexterlabs.listdetail.domain.SearchVenuesResult
import com.hexterlabs.listdetail.network.ConnectivityManager
import com.hexterlabs.listdetail.repositories.VenuesRepository
import com.hexterlabs.listdetail.ui.ListDetailViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted.Companion.Eagerly
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchVenuesViewModel @Inject constructor(
    private val venuesRepository: VenuesRepository,
    connectivityManager: ConnectivityManager,
    private val state: SavedStateHandle
) : ListDetailViewModel(connectivityManager) {

    /**
     * Observable that emits the list of [SearchVenuesResult] for the last search query.
     */
    val searchVenuesState: StateFlow<List<SearchVenuesResult>?> =
        venuesRepository.searchVenuesResults.stateIn(viewModelScope, Eagerly, null)

    /**
     * Holds a reference to the previous search venues job so we can cancel it in case the user triggers another
     * request in less than [QUERY_SEARCH_DELAY_IN_MILLIS].
     */
    private var previousSearchVenuesJob: Job? = null

    init {
        // Lets start with some random venues the very first time we open the app.
        if (getPreviousSearchQuery() == null && getPreviousFailedSearchQuery() == null) {
            Timber.d("init firing search with empty string")
            searchVenues("")
        } else if (getPreviousFailedSearchQuery() != null) {
            updateRefreshDataStatus(RefreshDataStatus.FAILURE)
        }
        registerForConnectivityChanges(onConnectivityConnected = { onConnectivityConnected() })
    }

    /**
     * Search for venues. To get the results of this call you must subscribe to the SateFlow [searchVenuesState].
     *
     * @param query A search term to be applied against venue names. Could be empty to search for all venues.
     */
    fun searchVenues(query: String) {
        Timber.d("searchVenues search requested: $query")
        if (getPreviousSearchQuery() == query) return // ignore if the query is the same. This could happen after rotating the device.
        setPreviousSearchQuery(query)
        setPreviousFailedSearchQuery(null)
        updateRefreshDataStatus(RefreshDataStatus.LOADING)
        previousSearchVenuesJob?.cancel()
        previousSearchVenuesJob = viewModelScope.launch {
            venuesRepository.clearSearchVenues()
            delay(QUERY_SEARCH_DELAY_IN_MILLIS) // short delay so we don't fire too many requests while the user is typing.
            try {
                Timber.d("searchVenues firing search: $query")
                if (venuesRepository.searchVenues(query).isNotEmpty()) {
                    updateRefreshDataStatus(RefreshDataStatus.SUCCESS)
                } else {
                    updateRefreshDataStatus(RefreshDataStatus.NOT_FOUND)
                }
            } catch (e: Exception) {
                if (e !is CancellationException) { // Let's make sure we don't catch the CancellationException.
                    // For simplicity we're going to catch all the errors and show them as the network being broken.
                    Timber.e("searchVenues error: ${e.message}")
                    setPreviousSearchQuery(null)
                    setPreviousFailedSearchQuery(query)
                    updateRefreshDataStatus(RefreshDataStatus.FAILURE)
                }
            }
        }
    }

    /**
     * @returns the previous fired search query from the saved state.
     */
    private fun getPreviousSearchQuery(): String? = state[QUERY_KEY]

    /**
     * Stores the last fired search query in the saved state.
     *
     * @param query to store in the saved state.
     */
    private fun setPreviousSearchQuery(query: String?) {
        state[QUERY_KEY] = query
    }

    /**
     * @returns the previous fired search query that failed from the saved state.
     */
    private fun getPreviousFailedSearchQuery(): String? = state[QUERY_FAILED_KEY]

    /**
     * Stores the last fired search query that failed in the saved state.
     *
     * @param query to store in the saved state.
     */
    private fun setPreviousFailedSearchQuery(query: String?) {
        state[QUERY_FAILED_KEY] = query
    }

    /**
     * When connection comes back we check if there was any pending request that failed (probably because the connection was lost).
     * If so, if there is a previousFailedSearchQuery, then we fire this request again.
     */
    private fun onConnectivityConnected() {
        val previousFailedSearchQuery = getPreviousFailedSearchQuery()
        if (previousFailedSearchQuery != null) {
            searchVenues(previousFailedSearchQuery)
        }
    }

    companion object {
        const val QUERY_SEARCH_DELAY_IN_MILLIS = 400L
        const val QUERY_KEY = "query"
        const val QUERY_FAILED_KEY = "query_failed"
    }
}