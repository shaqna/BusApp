package com.maou.busapp.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import com.maou.busapp.R
import pub.devrel.easypermissions.EasyPermissions

class PermissionHelper(private val context: Context) {


    fun hasLocationPermission(): Boolean {
        return EasyPermissions.hasPermissions(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    fun requestLocationPermission(host: Activity, requestCode: Int) {
        EasyPermissions.requestPermissions(
            host,
            context.getString(R.string.location_rationale),
            requestCode,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
}