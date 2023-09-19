package com.maou.busapp.data.source.response

import com.maou.busapp.domain.model.BusStop

data class BusStopResponse(
    val deviceID: String,
    val busStopID: String,
    val busStopName: String,
    val deviceLocation: DeviceLocation
)

data class DeviceLocation(
    val latitude: String,
    val longitude: String
)

