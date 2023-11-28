package com.hexterlabs.listdetail.ui.venue

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.test.annotation.UiThreadTest
import androidx.test.core.app.ApplicationProvider
import com.hexterlabs.listdetail.R
import com.hexterlabs.listdetail.databinding.FragmentVenueBinding
import com.hexterlabs.listdetail.domain.Venue
import com.hexterlabs.listdetail.ui.ListDetailViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VenueBindingsTest {

    private val venueA = Venue("1", "abc", "pepe", 5.0, "contact1", "address1", "description1")
    private val venueNoRatting = Venue("2", "bca", "que tal", Double.MAX_VALUE, "contact2", "address2", "description2")
    private val venueNoRattingNoAddress = Venue("3", "aca", "buenos dias", 0.0, "contact3", "", "description3")
    private val venueNoDescription = Venue("4", "aaa", "hola", 1.0, "contact4", "address4", "")
    private val venueNoContact = Venue("4", "aaa", "adios", 1.0, "", "address4", "")

    private lateinit var context: Context
    private lateinit var tested: FragmentVenueBinding

    private val venueLiveData = MutableLiveData<Venue>()

    private val stateLiveData = MutableLiveData(ListDetailViewModel.RefreshDataStatus.LOADING)

    private val viewModel = mockk<VenueViewModel> {
        every { venue } returns venueLiveData
        every { refreshDataState } returns stateLiveData
    }

    @Before
    @UiThreadTest
    fun setUp() {
        context = ApplicationProvider.getApplicationContext() as Context
        context.setTheme(R.style.Theme_ListDetail)
        val layoutInflater: LayoutInflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        tested = FragmentVenueBinding.inflate(layoutInflater)
    }

    @Test
    @UiThreadTest
    fun testSuccess() {
        stateLiveData.value = ListDetailViewModel.RefreshDataStatus.SUCCESS
        tested.viewModel = viewModel
        tested.executePendingBindings()
        assertEquals(View.GONE, tested.venuesProgress.visibility)
        assertEquals(View.GONE, tested.venuesErrorImage.visibility)
    }

    @Test
    @UiThreadTest
    fun testFailure() {
        stateLiveData.value = ListDetailViewModel.RefreshDataStatus.FAILURE
        tested.viewModel = viewModel
        tested.executePendingBindings()
        assertEquals(View.GONE, tested.venuesProgress.visibility)
        assertEquals(View.VISIBLE, tested.venuesErrorImage.visibility)
    }

    @Test
    @UiThreadTest
    fun testVenue() {
        testVenue(venueA)
    }

    @Test
    @UiThreadTest
    fun testVenueB() {
        testVenue(venueNoRatting)
    }

    @Test
    @UiThreadTest
    fun testVenueNoRattingNoAddress() {
        testVenue(venueNoRattingNoAddress)
    }

    @Test
    @UiThreadTest
    fun testVenueNoDescription() {
        testVenue(venueNoDescription)
    }

    @Test
    @UiThreadTest
    fun testVenueNoContact() {
        testVenue(venueNoContact)
    }

    private fun testVisibility(text: String, view: View) {
        assertEquals(if (text.isEmpty()) View.GONE else View.VISIBLE, view.visibility)
    }

    private fun testVenue(venue: Venue) {
        venueLiveData.value = venue
        tested.viewModel = viewModel
        tested.executePendingBindings()
        assertEquals(venue.name, tested.venueName.text)
        assertEquals(
            if (venue.rating == Double.MAX_VALUE || venue.rating == 0.0) "" else context.getString(R.string.venue_rating, venue.rating),
            tested.venueRating.text
        )
        assertEquals(venue.address, tested.venueAddress.text)
        testVisibility(venue.address, tested.venueAddress)
        assertEquals(venue.contact, tested.venueContact.text)
        testVisibility(venue.contact, tested.venueContact)
        assertEquals(venue.description, tested.venueDescription.text)
        testVisibility(venue.description, tested.venueDescription)
    }
}