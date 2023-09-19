package com.maou.busapp.domain.usecase.transport

import com.maou.busapp.data.BaseResult
import com.maou.busapp.data.source.request.BusEtaRequest
import com.maou.busapp.data.source.request.BusStopListRequest
import com.maou.busapp.data.source.request.BusStopRequest
import com.maou.busapp.data.source.request.PredictionBusPathRequest
import com.maou.busapp.domain.model.BusEta
import com.maou.busapp.domain.model.BusStop
import com.maou.busapp.domain.model.Points
import kotlinx.coroutines.flow.Flow

interface TransportUseCase {

    suspend fun getBusStopList(request: BusStopListRequest): Flow<BaseResult<List<BusStop>, String>>
    fun getBusEtaInfo(request: BusEtaRequest): Flow<BaseResult<List<BusEta>, String>>
    suspend fun getBusStopInfo(request: BusStopRequest): Flow<BaseResult<BusStop, String>>
    fun getPredictionBusPath(request: PredictionBusPathRequest): Flow<BaseResult<Points, String>>

}