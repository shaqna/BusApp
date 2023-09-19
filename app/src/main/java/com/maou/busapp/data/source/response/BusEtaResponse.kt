package com.maou.busapp.data.source.response

import com.squareup.moshi.Json

data class BusEtaResponse(
    val line: String,
    val incoming: List<Incoming>
)

data class Incoming(
    val lastUpdateTimestamp: Long,
    val currStopName: String,
    @field:Json(name = "ETAtoCurrStop")
    val etaToCurrStop: Long,
    @field:Json(name = "busCarplate")
    val busCarPlate: String
)
