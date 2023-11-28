package com.hexterlabs.listdetail.network

import android.net.NetworkRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectivityManagerTest {

    private val callback = slot<android.net.ConnectivityManager.NetworkCallback>()

    private val connectivityManager: android.net.ConnectivityManager = mockk {
        every { registerNetworkCallback(any(), capture(callback)) } answers {}
    }

    private val networkRequest: NetworkRequest = mockk()

    private val networkRequestBuilder = mockk<NetworkRequest.Builder> {
        every { addCapability(any()) } returns this
        every { build() } returns networkRequest
    }

    private val testDispatcher = UnconfinedTestDispatcher()

    private val tested = ConnectivityManagerImpl(connectivityManager, networkRequestBuilder, testDispatcher)

    @Test
    fun testInit() = runTest {
        verify {
            connectivityManager.registerNetworkCallback(networkRequest, any<android.net.ConnectivityManager.NetworkCallback>())
        }
    }

    @Test
    fun testStateOfConnection() = runTest {
        assertEquals(ConnectivityManager.ConnectionStatus.DISCONNECTED, tested.getNetworkStatus().first())
        callback.captured.onAvailable(mockk())
        assertEquals(ConnectivityManager.ConnectionStatus.CONNECTED, tested.getNetworkStatus().first())
        callback.captured.onLost(mockk())
        assertEquals(ConnectivityManager.ConnectionStatus.DISCONNECTED, tested.getNetworkStatus().first())
        callback.captured.onUnavailable()
        assertEquals(ConnectivityManager.ConnectionStatus.DISCONNECTED, tested.getNetworkStatus().first())
        callback.captured.onAvailable(mockk())
        assertEquals(ConnectivityManager.ConnectionStatus.CONNECTED, tested.getNetworkStatus().first())
    }
}