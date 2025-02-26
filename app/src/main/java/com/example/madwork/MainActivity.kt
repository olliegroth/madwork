package com.example.madwork

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.ramani.compose.CameraPosition
import org.ramani.compose.MapLibre

class MainActivity : ComponentActivity(), LocationListener {
    private val locationViewModel: LocationViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()

        setContent {
            val location by locationViewModel.location.observeAsState()
            val coordinates = location?.let { org.maplibre.android.geometry.LatLng(it.latitude, it.longitude) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Latitude: ${location?.latitude ?: "Loading..."}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Longitude: ${location?.longitude ?: "Loading..."}")
                Spacer(modifier = Modifier.height(16.dp))
                coordinates?.let {
                    MapLibre(
                        modifier = Modifier.fillMaxSize(),
                        styleBuilder = org.maplibre.android.maps.Style.Builder().fromUri("https://tiles.openfreemap.org/styles/bright"),
                        cameraPosition = CameraPosition(it, 14.0)
                    )
                }
            }
        }
    }

    private fun checkPermissions() {
        val requiredPermission = Manifest.permission.ACCESS_FINE_LOCATION

        if (checkSelfPermission(requiredPermission) == PackageManager.PERMISSION_GRANTED) {
            locationViewModel.startGPS()
        } else {
            val permissionLauncher =
                this.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                    if (isGranted) {
                        locationViewModel.startGPS()
                    } else {
                        Toast.makeText(this, "GPS permission not granted", Toast.LENGTH_LONG).show()
                    }
                }
            permissionLauncher.launch(requiredPermission)
        }
    }

    override fun onLocationChanged(location: Location) {
        locationViewModel.updateLocation(location)
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}

class LocationViewModel(application: android.app.Application) : AndroidViewModel(application), LocationListener {
    private val _location = MutableLiveData<Location>()
    val location: LiveData<Location> = _location

    private val locationManager = application.getSystemService(android.app.Application.LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission")
    fun startGPS() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
    }

    fun updateLocation(location: Location) {
        _location.value = location
    }

    override fun onLocationChanged(location: Location) {
        _location.value = location
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}