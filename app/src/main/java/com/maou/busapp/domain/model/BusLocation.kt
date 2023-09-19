package com.maou.busapp.domain.model


data class BusLocation(
    val lastUpdateTimestamp: Long,
    val prevStop: String,
    val nextStop: String,
    val isRunning: Int,
    val currStop: String,
    val mobilityStatus: String,
    val nextStopETA: Int,
    val busCarPlate: String,
    val busRoute: String
)
