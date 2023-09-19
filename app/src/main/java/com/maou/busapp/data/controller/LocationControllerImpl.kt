package com.maou.busapp.data.controller

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.maou.busapp.domain.usecase.location.LocationController
import com.maou.busapp.presentation.maps.MapsActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.inject
import org.koin.core.component.KoinComponent

class LocationControllerImpl(private val context: Context) : LocationController, KoinComponent {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val locationRequest: LocationRequest by inject()

    private val locationCallback = LocationCallback { locationResult ->
        Log.d(MapsActivity.TAG, "controller: $locationResult")
        _locationState.value = locationResult
    }

    private val _locationState = MutableStateFlow<Location?>(null)

    override val locationState: StateFlow<Location?>
        get() = _locationState


    @SuppressLint("MissingPermission")
    override fun startRequestLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (exception: SecurityException) {
            Log.e("Location Controller", "Error : " + exception.message)
        }
    }

    override fun stopRequestLocationUpdates() {
        _locationState.update {
            null
        }
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

}