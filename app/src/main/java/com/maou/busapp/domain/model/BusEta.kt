package com.maou.busapp.domain.model

import com.maou.busapp.data.source.response.Incoming
import com.squareup.moshi.Json

data class BusEta(
    val line: String,
    val incoming: List<BusIncoming>
)

data class BusIncoming(
    val lastUpdateTimestamp: Long,
    val currStopName: String,
    val etaToCurrStop: Long,
    val busCarPlate: String
)
