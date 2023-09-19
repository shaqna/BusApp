package com.maou.busapp.data.mapping

import com.maou.busapp.data.source.response.ActiveBusStopResponse
import com.maou.busapp.data.source.response.BusEtaResponse
import com.maou.busapp.data.source.response.BusLocationResponse
import com.maou.busapp.data.source.response.BusStopResponse
import com.maou.busapp.data.source.response.Incoming
import com.maou.busapp.data.source.response.RoutesPoint
import com.maou.busapp.data.source.response.RoutesResponse
import com.maou.busapp.domain.model.ActiveBusStop
import com.maou.busapp.domain.model.BusEta
import com.maou.busapp.domain.model.BusIncoming
import com.maou.busapp.domain.model.BusLocation
import com.maou.busapp.domain.model.BusStop
import com.maou.busapp.domain.model.Points
import com.maou.busapp.domain.model.Routes

@JvmName(name = "toListBusStopModel")
fun List<BusStopResponse>.toListModel(): List<BusStop> =
    map { it.toModel() }

@JvmName(name = "toBusStopModel")
fun BusStopResponse.toModel(): BusStop =
    BusStop(
        deviceID = this.deviceID,
        busStopID = this.busStopID,
        busStopName = this.busStopName,
        latitude = this.deviceLocation.latitude,
        longitude = this.deviceLocation.longitude
    )

@JvmName(name = "toBusLocationModel")
fun BusLocationResponse.toModel(): BusLocation =
    BusLocation(
        lastUpdateTimestamp,
        prevStop,
        nextStop,
        isRunning,
        currStop,
        mobilityStatus,
        nextStopETA,
        busCarPlate,
        busRoute
    )

@JvmName(name = "toListBusEtaModel")
fun List<BusEtaResponse>.toListModel(): List<BusEta> =
    map { it.toModel() }

@JvmName(name = "toBusEtaModel")
private fun BusEtaResponse.toModel(): BusEta =
    BusEta(
        line = this.line,
        incoming = this.incoming.map { it.toModel() }
    )

@JvmName(name = "toBusIncomingModel")
fun Incoming.toModel(): BusIncoming =
    BusIncoming(
        lastUpdateTimestamp, currStopName, etaToCurrStop, busCarPlate
    )

@JvmName(name = "toActiveBusStopModel")
fun ActiveBusStopResponse.toModel(): ActiveBusStop =
    ActiveBusStop(deviceID, busStopID, isActive)

@JvmName(name = "toListRoutesModel")
fun List<RoutesResponse>.toListModel(): List<Routes> =
    map {it.toModel() }

@JvmName(name = "toRoutesModel")
private fun RoutesResponse.toModel(): Routes =
    Routes(
        points = busStopPoint.map {
            it.toModel()
        }
    )

@JvmName(name = "toPointsModel")
fun RoutesPoint.toModel(): Points =
    Points(
        latitude, longitude
    )