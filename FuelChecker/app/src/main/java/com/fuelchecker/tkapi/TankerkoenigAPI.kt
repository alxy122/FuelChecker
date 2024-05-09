package com.fuelchecker.tkapi

import android.content.Context
import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.File

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


    fun getFuelStations(lat:Double, lng: Double, rad:Int, sort: String, type: String, onResult: (List<FuelStation>) -> Unit) {
        var sortType:String = sort
        if (type=="all"){
            sortType = "dist"
        }
        val apiService = RetrofitClient.fuelStationService
        apiService.getFuelStations(lat, lng, rad, sortType, type, apikey).enqueue(object :
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
        private const val BASE_URL = "https://creativecommons.tankerkoenig.de/"

        val fuelStationService: FuelStationService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(FuelStationService::class.java)
        }

    }
}