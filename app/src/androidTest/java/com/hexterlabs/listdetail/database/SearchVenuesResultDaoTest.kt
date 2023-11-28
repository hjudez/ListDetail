package com.hexterlabs.listdetail.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SearchVenuesResultDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var searchVenuesResultDao: SearchVenuesResultDao

    private val venueA = DatabaseSearchVenuesResult("1", "abc", "address1")
    private val venueB = DatabaseSearchVenuesResult("2", "bca", "address2")
    private val venueC = DatabaseSearchVenuesResult("3", "cdc", "address3")

    @Before
    fun createDb() = runBlocking {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java).build()
        searchVenuesResultDao = database.searchVenuesResultDao()
        searchVenuesResultDao.insertAll(listOf(venueA, venueB, venueC))
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testGetVenues() = runBlocking {
        val venuesList = searchVenuesResultDao.getSearchVenues().first()
        assertEquals(3, venuesList.size)
        assertEquals(venuesList[0], venueA)
        assertEquals(venuesList[1], venueB)
        assertEquals(venuesList[2], venueC)
    }


    @Test
    fun testClear() = runBlocking {
        searchVenuesResultDao.clear()
        assertEquals(0, searchVenuesResultDao.getSearchVenues().first().size)
    }
}
