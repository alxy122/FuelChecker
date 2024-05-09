package com.fuelchecker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.AbsListView
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.fuelchecker.tkapi.FuelStation
import com.fuelchecker.tkapi.FuelType
import com.fuelchecker.tkapi.SortType
import com.fuelchecker.tkapi.TankerkoenigAPI
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var tankerkoenigAPI: TankerkoenigAPI
    private var fuelStations : MutableLiveData<List<FuelStation>> = MutableLiveData(emptyList())
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var lat=0.0
    private var lng=0.0
    private var fuelType = FuelType.DIESEL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setTitle(R.string.app_name)
        val toolbarColour = (toolbar.background as ColorDrawable).color
        window.statusBarColor = toolbarColour

        requestPermissions()


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        tankerkoenigAPI = TankerkoenigAPI(readApiKey(this))

        swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener {
            Toast.makeText(this, "Refreshing", Toast.LENGTH_SHORT).show()
            callAPI()
        }
        callAPI()

        val listView = findViewById<ListView>(R.id.list_view)
        listView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                // No-op
            }

            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                val topRowVerticalPosition = if (listView.childCount == 0) 0 else listView.getChildAt(0).top
                swipeRefreshLayout.isEnabled = firstVisibleItem == 0 && topRowVerticalPosition >= 0
            }
        })


        fuelStations.observe(this) { stations ->
            val adapter = FuelStationAdapter(this, lat, lng,fuelType, stations)
            listView.adapter = adapter
        }

    }

    private fun readApiKey(context: Context): String {
        return context.assets.open("apikey.txt").bufferedReader().use { it.readText() }
    }

    private fun callAPI() {
        swipeRefreshLayout.isRefreshing = true
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED )
        {

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                lat = location?.latitude ?: 0.0
                lng = location?.longitude ?: 0.0
                if (lat != null && lng != null) {
                    tankerkoenigAPI.getFuelStations(
                        lat,
                        lng,
                        15,
                        SortType.PRICE,
                        fuelType
                    ) { stations ->
                        fuelStations.value = stations
                        Log.d("FuelChecker", "Got ${stations.size} stations")
                    }
                } else {
                    Log.e("FuelChecker", "Failed to get location")
                }
            }
        }
        swipeRefreshLayout.isRefreshing = false
    }

    private fun requestPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                0
            )
        }
    }
}

