package com.fuelchecker.tkapi

data class FuelStationResponse(
    val stations: List<FuelStation>
)

data class FuelStation(
    val id: String,
    val name: String,
    val brand: String,
    val diesel: Double?,
    val e5: Double?,
    val e10: Double?,
    val price: Double?,
    val lat: Double,
    val lng: Double,
)