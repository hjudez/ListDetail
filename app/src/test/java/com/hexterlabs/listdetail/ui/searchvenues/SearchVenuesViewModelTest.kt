package com.hexterlabs.listdetail.ui.searchvenues

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.hexterlabs.listdetail.MainDispatcherRule
import com.hexterlabs.listdetail.domain.SearchVenuesResult
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@OptIn(ExperimentalCoroutinesApi::class)
class SearchVenuesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private lateinit var tested: SearchVenuesViewModel

    private val state = SavedStateHandle()

    private val venueA = SearchVenuesResult("1", "abc", "address1", 10)
    private val venueB = SearchVenuesResult("2", "bca", "address2", 15)
    private val listOfSearchVenuesResult = listOf(venueA, venueB)

    private val flowOfSearchVenuesResult = MutableStateFlow(listOfSearchVenuesResult)
    private val flowOfConnectivity = MutableStateFlow(ConnectivityManager.ConnectionStatus.CONNECTED)

    private val venuesRepository = mockk<VenuesRepository> {
        every { searchVenuesResults } returns flowOfSearchVenuesResult
        coEvery { clearSearchVenues() } answers {}
        coEvery { searchVenues() } coAnswers { flowOfSearchVenuesResult.first() }
        coEvery { searchVenues(EMPTY_RESULTS_QUERY) } coAnswers { emptyList() }
    }

    private val connectivityManager = mockk<ConnectivityManagerImpl> {
        every { getNetworkStatus() } returns flowOfConnectivity
    }

    @Test
    fun `init triggers search with empty string`() = runTest {
        tested = SearchVenuesViewModel(venuesRepository, connectivityManager, state)
        assertEquals(ListDetailViewModel.RefreshDataStatus.LOADING, tested.refreshDataState.value)
        advanceUntilIdle()
        coVerify {
            venuesRepository.clearSearchVenues()
            venuesRepository.searchVenues("")
        }
        assertEquals(listOfSearchVenuesResult, tested.searchVenuesResults.getOrAwaitValue())
        assertEquals(ListDetailViewModel.RefreshDataStatus.SUCCESS, tested.refreshDataState.value)
    }

    @Test
    fun `init with a failed query in state triggers search with failed query`() = runTest {
        state[SearchVenuesViewModel.QUERY_FAILED_KEY] = FAILED_QUERY
        tested = SearchVenuesViewModel(venuesRepository, connectivityManager, state)
        assertEquals(ListDetailViewModel.RefreshDataStatus.LOADING, tested.refreshDataState.value)
        advanceUntilIdle()
        coVerify {
            venuesRepository.clearSearchVenues()
            venuesRepository.searchVenues(FAILED_QUERY)
        }
    }

    @Test
    fun `search that returns empty results sets no results in UI`() = runTest {
        flowOfSearchVenuesResult.emit(emptyList())
        tested = SearchVenuesViewModel(venuesRepository, connectivityManager, state)
        tested.searchVenues(EMPTY_RESULTS_QUERY)
        advanceUntilIdle()
        coVerify {
            venuesRepository.clearSearchVenues()
            venuesRepository.searchVenues(EMPTY_RESULTS_QUERY)
        }
        assertEquals(emptyList<SearchVenuesResult>(), tested.searchVenuesResults.getOrAwaitValue())
        assertEquals(ListDetailViewModel.RefreshDataStatus.NOT_FOUND, tested.refreshDataState.value)
    }

    @Test
    fun `search that triggers an error sets error in UI`() = runTest {
        tested = SearchVenuesViewModel(venuesRepository, connectivityManager, state)
        tested.searchVenues(ERROR_QUERY) // since we didn't configure the mock to answer to this query, it will throw an error.
        advanceUntilIdle()
        coVerify {
            venuesRepository.clearSearchVenues()
            venuesRepository.searchVenues(ERROR_QUERY)
        }
        assertEquals(ERROR_QUERY, state[SearchVenuesViewModel.QUERY_FAILED_KEY])
        assertEquals(ListDetailViewModel.RefreshDataStatus.FAILURE, tested.refreshDataState.value)
    }

    @Test
    fun `connecting to internet after error triggers a search with same query`() = runTest {
        flowOfConnectivity.emit(ConnectivityManager.ConnectionStatus.DISCONNECTED)
        `search that triggers an error sets error in UI`()
        flowOfConnectivity.emit(ConnectivityManager.ConnectionStatus.CONNECTED)
        advanceUntilIdle()
        coVerify(atLeast = 2) { // 2 times called
            venuesRepository.clearSearchVenues()
            venuesRepository.searchVenues(ERROR_QUERY)
        }
    }

    @Test
    fun `trying to search again with the same query does not trigger a new search`() = runTest {
        tested = SearchVenuesViewModel(venuesRepository, connectivityManager, state)
        tested.searchVenues(EMPTY_RESULTS_QUERY)
        advanceUntilIdle()
        tested.searchVenues(EMPTY_RESULTS_QUERY)
        coVerify(atMost = 1) { // it was called at most one time
            venuesRepository.searchVenues(EMPTY_RESULTS_QUERY)
        }
    }

    companion object {
        const val FAILED_QUERY = "A failed query"
        const val EMPTY_RESULTS_QUERY = "An empty results query"
        const val ERROR_QUERY = "An error query"
    }
}