package com.maou.busapp.data.source.response

data class ActiveBusStopResponse(
    val deviceID: String,
    val busStopID: String,
    val isActive: Boolean
)
