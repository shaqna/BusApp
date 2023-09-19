package com.maou.busapp.data.source.request

data class ActiveBusStopRequest(
    val country: String,
    val state: String,
    val city: String,
    val busRoute: String
)
