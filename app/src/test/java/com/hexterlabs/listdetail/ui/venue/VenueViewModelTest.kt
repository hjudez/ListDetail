package com.hexterlabs.listdetail.ui.venue

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.hexterlabs.listdetail.MainDispatcherRule
import com.hexterlabs.listdetail.domain.Venue
import com.hexterlabs.listdetail.getOrAwaitValue
import com.hexterlabs.listdetail.network.ConnectivityManager
import com.hexterlabs.listdetail.network.ConnectivityManagerImpl
import com.hexterlabs.listdetail.repositories.VenuesRepository
import com.hexterlabs.listdetail.ui.ListDetailViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@OptIn(ExperimentalCoroutinesApi::class)
class VenueViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private lateinit var tested: VenueViewModel

    private val state = SavedStateHandle()

    private val venueA = Venue(VENUE_A_ID, "abc", "pepe", 5.0, "contact1", "address1", "description1")

    private val venueResult = MutableStateFlow(venueA)
    private val flowOfConnectivity = MutableStateFlow(ConnectivityManager.ConnectionStatus.CONNECTED)

    private val venuesRepository = mockk<VenuesRepository> {
        coEvery { loadVenue(VENUE_A_ID) } returns venueA
        every { getVenue(ERROR_ID) } returns flowOf(null)
        every { getVenue(VENUE_A_ID) } returns venueResult
        coEvery { isVenueInCache(VENUE_A_ID) } returns false
        coEvery { isVenueInCache(ERROR_ID) } returns false
    }

    private val connectivityManager = mockk<ConnectivityManagerImpl> {
        every { getNetworkStatus() } returns flowOfConnectivity
    }

    @Test
    fun `init triggers search with ID`() = runTest {
        state[VenueViewModel.PARAM_ID] = VENUE_A_ID
        tested = VenueViewModel(venuesRepository, connectivityManager, state)
        coVerify {
            venuesRepository.loadVenue(VENUE_A_ID)
        }
        assertEquals(venueA, tested.venue.getOrAwaitValue())
        assertEquals(ListDetailViewModel.RefreshDataStatus.SUCCESS, tested.refreshDataState.value)
    }

    @Test
    fun `init with a failed id in state triggers load with failed id`() = runTest {
        state[VenueViewModel.PARAM_ID] = VENUE_A_ID
        state[VenueViewModel.LOAD_FAILED_KEY] = true
        tested = VenueViewModel(venuesRepository, connectivityManager, state)
        coVerify {
            venuesRepository.loadVenue(VENUE_A_ID)
        }
    }

    @Test
    fun `search that triggers an error sets error in UI if not cached`() = runTest {
        state[VenueViewModel.PARAM_ID] = ERROR_ID // since we didn't configure the mock to answer to this ID, it will throw an error.
        tested = VenueViewModel(venuesRepository, connectivityManager, state)
        coVerify {
            venuesRepository.loadVenue(ERROR_ID)
        }
        assertTrue(state[VenueViewModel.LOAD_FAILED_KEY]!!)
        assertEquals(ListDetailViewModel.RefreshDataStatus.FAILURE, tested.refreshDataState.value)
    }

    @Test
    fun `search that triggers an error does NOT set error in UI if cached`() = runTest {
        coEvery { venuesRepository.isVenueInCache(ERROR_ID) } returns true
        state[VenueViewModel.PARAM_ID] = ERROR_ID // since we didn't configure the mock to answer to this ID, it will throw an error.
        tested = VenueViewModel(venuesRepository, connectivityManager, state)
        coVerify {
            venuesRepository.loadVenue(ERROR_ID)
        }
        assertTrue(state[VenueViewModel.LOAD_FAILED_KEY]!!)
        assertEquals(ListDetailViewModel.RefreshDataStatus.SUCCESS, tested.refreshDataState.value)
    }

    @Test
    fun `connecting to internet after error triggers a load with same id`() = runTest {
        flowOfConnectivity.emit(ConnectivityManager.ConnectionStatus.DISCONNECTED)
        `search that triggers an error sets error in UI if not cached`()
        flowOfConnectivity.emit(ConnectivityManager.ConnectionStatus.CONNECTED)
        coVerify(atLeast = 2) { // 2 times called
            venuesRepository.loadVenue(ERROR_ID)
        }
    }

    companion object {
        const val VENUE_A_ID = "1234"
        const val ERROR_ID = "An error query"
    }
}