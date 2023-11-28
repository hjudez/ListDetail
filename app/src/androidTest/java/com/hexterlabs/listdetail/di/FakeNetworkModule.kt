package com.hexterlabs.listdetail.di

import com.hexterlabs.listdetail.network.ConnectivityManager
import com.hexterlabs.listdetail.network.FakeConnectivityManager
import com.hexterlabs.listdetail.network.FakeFoursquareService
import com.hexterlabs.listdetail.network.FoursquareService
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NetworkModule::class]
)
object FakeNetworkModule {
    @Singleton
    @Provides
    fun provideFoursquareService(): FoursquareService {
        return FakeFoursquareService()
    }

    @Singleton
    @Provides
    fun provideFakeConnectivityManager(): ConnectivityManager {
        return FakeConnectivityManager()
    }
}