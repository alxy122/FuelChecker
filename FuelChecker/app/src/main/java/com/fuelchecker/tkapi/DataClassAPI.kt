package com.fuelchecker.tkapi

data class FuelStationResponse(
    val stations: List<FuelStation>
)

data class FuelStation(
    val id: String,
    val name: String,
    val brand: String,
    val street: String,
    val houseNumber: String,
    val postCode: String,
    val place: String,
    val lat: Double,
    val lng: Double,
    val diesel: Double?,
    val e5: Double?,
    val e10: Double?,
    val price: Double?,
    val isOpen: Boolean,

)