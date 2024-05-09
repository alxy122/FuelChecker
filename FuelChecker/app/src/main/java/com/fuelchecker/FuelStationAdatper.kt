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


class FuelStationAdapter(
    private val activity: MainActivity,
    private val lat: Double,
    private val lng: Double,
    private val fuelType: FuelType,
    private val fuelStations: List<FuelStation>
) :
    ArrayAdapter<FuelStation>(activity, R.layout.list_fuel_stations, fuelStations) {

    init {
        // Pre-calculate distances when setting the data
        fuelStations.forEach { station ->
            val results = FloatArray(1)
            Location.distanceBetween(lat, lng, station.lat, station.lng, results)
            station.dist = results[0] / 1000 // Store this value in your data model
        }
    }

    private class ViewHolder(view: View) {
        val priceTextView: TextView = view.findViewById(R.id.price)
        val stationNameTextView: TextView = view.findViewById(R.id.station_name)
        val stationPlace: TextView = view.findViewById(R.id.station_place)
        val distanceTextView: TextView = view.findViewById(R.id.distance)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val viewHolder: ViewHolder
        val view: View

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_fuel_stations, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val station = getItem(position) ?: return view
        val stationPlace= station.place.lowercase().replaceFirstChar { it.uppercase() }
        viewHolder.priceTextView.text = station.price.toString()
        viewHolder.stationNameTextView.text = station.name
        viewHolder.stationPlace.text = stationPlace
        viewHolder.distanceTextView.text = String.format("%.1f km", station.dist)

        view.setOnClickListener { showMapDialog(station) }

        return view
    }

    private fun showMapDialog(station: FuelStation) {
        val gmmIntentUri = Uri.parse("geo:${station.lat},${station.lng}?q=${station.lat},${station.lng}(${station.name})")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(activity.packageManager) != null) {
            AlertDialog.Builder(activity)
                .setTitle("Open in Google Maps")
                .setMessage("Do you want to open ${station.name} in Google Maps?")
                .setPositiveButton("Yes") { _, _ -> activity.startActivity(mapIntent) }
                .setNegativeButton("No", null)
                .show()
        }
    }
}