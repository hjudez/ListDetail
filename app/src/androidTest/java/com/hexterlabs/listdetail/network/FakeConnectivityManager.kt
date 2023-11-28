package com.hexterlabs.listdetail.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeConnectivityManager : ConnectivityManager {

    private val connectionStateFlow = MutableStateFlow(ConnectivityManager.ConnectionStatus.CONNECTED)

    override fun getNetworkStatus(): Flow<ConnectivityManager.ConnectionStatus> {
        return connectionStateFlow
    }

    suspend fun sendConnected() {
        connectionStateFlow.emit(ConnectivityManager.ConnectionStatus.CONNECTED)
    }

    suspend fun sendDisconnected() {
        connectionStateFlow.emit(ConnectivityManager.ConnectionStatus.DISCONNECTED)
    }
}