package com.hexterlabs.listdetail.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchVenuesResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(searchVenues: List<DatabaseSearchVenuesResult>)

    @Query("SELECT * FROM DatabaseSearchVenuesResult ORDER BY distance")
    fun getSearchVenues(): Flow<List<DatabaseSearchVenuesResult>>

    @Query("DELETE FROM DatabaseSearchVenuesResult")
    suspend fun clear()
}

@Dao
interface VenueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(venue: DatabaseVenue)

    @Query("SELECT * from DatabaseVenue WHERE id = :id")
    fun getVenue(id: String): Flow<DatabaseVenue?>

    @Query("SELECT COUNT(id) from DatabaseVenue WHERE id = :id")
    suspend fun countVenue(id: String): Int

    @Query("DELETE FROM DatabaseVenue")
    suspend fun clear()
}

@Database(
    entities = [DatabaseSearchVenuesResult::class, DatabaseVenue::class],
    version = 1,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun searchVenuesResultDao(): SearchVenuesResultDao
    abstract fun venueDao(): VenueDao

    companion object {
        private const val DATABASE_NAME = "listDetail-db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME).build()
        }
    }
}
