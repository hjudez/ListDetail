package com.hexterlabs.listdetail.ui.venue

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.hexterlabs.listdetail.domain.Venue
import com.hexterlabs.listdetail.network.ConnectivityManager
import com.hexterlabs.listdetail.repositories.VenuesRepository
import com.hexterlabs.listdetail.ui.ListDetailViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

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
    val venue: LiveData<Venue?> = venuesRepository.getVenue(id).asLiveData()

    init {
        viewModelScope.launch {
            if (getPreviousLoadFailed() == null) {
                Timber.d("init firing load of venue details")
                loadVenue()
            } else if (getPreviousLoadFailed() == true && !venuesRepository.isVenueInCache(id)) {
                updateRefreshDataStatus(RefreshDataStatus.FAILURE)
            }
        }
        registerForConnectivityChanges(onConnectivityConnected = { onConnectivityConnected() })
    }

    /**
     * Load venue details. To get the results of this call you must subscribe to the LiveData [venue].
     */
    private suspend fun loadVenue() = coroutineScope {
        Timber.d("load venue requested: $id")
        setPreviousLoadFailed(false)
        updateRefreshDataStatus(RefreshDataStatus.LOADING)
        launch {
            try {
                venuesRepository.loadVenue(id)
                updateRefreshDataStatus(RefreshDataStatus.SUCCESS)
            } catch (e: Exception) {
                if (e !is CancellationException) { // Let's make sure we don't catch the CancellationException.
                    // For simplicity we're going to catch all the errors and show them as the network being broken.
                    Timber.e("loadVenue error: ${e.message}")
                    setPreviousLoadFailed(true)
                    if (venuesRepository.isVenueInCache(id)) {
                        // If we have already cached this venue before we don't want to show any error to the user.
                        updateRefreshDataStatus(RefreshDataStatus.SUCCESS)
                    } else {
                        updateRefreshDataStatus(RefreshDataStatus.FAILURE)
                    }
                }
            }
        }
    }

    /**
     * @returns whether or not the previous load failed from the saved state.
     */
    private fun getPreviousLoadFailed(): Boolean? = state[LOAD_FAILED_KEY]

    /**
     * Stores whether or not the previous load failed in the saved state.
     *
     * @param failed true if the previous load failed, false otherwise.
     */
    private fun setPreviousLoadFailed(failed: Boolean) {
        state[LOAD_FAILED_KEY] = failed
    }

    /**
     * When connection comes back we check if there was any pending request that failed (probably because the connection was lost).
     * If so, if getPreviousLoadFailed is true, then we fire this request again.
     */
    private suspend fun onConnectivityConnected() {
        val previousLoadFailed = getPreviousLoadFailed()
        if (previousLoadFailed == true) {
            loadVenue()
        }
    }

    companion object {
        const val PARAM_ID = "id"
        const val LOAD_FAILED_KEY = "query_failed"
    }
}