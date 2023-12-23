package com.hexterlabs.listdetail.ui.venue

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.hexterlabs.listdetail.domain.Venue
import com.hexterlabs.listdetail.network.ConnectivityManager
import com.hexterlabs.listdetail.repositories.VenuesRepository
import com.hexterlabs.listdetail.ui.ListDetailViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class VenueViewModel @Inject constructor(
    private val venuesRepository: VenuesRepository,
    connectivityManager: ConnectivityManager,
    private val state: SavedStateHandle
) : ListDetailViewModel(connectivityManager) {

    private val id: String = state[PARAM_ID] ?: throw IllegalStateException("Param $PARAM_ID missing")

    /**
     * Observable that emits the [Venue] loaded by this viewModel.
     */
    val venue: StateFlow<Venue?> =
        venuesRepository.getVenue(id).stateIn(viewModelScope, SharingStarted.Eagerly, null)

    init {
        viewModelScope.launch {
            if (getLoadingVenueSucceeded() == null) {
                Timber.d("***hjs*** init firing load of venue details")
                loadVenue()
            } else {
                Timber.d("***hjs*** init update previous state")
                updateRefreshDataStatusBasedOnCache()
            }
            registerForConnectivityChanges(onConnectivityConnected = { onConnectivityConnected() })
        }
    }

    /**
     * Load venue details. To get the results of this call you must subscribe to the StateFlow [venue].
     */
    private suspend fun loadVenue() {
        Timber.d("***hjs*** loading venue requested: $id")
        updateRefreshDataStatus(RefreshDataStatus.LOADING)
        try {
            venuesRepository.loadVenue(id)
            setLoadingVenueSucceeded(true)
            updateRefreshDataStatus(RefreshDataStatus.SUCCESS)
        } catch (e: Exception) {
            Timber.e("***hjs*** loading venue error: ${e.message}")
            // For simplicity we're going to catch all the errors and show them as the network being broken.
            if (e !is CancellationException) { // Let's make sure we don't catch the CancellationException.
                setLoadingVenueSucceeded(false)
                updateRefreshDataStatusBasedOnCache()
            } else {
                throw e
            }
        }
    }

    /**
     * @returns from the saved state whether or not loading the venue has succeeded.
     */
    private fun getLoadingVenueSucceeded(): Boolean? = state[LOAD_SUCCEEDED_KEY]

    /**
     * Stores in the saved state whether or not the loading the venue has succeeded.
     *
     * @param failed true if loading the venue failed, false otherwise.
     */
    private fun setLoadingVenueSucceeded(failed: Boolean) {
        state[LOAD_SUCCEEDED_KEY] = failed
    }

    /**
     * Helper method to update the refresh data state based on whether or not the venue is already cached.
     */
    private suspend fun updateRefreshDataStatusBasedOnCache() {
        if (venuesRepository.isVenueInCache(id)) {
            // If we have already cached this venue before we don't want to show any error to the user.
            updateRefreshDataStatus(RefreshDataStatus.SUCCESS)
        } else {
            updateRefreshDataStatus(RefreshDataStatus.FAILURE)
        }
    }

    /**
     * When connection comes back we check if there was any pending request that failed (probably because the connection was lost).
     * If so, if loadingVenueSucceeded is false, then we fire this request again.
     */
    private suspend fun onConnectivityConnected() {
        Timber.d("***hjs*** onConnectivityConnected()")
        val loadingVenueSucceeded = getLoadingVenueSucceeded()
        if (loadingVenueSucceeded == false) {
            loadVenue()
        }
    }

    companion object {
        const val PARAM_ID = "venueId"
        const val LOAD_SUCCEEDED_KEY = "loading_venue_succeeded"
    }
}