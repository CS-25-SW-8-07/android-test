package com.example.rust3

import android.location.Address
import android.location.Geocoder
import android.location.LocationListener
import android.os.Bundle
import android.os.Looper // <-- added
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.IOException
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class MainActivity : AppCompatActivity() {

    //<RustJNI>
    // auto-generated code

    external fun startProgram(artifact_path: String): String

    init { System.loadLibrary("my_rust_lib") }

    //</RustJNI>




    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationText: TextView
    private val locationList = ArrayList<String>() // store location strings
    private var locationUpdatesRunning = false // prevent duplicate starts
    private lateinit var locationCallback: LocationCallback

    private val LOCATION_PERMISSION_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val artifactDir = File(filesDir, "training-logs")
        if (!artifactDir.exists()) {
            artifactDir.mkdirs()
        }

        val myBtn: Button = findViewById(R.id.somebtn)
        val artifact_path = artifactDir.absolutePath

        myBtn.setOnClickListener {
            val text = findViewById<TextView>(R.id.kagemand)
            text.text = startProgram(artifact_path)
        }

        locationText = findViewById(R.id.loc)
        val getLocationBtn = findViewById<Button>(R.id.getLoc)

        locationClient = LocationServices.getFusedLocationProviderClient(this)

        getLocationBtn.setOnClickListener {
            startLocationUpdates() // now triggers continuous updates
        }
    }

    private fun startLocationUpdates() {
        if (locationUpdatesRunning) return // avoid restarting if already running

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000)
            .setMinUpdateIntervalMillis(5_000) // fastest updates every 5 seconds
            .setMaxUpdates(10) // Optional: can limit max updates if needed
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null && locationList.size < 10) {
                    val lat = location.latitude
                    val lon = location.longitude
                    val entry = "Lat: $lat, Lon: $lon"
                    locationList.add(entry)

                    // Update TextView with all entries
                    locationText.text = locationList.joinToString("\n")

                    // Stop if we've collected 10 entries
                    if (locationList.size >= 10) {
                        stopLocationUpdates()
                    }
                }
            }
        }

        locationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        locationUpdatesRunning = true
    }

    private fun stopLocationUpdates() {
        if (::locationCallback.isInitialized) {
            locationClient.removeLocationUpdates(locationCallback)
            locationUpdatesRunning = false
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates()
        } else {
            locationText.text = "Location permission denied"
        }
    }
}
