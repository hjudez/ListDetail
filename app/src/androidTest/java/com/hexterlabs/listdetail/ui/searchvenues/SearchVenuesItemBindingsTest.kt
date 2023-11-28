package com.hexterlabs.listdetail.ui.searchvenues

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.view.LayoutInflater
import androidx.test.annotation.UiThreadTest
import androidx.test.core.app.ApplicationProvider
import com.hexterlabs.listdetail.R
import com.hexterlabs.listdetail.databinding.ListItemVenueBinding
import com.hexterlabs.listdetail.domain.SearchVenuesResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchVenuesItemBindingsTest {

    private val venueA = SearchVenuesResult("1", "abc", "address1", 10)
    private val venueB = SearchVenuesResult("2", "bca", "address2", 15)
    private val venueNoDistance = SearchVenuesResult("3", "cba", "address3", Int.MAX_VALUE)

    private lateinit var context: Context
    private lateinit var tested: ListItemVenueBinding

    @Before
    @UiThreadTest
    fun setUp() {
        context = ApplicationProvider.getApplicationContext() as Context
        val layoutInflater: LayoutInflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        tested = ListItemVenueBinding.inflate(layoutInflater)
    }

    @Test
    @UiThreadTest
    fun testVenueA() {
        testVenue(venueA)
    }

    @Test
    @UiThreadTest
    fun testVenueB() {
        testVenue(venueB)
    }

    @Test
    @UiThreadTest
    fun testVenueNoDistance() {
        testVenue(venueNoDistance)
    }

    private fun testVenue(venue: SearchVenuesResult) {
        val function: (String) -> Unit = { venueId -> assertEquals(venue.id, venueId) }
        tested.searchVenuesResult = venue
        tested.onClickListener = function
        tested.executePendingBindings()
        tested.venueContainer.performClick()
        assertEquals(venue.name, tested.venueName.text)
        assertEquals(venue.location, tested.venueLocation.text)
        assertEquals(
            if (venue.distance == Int.MAX_VALUE) "" else context.getString(R.string.distance_away, venue.distance),
            tested.venueDistance.text
        )
    }
}