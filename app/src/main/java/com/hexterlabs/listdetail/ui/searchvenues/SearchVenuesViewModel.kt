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
        if (getSucceededSearchQuery() == null && getFailedSearchQuery() == null) {
            Timber.d("***hjs*** init call, let's search for all venues")
            searchVenues(getSearchingQuery() ?: "", true)
        } else if (getFailedSearchQuery() != null) {
            Timber.d("***hjs*** init error state")
            updateRefreshDataStatus(RefreshDataStatus.FAILURE)
        }
        registerForConnectivityChanges(onConnectivityConnected = { onConnectivityConnected() })
    }

    /**
     * Search for venues. To get the results of this call you must subscribe to the SateFlow [searchVenuesState].
     *
     * @param query A search term to be applied against venue names. Could be empty to search for all venues.
     */
    fun searchVenues(query: String, init: Boolean = false) {
        Timber.d("***hjs*** searchVenues search requested: $query")
        if (!init && (getSearchingQuery() == query || getSucceededSearchQuery() == query)) return // ignore if the query is the same. This could happen after rotating the device.
        updateRefreshDataStatus(RefreshDataStatus.LOADING)
        setSearchingQuery(query)
        setSucceededSearchQuery(null)
        setFailedSearchQuery(null)
        previousSearchVenuesJob?.cancel()
        previousSearchVenuesJob = viewModelScope.launch {
            venuesRepository.clearSearchVenues()
            delay(QUERY_SEARCH_DELAY_IN_MILLIS) // short delay so we don't fire too many requests while the user is typing.
            try {
                Timber.d("***hjs*** searchVenues firing search: $query")
                if (venuesRepository.searchVenues(query).isNotEmpty()) {
                    updateRefreshDataStatus(RefreshDataStatus.SUCCESS)
                } else {
                    updateRefreshDataStatus(RefreshDataStatus.NOT_FOUND)
                }
                setSearchingQuery(null)
                setSucceededSearchQuery(query)
            } catch (e: Exception) {
                // For simplicity we're going to catch all the errors and show them as the network being broken.
                if (e !is CancellationException) { // Let's make sure we don't catch the CancellationException.
                    Timber.e("***hjs*** searchVenues error: ${e.message}")
                    setSearchingQuery(null)
                    setFailedSearchQuery(query)
                    updateRefreshDataStatus(RefreshDataStatus.FAILURE)
                } else {
                    throw e
                }
            }
        }
    }

    /**
     * @returns the current search query from the saved state.
     */
    private fun getSearchingQuery(): String? = state[QUERY_KEY]

    /**
     * Stores the current search query in the saved state.
     *
     * @param query to store in the saved state.
     */
    private fun setSearchingQuery(query: String?) {
        state[QUERY_KEY] = query
    }

    /**
     * @returns the previous fired search query that succeeded from the saved state.
     */
    private fun getSucceededSearchQuery(): String? = state[QUERY_SUCCEEDED_KEY]

    /**
     * Stores the last fired search query that succeeded in the saved state.
     *
     * @param query to store in the saved state.
     */
    private fun setSucceededSearchQuery(query: String?) {
        state[QUERY_SUCCEEDED_KEY] = query
    }

    /**
     * @returns the previous fired search query that failed from the saved state.
     */
    private fun getFailedSearchQuery(): String? = state[QUERY_FAILED_KEY]

    /**
     * Stores the last fired search query that failed in the saved state.
     *
     * @param query to store in the saved state.
     */
    private fun setFailedSearchQuery(query: String?) {
        state[QUERY_FAILED_KEY] = query
    }

    /**
     * When connection comes back we check if there was any pending request that failed (probably because the connection was lost).
     * If so, if there is a previousFailedSearchQuery, then we fire this request again.
     */
    private fun onConnectivityConnected() {
        val previousFailedSearchQuery = getFailedSearchQuery()
        if (previousFailedSearchQuery != null) {
            searchVenues(previousFailedSearchQuery)
        }
    }

    companion object {
        const val QUERY_SEARCH_DELAY_IN_MILLIS = 400L
        const val QUERY_KEY = "query"
        const val QUERY_SUCCEEDED_KEY = "query_succeeded"
        const val QUERY_FAILED_KEY = "query_failed"
    }
}