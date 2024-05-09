package com.fuelchecker.tkapi

import android.util.Log
import com.fuelchecker.FuelSettingsData
import com.fuelchecker.FuelType
import com.fuelchecker.SortType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


private const val BASE_URL = "https://creativecommons.tankerkoenig.de/"

class TankerkoenigAPI (private val apikey:String) {
    interface FuelStationService {
        @GET("json/list.php")
        fun getFuelStations(
            @Query("lat") lat: Double,
            @Query("lng") lng: Double,
            @Query("rad") rad: Int,
            @Query("sort") sort: String,
            @Query("type") type: String,
            @Query("apikey") apikey: String
        ): Call<FuelStationResponse>
    }

    suspend fun getFuelStations(lat:Double, lng: Double, fuelSettingsData: FuelSettingsData): List<FuelStation> {
        //val finalSortType = if (fuelSettingsData.fuelType == FuelType.ALL) SortType.DIST else fuelSettingsData.sortType
        val apiService = RetrofitClient.fuelStationService
        val call = apiService.getFuelStations(lat, lng, fuelSettingsData.rad, fuelSettingsData.sortType.value, fuelSettingsData.fuelType.value, apikey)
        return withContext(Dispatchers.IO) {
            val response = call.execute()
            if (response.isSuccessful) {
                response.body()?.stations ?: emptyList()
            } else {
                Log.e("API", "Failed to fetch fuel stations", Throwable("Failed to fetch fuel stations"))
                emptyList()
            }
        }
    }
    object RetrofitClient {
        val fuelStationService: FuelStationService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(FuelStationService::class.java)
        }
    }
}