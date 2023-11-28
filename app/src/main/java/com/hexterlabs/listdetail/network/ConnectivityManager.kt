package com.hexterlabs.listdetail.network

import kotlinx.coroutines.flow.Flow

interface ConnectivityManager {
    enum class ConnectionStatus { CONNECTED, DISCONNECTED }

    fun getNetworkStatus(): Flow<ConnectionStatus>
}