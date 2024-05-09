package com.fuelchecker

import android.app.AlertDialog
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.fuelchecker.tkapi.FuelStation
import com.fuelchecker.tkapi.FuelType

class FuelStationAdapter(private val activity: MainActivity,private val lat:Double, private val lng:Double, private val fuelType:FuelType, private val fuelStations: List<FuelStation>) :
    ArrayAdapter<FuelStation>(activity, R.layout.list_fuel_stations, fuelStations) {


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.list_fuel_stations, parent, false)

        val station = fuelStations[position]

        val priceTextView = view.findViewById<TextView>(R.id.price)
        val stationNameTextView = view.findViewById<TextView>(R.id.station_name)
        val fuelTypeTextView = view.findViewById<TextView>(R.id.fuel_type)
        val distanceTextView = view.findViewById<TextView>(R.id.distance)

        priceTextView.text = station.price.toString()
        stationNameTextView.text = station.name
        fuelTypeTextView.text = fuelType.value
        val results = FloatArray(1)
        Location.distanceBetween(lat, lng, station.lat, station.lng, results)
        val distanceInKm = results[0] / 1000
        val roundedDistance = String.format("%.1f", distanceInKm)
        distanceTextView.text = "$roundedDistance km"

        view.setOnClickListener {
            val gmmIntentUri =
                Uri.parse("geo:${station.lat},${station.lng}?q=${station.lat},${station.lng}(${station.name})")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(activity.packageManager) != null) {
                AlertDialog.Builder(activity)
                    .setTitle("Open in Google Maps")
                    .setMessage("Do you want to open ${station.name} in Google Maps?")
                    .setPositiveButton("Yes") { _, _ ->
                        // Start the map intent activity when the "Yes" button is clicked
                        activity.startActivity(mapIntent)
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }

        return view
    }
}