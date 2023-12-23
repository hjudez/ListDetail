package com.hexterlabs.listdetail.network

import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber

class ConnectivityManagerImpl(
    connectivityManager: android.net.ConnectivityManager,
    networkRequestBuilder: NetworkRequest.Builder,
    coroutineDispatcher: CoroutineDispatcher
) : ConnectivityManager {

    private val coroutineScope = CoroutineScope(coroutineDispatcher)

    private val request = networkRequestBuilder
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    private val networkStatus = callbackFlow {
        val callback = object : android.net.ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Timber.d("***hjs*** callbackFlow - networkStatus onAvailable")
                trySend(ConnectivityManager.ConnectionStatus.CONNECTED)
            }

            override fun onLost(network: Network) {
                Timber.d("***hjs*** callbackFlow - networkStatus onLost")
                trySend(ConnectivityManager.ConnectionStatus.DISCONNECTED)
            }

            override fun onUnavailable() {
                Timber.d("***hjs*** callbackFlow - networkStatus onUnavailable")
                trySend(ConnectivityManager.ConnectionStatus.DISCONNECTED)
            }
        }
        connectivityManager.registerNetworkCallback(request, callback)
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.stateIn(
        coroutineScope,
        started = SharingStarted.Eagerly,
        ConnectivityManager.ConnectionStatus.DISCONNECTED
    )

    override fun getNetworkStatus(): Flow<ConnectivityManager.ConnectionStatus> = networkStatus
}