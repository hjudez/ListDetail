package com.hexterlabs.listdetail.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hexterlabs.listdetail.network.ConnectivityManager
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel that acts as a service layer, abstracting common functionality to all the viewModels.
 */
open class ListDetailViewModel(
    private val connectivityManager: ConnectivityManager
) : ViewModel() {

    /**
     * Represents the state of refreshing the main data from the network layer.
     */
    enum class RefreshDataStatus { LOADING, SUCCESS, NOT_FOUND, FAILURE }

    /**
     * MutableLiveData where we can set current state of refreshing the data from the network layer.
     */
    private val _refreshDataState = MutableLiveData<RefreshDataStatus>()

    /**
     * Observable that emits the current state of refreshing the data from the network layer.
     */
    val refreshDataState: LiveData<RefreshDataStatus> = _refreshDataState

    /**
     * Register to receive notifications about connectivity changes.
     * Calling this method will trigger a callback right away.
     */
    fun registerForConnectivityChanges(
        onConnectivityConnected: (suspend () -> Unit)? = null,
        onConnectivityDisconnected: (suspend () -> Unit)? = null
    ) {
        viewModelScope.launch {
            connectivityManager.getNetworkStatus().collect {
                // Let's observe for connectivity changes...
                if (it == ConnectivityManager.ConnectionStatus.CONNECTED) {
                    Timber.d("***hjs*** registerForConnectivityChanges() CONNECTED")
                    onConnectivityConnected?.invoke()
                } else {
                    Timber.d("***hjs*** registerForConnectivityChanges() DISCONNECTED")
                    onConnectivityDisconnected?.invoke()
                }
            }
        }
    }

    fun updateRefreshDataStatus(refreshDataStatus: RefreshDataStatus) {
        _refreshDataState.value = refreshDataStatus
    }
}