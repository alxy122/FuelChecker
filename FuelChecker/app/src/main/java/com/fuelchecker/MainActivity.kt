package com.fuelchecker

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.os.Bundle
import android.widget.AbsListView
import android.widget.Button
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.fuelchecker.tkapi.FuelStation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

private const val PERMISSION_REQUEST_CODE = 11111
class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var listView: ListView
    private lateinit var viewModel: FuelStationViewModel
    private var locationData = LocationData(0.0, 0.0)
//    private var fuelSettingsData = FuelSettingsData(FuelType.E10, SortType.PRICE, 15)
    private var fuelSettingsData = MutableLiveData(FuelSettingsData(FuelType.DIESEL, SortType.PRICE, 15))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupToolbar()
        setupSwipeRefreshLayout()
        setupListView()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val factory = FuelStationViewModelFactory(this)
        viewModel = ViewModelProvider(this, factory)[FuelStationViewModel::class.java]
        viewModel.fuelStations.observe(this) { stations ->
            updateListView(stations)
        }

        fuelSettingsData.observe(this) {
            getLocationAndCallAPI()
        }
        assignButtonListeners()

        if (!PermissionUtils.hasLocationPermission(this)) {

            PermissionUtils.requestLocationPermissions(this, PERMISSION_REQUEST_CODE)
        } else {
            getLocationAndCallAPI()
        }
    }

    private fun setupToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.app_name)
        toolbar.background?.let {
            window.statusBarColor = (it as ColorDrawable).color
        }
    }

    private fun setupSwipeRefreshLayout() {
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout)
        swipeRefreshLayout.setOnRefreshListener {
            getLocationAndCallAPI()
        }
    }

    private fun setupListView() {
        listView = findViewById(R.id.list_view)
        listView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) { }
            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                swipeRefreshLayout.isEnabled =
                    firstVisibleItem == 0 && (listView.getChildAt(0)?.top ?: 0) >= 0
            }
        })
    }

    private fun updateListView(stations: List<FuelStation>) {
        listView.adapter = FuelStationAdapter(this, locationData.lat, locationData.lng, fuelSettingsData.value?.fuelType ?:FuelType.DIESEL, stations)
    }

    @SuppressLint("MissingPermission")
    private fun getLocationAndCallAPI() {
        if (PermissionUtils.hasLocationPermission(this)) {
            swipeRefreshLayout.isRefreshing = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    locationData = LocationData(location.latitude, location.longitude)
                    viewModel.getFuelSpecificStations(locationData, fuelSettingsData.value ?:FuelSettingsData(FuelType.DIESEL, SortType.DIST, 15))
                } else {
                    Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show()
                }
                swipeRefreshLayout.isRefreshing = false
            }.addOnFailureListener {
                Toast.makeText(this, "Error obtaining location", Toast.LENGTH_SHORT).show()
                swipeRefreshLayout.isRefreshing = false
            }
        } else {
            PermissionUtils.requestLocationPermissions(this, PERMISSION_REQUEST_CODE)
        }
    }

    private fun assignButtonListeners(){
        val button1: Button = findViewById(R.id.button1)
        button1.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            popupMenu.menuInflater.inflate(R.menu.fuel_type_dropdown_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.E5 -> {
                        fuelSettingsData.value = FuelSettingsData(FuelType.E5, fuelSettingsData.value?.sortType ?:SortType.DIST, fuelSettingsData.value?.rad ?:15)
                        button1.text = "E5"
                        getLocationAndCallAPI()
                        true
                    }
                    R.id.E10 -> {
                        fuelSettingsData.value = FuelSettingsData(FuelType.E10, fuelSettingsData.value?.sortType ?:SortType.DIST, fuelSettingsData.value?.rad ?:15)
                        getLocationAndCallAPI()
                        button1.text = "E10"
                        true
                    }
                    R.id.Diesel -> {
                        fuelSettingsData.value = FuelSettingsData(FuelType.DIESEL, fuelSettingsData.value?.sortType ?:SortType.DIST, fuelSettingsData.value?.rad ?:15)
                        getLocationAndCallAPI()
                        button1.text = "Diesel"
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        val button2: Button = findViewById(R.id.button2)
        button2.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            popupMenu.menuInflater.inflate(R.menu.sort_dropdown_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.distance -> {
                        fuelSettingsData.value = FuelSettingsData(fuelSettingsData.value?.fuelType ?:FuelType.DIESEL, SortType.DIST, fuelSettingsData.value?.rad ?:15)
                        getLocationAndCallAPI()
                        button2.text = "Distance"
                        true
                    }
                    R.id.price -> {
                        fuelSettingsData.value = FuelSettingsData(fuelSettingsData.value?.fuelType ?:FuelType.DIESEL, SortType.PRICE, fuelSettingsData.value?.rad ?:15)
                        getLocationAndCallAPI()
                        button2.text = "Price"
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }
    }
}

