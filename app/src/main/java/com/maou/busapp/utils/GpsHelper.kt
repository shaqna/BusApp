package com.maou.busapp.utils

import android.content.Context
import android.location.LocationManager

class GpsHelper(private val context: Context) {

    fun isGpsEnable(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

}