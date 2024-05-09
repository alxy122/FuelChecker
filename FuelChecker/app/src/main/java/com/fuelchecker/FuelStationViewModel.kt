package com.fuelchecker

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fuelchecker.tkapi.FuelStation
import com.fuelchecker.tkapi.TankerkoenigAPI
import kotlinx.coroutines.launch

class FuelStationViewModel(private val tankerkoenigAPI: TankerkoenigAPI) : ViewModel() {
    val fuelStations = MutableLiveData<List<FuelStation>>()

    fun getFuelSpecificStations(locationData: LocationData, fuelSettingsData: FuelSettingsData) {
        viewModelScope.launch {
            val stations = tankerkoenigAPI.getFuelStations(
                locationData.lat, locationData.lng, fuelSettingsData
            )
            Log.i("FuelStationViewModel", "Fetched ${stations.size} fuel stations")
            val filteredStations = stations.filter { it.price != null }
            fuelStations.postValue(filteredStations)
        }
    }
}
