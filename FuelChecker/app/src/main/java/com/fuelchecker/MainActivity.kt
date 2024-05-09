package com.fuelchecker

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fuelchecker.tkapi.TankerkoenigAPI

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val api = TankerkoenigAPI(readApiKey(this))
        api.getFuelStations(52.520008, 13.404954, 5, "price", "all") { stations ->
            stations.forEach {
                Log.i("FuelChecker", it.toString())
            }
        }
    }

    private fun readApiKey(context: Context): String {
        return context.assets.open("apikey.txt").bufferedReader().use { it.readText() }
    }
}