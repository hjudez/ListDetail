package com.hexterlabs.listdetail.di

import android.app.Application
import android.content.Context
import android.net.NetworkRequest
import com.hexterlabs.listdetail.network.ConnectivityManager
import com.hexterlabs.listdetail.network.ConnectivityManagerImpl
import com.hexterlabs.listdetail.network.FoursquareService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {

    @Singleton
    @Provides
    fun provideFoursquareService(): FoursquareService {
        return FoursquareService.create()
    }

    @Singleton
    @Provides
    fun provideAndroidConnectivityManager(application: Application): android.net.ConnectivityManager {
        return application.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
    }

    @Provides
    fun provideNetworkRequestBuilder(): NetworkRequest.Builder {
        return NetworkRequest.Builder()
    }

    @Singleton
    @Provides
    fun provideConnectivityManager(
        androidConnectivityManager: android.net.ConnectivityManager,
        networkRequestBuilder: NetworkRequest.Builder,
        @IoDispatcher coroutineDispatcher: CoroutineDispatcher
    ): ConnectivityManager {
        return ConnectivityManagerImpl(
            androidConnectivityManager,
            networkRequestBuilder,
            coroutineDispatcher
        )
    }
}