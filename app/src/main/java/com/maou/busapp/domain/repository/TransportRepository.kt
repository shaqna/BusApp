package com.maou.busapp.domain.repository

import com.maou.busapp.data.BaseResult
import com.maou.busapp.data.source.request.ActiveBusStopRequest
import com.maou.busapp.data.source.request.BusEtaRequest
import com.maou.busapp.data.source.request.BusStopListRequest
import com.maou.busapp.data.source.request.BusStopRequest
import com.maou.busapp.data.source.request.PredictionBusPathRequest
import com.maou.busapp.domain.model.ActiveBusStop
import com.maou.busapp.domain.model.BusEta
import com.maou.busapp.domain.model.BusLocation
import com.maou.busapp.domain.model.BusStop
import com.maou.busapp.domain.model.Points
import kotlinx.coroutines.flow.Flow

interface TransportRepository {

    fun getBusStopList(request: BusStopListRequest): Flow<BaseResult<List<BusStop>, String>>
    fun getBusLocationInfo(): Flow<BaseResult<List<BusLocation>, String>>
    fun getBusEtaInfo(request: BusEtaRequest): Flow<BaseResult<List<BusEta>, String>>
    fun getActiveBusStop(request: ActiveBusStopRequest): Flow<BaseResult<List<ActiveBusStop>, String>>
    fun getBusStopInfo(request: BusStopRequest): Flow<BaseResult<BusStop, String>>
    fun getPredictionBusPath(request: PredictionBusPathRequest): Flow<BaseResult<Points, String>>

}