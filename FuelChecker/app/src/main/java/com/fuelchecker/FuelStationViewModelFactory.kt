package com.fuelchecker

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.fuelchecker.tkapi.TankerkoenigAPI

class FuelStationViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private var api: TankerkoenigAPI

    init {
        val apiKey = readApiKey(context)
        api = TankerkoenigAPI(apiKey)
    }
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FuelStationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FuelStationViewModel(api) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    private fun readApiKey(context: Context): String {
        return context.assets.open("apikey.txt").bufferedReader().use { it.readText() }
    }
}

