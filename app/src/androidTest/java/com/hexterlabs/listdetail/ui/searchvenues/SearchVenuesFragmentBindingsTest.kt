package com.hexterlabs.listdetail.ui.searchvenues

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.view.LayoutInflater
import android.view.View
import androidx.test.annotation.UiThreadTest
import androidx.test.core.app.ApplicationProvider
import com.hexterlabs.listdetail.databinding.FragmentSearchVenuesBinding
import com.hexterlabs.listdetail.ui.ListDetailViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SearchVenuesFragmentBindingsTest {

    private lateinit var tested: FragmentSearchVenuesBinding

    private val refreshDataStateLiveData = MutableStateFlow(ListDetailViewModel.RefreshDataStatus.LOADING)

    private val searchVenuesViewModel = mockk<SearchVenuesViewModel> {
        every { refreshDataState } returns refreshDataStateLiveData
    }

    @Before
    @UiThreadTest
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext() as Context
        val layoutInflater: LayoutInflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        tested = FragmentSearchVenuesBinding.inflate(layoutInflater)
        tested.viewModel = searchVenuesViewModel
    }

    @Test
    @UiThreadTest
    fun testSearching() {
        tested.executePendingBindings()
        assertEquals(View.VISIBLE, tested.venuesProgress.visibility)
        assertEquals(View.GONE, tested.venuesErrorImage.visibility)
        assertEquals(View.GONE, tested.venuesNoResult.visibility)
    }

    @Test
    @UiThreadTest
    fun testSuccess() {
        refreshDataStateLiveData.value = ListDetailViewModel.RefreshDataStatus.SUCCESS
        tested.executePendingBindings()
        assertEquals(View.GONE, tested.venuesProgress.visibility)
        assertEquals(View.GONE, tested.venuesErrorImage.visibility)
        assertEquals(View.GONE, tested.venuesNoResult.visibility)
    }

    @Test
    @UiThreadTest
    fun testFailure() {
        refreshDataStateLiveData.value = ListDetailViewModel.RefreshDataStatus.FAILURE
        tested.executePendingBindings()
        assertEquals(View.GONE, tested.venuesProgress.visibility)
        assertEquals(View.VISIBLE, tested.venuesErrorImage.visibility)
        assertEquals(View.GONE, tested.venuesNoResult.visibility)
    }

    @Test
    @UiThreadTest
    fun testNoResults() {
        refreshDataStateLiveData.value = ListDetailViewModel.RefreshDataStatus.NOT_FOUND
        tested.executePendingBindings()
        assertEquals(View.GONE, tested.venuesProgress.visibility)
        assertEquals(View.GONE, tested.venuesErrorImage.visibility)
        assertEquals(View.VISIBLE, tested.venuesNoResult.visibility)
    }
}