package com.hexterlabs.listdetail.ui.searchvenues

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.test.annotation.UiThreadTest
import androidx.test.core.app.ApplicationProvider
import com.hexterlabs.listdetail.domain.SearchVenuesResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchVenuesAdapterTest {

    private val venueA = SearchVenuesResult("1", "abc", "address1", 10)
    private val venueB = SearchVenuesResult("2", "bca", "address2", 15)
    private val onClickListener: (String) -> Unit = { venueId -> println(venueId) }

    private lateinit var diff: VenueDiffCallback
    private lateinit var adapter: SearchVenuesAdapter

    @Before
    fun before() {
        adapter = SearchVenuesAdapter(onClickListener)
        diff = VenueDiffCallback()
    }

    @Test
    @UiThreadTest
    fun testBinding() {
        val viewHolder = adapter.onCreateViewHolder(ConstraintLayout(ApplicationProvider.getApplicationContext()), 0)
        adapter.submitList(listOf(venueA, venueB))
        adapter.onBindViewHolder(viewHolder, 0)
        assertEquals(venueA, viewHolder.binding.searchVenuesResult)
        adapter.onBindViewHolder(viewHolder, 1)
        assertEquals(venueB, viewHolder.binding.searchVenuesResult)
        assertEquals(onClickListener, viewHolder.binding.onClickListener)
    }

    @Test
    fun testAreItemsTheSame() {
        assertTrue(diff.areItemsTheSame(venueA, venueA))
        assertTrue(diff.areItemsTheSame(venueB, venueB))
        assertFalse(diff.areItemsTheSame(venueA, venueB))
        assertFalse(diff.areItemsTheSame(venueB, venueA))
    }

    @Test
    fun testAreContentsTheSame() {
        assertTrue(diff.areContentsTheSame(venueA, venueA))
        assertTrue(diff.areContentsTheSame(venueB, venueB))
        assertFalse(diff.areContentsTheSame(venueA, venueB))
        assertFalse(diff.areContentsTheSame(venueB, venueA))
    }
}