package com.example.madwork

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.maplibre.android.geometry.LatLng
import org.ramani.compose.CameraPosition
import org.ramani.compose.MapLibre

class MainActivity : ComponentActivity(), LocationListener {
    private val latLngViewModel: LatLngViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissions()

        setContent {
            var latitudeInput by remember { mutableStateOf("") }
            var longitudeInput by remember { mutableStateOf("") }
            var inputCoordinates by remember { mutableStateOf<LatLng?>(null) }
            val coordinates = latLngViewModel.latLng.let { LatLng(it.latitude, it.longitude) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextField(
                        value = latitudeInput,
                        onValueChange = { latitudeInput = it },
                        label = { Text("Latitude") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = longitudeInput,
                        onValueChange = { longitudeInput = it },
                        label = { Text("Longitude") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (latitudeInput.isNotEmpty() && longitudeInput.isNotEmpty()) {
                            inputCoordinates = LatLng(latitudeInput.toDouble(), longitudeInput.toDouble())
                            latLngViewModel.latLng = Location(inputCoordinates.latitude, inputCoordinates.longitude)
                        }
                    }) {
                        Text("Go!")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Latitude: ${latLngViewModel.latLng.latitude ?: "Loading..."}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Longitude: ${latLngViewModel.latLng.longitude ?: "Loading..."}")
                Spacer(modifier = Modifier.height(16.dp))
                val finalCoordinates = inputCoordinates ?: coordinates
                finalCoordinates?.let {
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
            latLngViewModel.startGPS()
        } else {
            val permissionLauncher =
                this.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                    if (isGranted) {
                        latLngViewModel.startGPS()
                    } else {
                        Toast.makeText(this, "GPS permission not granted", Toast.LENGTH_LONG).show()
                    }
                }
            permissionLauncher.launch(requiredPermission)
        }
    }

    override fun onLocationChanged(location: Location) {
        latLngViewModel.updateLocation(location)
    }
}

class LatLngViewModel: ViewModel() {
    var latLng = LatLng(51.05, -0.72)
        set(newValue) {
            field = newValue
            latLngLiveData.value = newValue
        }
    var latLngLiveData = MutableLiveData<LatLng>()

    fun startGPS() {
        // Start GPS
    }

    fun updateLocation(location: Location) {
        latLng = LatLng(location.latitude, location.longitude)
    }
}