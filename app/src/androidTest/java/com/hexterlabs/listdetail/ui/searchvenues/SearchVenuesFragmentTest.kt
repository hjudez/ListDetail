package com.hexterlabs.listdetail.ui.searchvenues

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.hexterlabs.listdetail.R
import com.hexterlabs.listdetail.network.ConnectivityManager
import com.hexterlabs.listdetail.network.FakeConnectivityManager
import com.hexterlabs.listdetail.network.FakeFoursquareService
import com.hexterlabs.listdetail.network.FoursquareSearchVenuesResponse
import com.hexterlabs.listdetail.network.FoursquareSearchVenuesResponseBody
import com.hexterlabs.listdetail.network.FoursquareService
import com.hexterlabs.listdetail.ui.MainActivity
import com.hexterlabs.listdetail.ui.venue.VenueFragmentTest
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
class SearchVenuesFragmentTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    @Inject
    lateinit var foursquareService: FoursquareService

    @Before
    fun init() {
        hiltRule.inject()
        /**
         * Using Thread.sleep is definitely a bad practice. But for the purpose of this small task it's ok.
         * Tests could fail when testing on a slower device.. or if the test becomes more complex.
         * Migrate to https://developer.android.com/training/testing/espresso/idling-resource when possible.
         */
        Thread.sleep(VenueFragmentTest.DELAY_IN_MILLIS)
    }

    @Test
    fun searchAndShowsItems() {
        onView(withText(FakeFoursquareService.VENUE_A_SEARCH_NAME)).check(matches(isDisplayed()))
        onView(withText(FakeFoursquareService.VENUE_B_SEARCH_NAME)).check(matches(isDisplayed()))
        onView(withText(FakeFoursquareService.VENUE_A_SEARCH_LOCATION)).check(matches(isDisplayed()))
        onView(withText(FakeFoursquareService.VENUE_B_SEARCH_LOCATION)).check(matches(isDisplayed()))
    }

    @Test
    fun searchWithNoConnectionShowsErrorAndWhenConnectionBackSearches(): Unit = runBlocking {
        (connectivityManager as FakeConnectivityManager).sendDisconnected()
        (foursquareService as FakeFoursquareService).setSearchVenuesResponse { throw IllegalStateException() }
        onView(withId(R.id.search_venues_search_view)).perform(typeText("Hello"))
        Thread.sleep(DELAY_IN_MILLIS)
        onView(withId(R.id.venues_error_image)).check(matches(isDisplayed()))
        (foursquareService as FakeFoursquareService).resetSearchVenuesResponse()
        (connectivityManager as FakeConnectivityManager).sendConnected()
        Thread.sleep(DELAY_IN_MILLIS)
        searchAndShowsItems()
    }

    @Test
    fun noResultsFound(): Unit = runBlocking {
        val foursquareSearchVenuesResponseBody = FoursquareSearchVenuesResponseBody(emptyList())
        val foursquareSearchVenuesResponse = FoursquareSearchVenuesResponse(foursquareSearchVenuesResponseBody)
        (foursquareService as FakeFoursquareService).setSearchVenuesResponse { foursquareSearchVenuesResponse }
        onView(withId(R.id.search_venues_search_view)).perform(typeText("Hello"))
        Thread.sleep(DELAY_IN_MILLIS)
        onView(withId(R.id.venues_no_result)).check(matches(isDisplayed()))
    }

    companion object {
        const val DELAY_IN_MILLIS = 2000L
    }
}