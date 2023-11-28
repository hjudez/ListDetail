package com.hexterlabs.listdetail.di

import android.content.Context
import androidx.room.Room
import com.hexterlabs.listdetail.database.AppDatabase
import com.hexterlabs.listdetail.database.SearchVenuesResultDao
import com.hexterlabs.listdetail.database.VenueDao
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object FakeDatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @Provides
    fun provideSearchVenuesResultDao(appDatabase: AppDatabase): SearchVenuesResultDao {
        return appDatabase.searchVenuesResultDao()
    }

    @Provides
    fun provideVenueDao(appDatabase: AppDatabase): VenueDao {
        return appDatabase.venueDao()
    }
}