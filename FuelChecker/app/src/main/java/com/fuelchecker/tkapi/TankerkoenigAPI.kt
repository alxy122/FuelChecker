package com.fuelchecker.tkapi

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

enum class FuelType(val value: String) {
    ALL("all"),
    DIESEL("diesel"),
    E5("e5"),
    E10("e10");
}
enum class SortType(val value: String) {
    PRICE("price"),
    DIST("dist")
}
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
    fun getFuelStations(lat:Double, lng: Double, rad:Int, sortType: SortType, fuelType: FuelType, onResult: (List<FuelStation>) -> Unit) {
        val finalSortType = if (fuelType == FuelType.ALL) SortType.DIST else sortType
        val apiService = RetrofitClient.fuelStationService
        apiService.getFuelStations(lat, lng, rad, finalSortType.value, fuelType.value, apikey).enqueue(object :
            Callback<FuelStationResponse> {
            override fun onResponse(call: Call<FuelStationResponse>, response: Response<FuelStationResponse>) {
                if (response.isSuccessful) {
                    val stations = response.body()?.stations ?: emptyList()
                    onResult(stations)
                } else {
                    Log.e("API", "Failed to fetch fuel stations", Throwable("Failed to fetch fuel stations"))
                }
            }
            override fun onFailure(call: Call<FuelStationResponse>, t: Throwable) {
                Log.e("API", "Failed to fetch fuel stations", t)
            }
        })
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