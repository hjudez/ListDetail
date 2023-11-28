package com.hexterlabs.listdetail.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.hexterlabs.listdetail.MainDispatcherRule
import com.hexterlabs.listdetail.network.ConnectivityManager
import com.hexterlabs.listdetail.network.ConnectivityManagerImpl
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@OptIn(ExperimentalCoroutinesApi::class)
class ListDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private lateinit var tested: ListDetailViewModel

    private val flowOfConnectivity = MutableStateFlow(ConnectivityManager.ConnectionStatus.CONNECTED)

    private val connectivityManager = mockk<ConnectivityManagerImpl> {
        every { getNetworkStatus() } returns flowOfConnectivity
    }

    @Before
    fun setUp() {
        tested = ListDetailViewModel(connectivityManager)
    }

    @Test
    fun `registering callback and changes in connectivity do callback also`() = runTest {
        var connected: Boolean? = null
        var disconnected: Boolean? = null
        tested.registerForConnectivityChanges({ connected = true }, { disconnected = true })
        Assert.assertTrue(connected!!)
        Assert.assertNull(disconnected)
        connected = null
        flowOfConnectivity.emit(ConnectivityManager.ConnectionStatus.DISCONNECTED)
        Assert.assertNull(connected)
        Assert.assertTrue(disconnected!!)
        disconnected = null
        flowOfConnectivity.emit(ConnectivityManager.ConnectionStatus.CONNECTED)
        Assert.assertTrue(connected!!)
        Assert.assertNull(disconnected)
    }
}