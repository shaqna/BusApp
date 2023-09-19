package com.maou.busapp.data.source.response

import com.squareup.moshi.Json

data class BusLocationResponse(
    val lastUpdateTimestamp: Long,
    val prevStop: String,
    val nextStop: String,
    val isRunning: Int,
    val currStop: String,
    val mobilityStatus: String,
    val nextStopETA: Int,
    @field:Json(name = "busCarplate")
    val busCarPlate: String,
    val busRoute: String
)
