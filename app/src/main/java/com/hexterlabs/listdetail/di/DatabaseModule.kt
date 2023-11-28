package com.hexterlabs.listdetail.di

import android.content.Context
import com.hexterlabs.listdetail.database.AppDatabase
import com.hexterlabs.listdetail.database.SearchVenuesResultDao
import com.hexterlabs.listdetail.database.VenueDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
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