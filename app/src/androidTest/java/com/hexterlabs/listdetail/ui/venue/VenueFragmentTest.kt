package com.hexterlabs.listdetail.ui.venue

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.hexterlabs.listdetail.R
import com.hexterlabs.listdetail.network.ConnectivityManager
import com.hexterlabs.listdetail.network.FakeConnectivityManager
import com.hexterlabs.listdetail.network.FakeFoursquareService
import com.hexterlabs.listdetail.network.FoursquareService
import com.hexterlabs.listdetail.ui.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
class VenueFragmentTest {

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
        Thread.sleep(DELAY_IN_MILLIS)
        closeSoftKeyboard()
    }

    @Test
    fun loadAndShowsItemBackCloseConnectionAndLoadFromCache() = runBlocking {
        loadAndShowsItem()
        pressBack()
        (connectivityManager as FakeConnectivityManager).sendDisconnected()
        (foursquareService as FakeFoursquareService).setVenueResponse { throw IllegalStateException() }
        loadAndShowsItem()
    }

    @Test
    fun firstLoadWithNoConnectionShowsError() = runBlocking {
        (connectivityManager as FakeConnectivityManager).sendDisconnected()
        (foursquareService as FakeFoursquareService).setVenueResponse { throw IllegalStateException() }
        loadAndShowError()
    }

    private fun loadAndShowsItem() {
        onView(withText(FakeFoursquareService.VENUE_A_SEARCH_NAME)).perform(click())
        Thread.sleep(DELAY_IN_MILLIS)
        checkVenueIsDisplayed()
    }

    private fun loadAndShowError() {
        onView(withText(FakeFoursquareService.VENUE_A_SEARCH_NAME)).perform(click())
        Thread.sleep(DELAY_IN_MILLIS)
        onView(ViewMatchers.withId(R.id.venues_error_image)).check(matches(isDisplayed()))
    }

    private fun checkVenueIsDisplayed() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val ratingText = context.resources.getString(R.string.venue_rating, FakeFoursquareService.VENUE_RATING)
        onView(withText(FakeFoursquareService.VENUE_NAME)).check(matches(isDisplayed()))
        onView(withText(ratingText)).check(matches(isDisplayed()))
        onView(withText(FakeFoursquareService.VENUE_CONTACT)).check(matches(isDisplayed()))
        onView(withText(FakeFoursquareService.VENUE_LOCATION)).check(matches(isDisplayed()))
        onView(withText(FakeFoursquareService.VENUE_DESCRIPTION)).check(matches(isDisplayed()))
    }

    companion object {
        const val DELAY_IN_MILLIS = 2000L
    }
}