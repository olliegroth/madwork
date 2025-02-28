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
            var coordinates by remember { mutableStateOf<LatLng?>(null) }

            latLngViewModel.latLngLiveData.observe(this) {
                coordinates = it
            }

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
                            coordinates = LatLng(latitudeInput.toDouble(), longitudeInput.toDouble())
                            val location = Location("manual").apply {
                                latitude = coordinates!!.latitude
                                longitude = coordinates!!.longitude
                            }
                            latLngViewModel.updateLocation(location)
                        }
                    }) {
                        Text("Go!")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Latitude: ${latLngViewModel.latLng.latitude}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Longitude: ${latLngViewModel.latLng.longitude}")
                Spacer(modifier = Modifier.height(16.dp))

                MapLibre(
                    modifier = Modifier.fillMaxSize(),
                    styleBuilder = org.maplibre.android.maps.Style.Builder().fromUri("https://tiles.openfreemap.org/styles/bright"),
                    cameraPosition = CameraPosition(coordinates, 14.0)
                )
            }
        }
    }

    private fun checkPermissions() {
        val requiredPermission = Manifest.permission.ACCESS_FINE_LOCATION

        if (checkSelfPermission(requiredPermission) == PackageManager.PERMISSION_GRANTED) {
            startGPS()
        } else {
            val permissionLauncher =
                this.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                    if (isGranted) {
                        startGPS()
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

    @SuppressLint("MissingPermission")
    fun startGPS() {
        val mgr = getSystemService(LOCATION_SERVICE) as LocationManager
        mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this )
    }
}

class LatLngViewModel: ViewModel() {
    var latLng = LatLng(51.05, -0.72)
        set(newValue) {
            field = newValue
            latLngLiveData.value = newValue
        }
    var latLngLiveData = MutableLiveData<LatLng>()

    fun updateLocation(location: Location) {
        latLng = LatLng(location.latitude, location.longitude)
    }
}