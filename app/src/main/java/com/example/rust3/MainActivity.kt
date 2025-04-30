package com.example.rust3

import android.os.Bundle
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.collection.ArraySet
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class MainActivity : AppCompatActivity() {

    //<RustJNI>
    // auto-generated code

    external fun startTraining(artifactPath: String, locationString: String): String

    init { System.loadLibrary("my_rust_lib") }

    //</RustJNI>




    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationText: TextView
    private val locationList = ArrayList<String>()
    private var locationUpdatesRunning = false
    private lateinit var locationCallback: LocationCallback
    private lateinit var artifactPath: String
    private lateinit var locationString: String

    private val entryAmount = 3
    private val LOCATION_PERMISSION_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val artifactDir = File(filesDir, "training-logs")
        if (!artifactDir.exists()) {
            artifactDir.mkdirs()
        }

        artifactPath = artifactDir.absolutePath

        locationString = ""

        locationText = findViewById(R.id.loc)
        val startBtn = findViewById<Button>(R.id.startProgram)

        locationClient = LocationServices.getFusedLocationProviderClient(this)

        startBtn.setOnClickListener {
            startLocationUpdates() // now triggers continuous updates
        }
        val uploadButton = findViewById<Button>(R.id.uploadButton)
        val uploader = DatabaseUploadManager(filesDir.absolutePath)

        uploadButton.setOnClickListener {
            uploader.uploadModelFile(uploader.serverUrl)
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

                if (locationList.size == 0){
                    val timestamp = System.currentTimeMillis()

                    locationString += "$timestamp,"

                }
                if (location != null && locationList.size < entryAmount) {
                    val lat = location.latitude
                    val lon = location.longitude
                    val entry = "($lat, $lon),"
                    locationList.add(entry)
                    locationString += entry

                    locationText.text = locationString

                    if (locationList.size >= entryAmount) {
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

        val text = findViewById<TextView>(R.id.testView)
        text.text = startTraining(artifactPath, locationString)
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    @SuppressLint("SetTextI18n")
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