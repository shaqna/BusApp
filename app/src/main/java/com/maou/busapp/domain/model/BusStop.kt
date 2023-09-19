package com.maou.busapp.domain.model

import com.maou.busapp.data.source.response.DeviceLocation

data class BusStop(
    val deviceID: String,
    val busStopID: String,
    val busStopName: String,
    val latitude: String,
    val longitude: String
)
