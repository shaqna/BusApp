package com.maou.busapp.data.controller

import android.location.Location
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.maou.busapp.presentation.maps.MapsActivity

class LocationCallback(
    private val onLocationUpdate: (Location) -> Unit
): LocationCallback() {
    override fun onLocationResult(locationResult: LocationResult) {
        super.onLocationResult(locationResult)

        for(location in locationResult.locations) {
            Log.d(MapsActivity.TAG, location.toString())
            onLocationUpdate(location)
        }
    }
}