package com.maou.busapp.domain.model

data class ActiveBusStop(
    val deviceID: String,
    val busStopID: String,
    val isActive: Boolean
)
