package com.maou.busapp.data.source.request

data class BusEtaRequest(
    val country: String,
    val state: String,
    val busStopName: String
)
