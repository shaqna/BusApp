package com.maou.busapp.domain.usecase.location

import android.location.Location
import kotlinx.coroutines.flow.StateFlow

interface LocationController {

    val locationState: StateFlow<Location?>

    fun startRequestLocationUpdates()
    fun stopRequestLocationUpdates()
}