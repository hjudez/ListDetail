package com.hexterlabs.listdetail.network

import com.hexterlabs.listdetail.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Used to connect to Foursquare API to search for venues and see their details.
 */
interface FoursquareService {

    @GET("venues/search")
    suspend fun searchVenues(
        @Query("query") query: String = "",
        @Query("ll") ll: String = LAT_LON_ROTTERDAM,
        @Query("radius") radius: Int = RADIUS,
        @Query("limit") limit: Int = LIMIT,
        @Query("v") v: String = VERSION_UP_TO,
        @Query("client_id") client_id: String = BuildConfig.FOURSQUARE_CLIENT_ID,
        @Query("client_secret") client_secret: String = BuildConfig.FOURSQUARE_CLIENT_SECRET
    ): FoursquareSearchVenuesResponse

    @GET("venues/{id}/")
    suspend fun venue(
        @Path("id") id: String = "",
        @Query("v") v: String = VERSION_UP_TO,
        @Query("client_id") client_id: String = BuildConfig.FOURSQUARE_CLIENT_ID,
        @Query("client_secret") client_secret: String = BuildConfig.FOURSQUARE_CLIENT_SECRET
    ): FoursquareVenueResponse

    companion object {
        private const val BASE_URL = "https://api.foursquare.com/v2/"
        private const val LAT_LON_ROTTERDAM = "51.922527,4.485559" // hardcoded location near Centrum Rotterdam.
        private const val RADIUS = 1000
        private const val LIMIT = 10
        private const val VERSION_UP_TO = "20210410"

        fun create(): FoursquareService {
            val logger = HttpLoggingInterceptor().apply { level = Level.BASIC }

            val client = OkHttpClient.Builder()
                .addInterceptor(logger)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(FoursquareService::class.java)
        }
    }
}