package com.maou.busapp.data.source.response

data class RoutesResponse(
    val busStopPoint: List<RoutesPoint>
)

data class RoutesPoint(
    val latitude: Double,
    val longitude: Double
)
