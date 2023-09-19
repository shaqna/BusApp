package com.maou.busapp.data.repository

import com.maou.busapp.data.BaseResult
import com.maou.busapp.data.mapping.toListModel
import com.maou.busapp.data.mapping.toModel
import com.maou.busapp.data.source.request.ActiveBusStopRequest
import com.maou.busapp.data.source.request.BusEtaRequest
import com.maou.busapp.data.source.request.BusStopListRequest
import com.maou.busapp.data.source.request.BusStopRequest
import com.maou.busapp.data.source.request.GeneralRequest
import com.maou.busapp.data.source.request.PredictionBusPathRequest
import com.maou.busapp.data.source.service.ApiService
import com.maou.busapp.domain.model.ActiveBusStop
import com.maou.busapp.domain.model.BusEta
import com.maou.busapp.domain.model.BusLocation
import com.maou.busapp.domain.model.BusStop
import com.maou.busapp.domain.model.Points
import com.maou.busapp.domain.repository.TransportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class TransportRepositoryImpl(private val apiService: ApiService) : TransportRepository {
    override fun getBusStopList(request: BusStopListRequest): Flow<BaseResult<List<BusStop>, String>> =
        flow {
            val response = apiService.getBusStopList(GeneralRequest(item = request))
            if(response.statusCode != 200)
                emit(BaseResult.Error("Error"))


            emit(BaseResult.Success(response.body.toListModel()))
        }.catch { e ->
            emit(BaseResult.Error(e.message.toString()))
        }.flowOn(Dispatchers.IO)

    override fun getBusLocationInfo(): Flow<BaseResult<List<BusLocation>, String>> {
        TODO("Not yet implemented")
    }

    override fun getBusEtaInfo(request: BusEtaRequest): Flow<BaseResult<List<BusEta>, String>> {
        return flow {
            val response = apiService.getBusEtaInfo(GeneralRequest(item = request))
            if(response.statusCode != 200) {
                emit(BaseResult.Error("Error"))
            }

            emit(BaseResult.Success(response.body.toListModel()))
        }.catch { e->
            emit(BaseResult.Error(e.message.toString()))
        }.flowOn(Dispatchers.IO)
    }

    override fun getActiveBusStop(request: ActiveBusStopRequest): Flow<BaseResult<List<ActiveBusStop>, String>> {
        TODO("Not yet implemented")
    }

    override fun getBusStopInfo(request: BusStopRequest): Flow<BaseResult<BusStop, String>> {
        return flow {
            val response = apiService.getBusStopInfo(GeneralRequest(item = request))
            if(response.statusCode != 200)
                emit(BaseResult.Error("Error"))
            emit(BaseResult.Success(response.body.toModel()))
        }.catch { e->
            emit(BaseResult.Error(e.message.toString()))
        }.flowOn(Dispatchers.IO)
    }

    override fun getPredictionBusPath(request: PredictionBusPathRequest): Flow<BaseResult<Points, String>> {
        return flow {
            val response = apiService.getPredictionBusPath(GeneralRequest(item = request))
            if(response.statusCode != 200)
                emit(BaseResult.Error("Error"))

            emit(BaseResult.Success(response.body.currentLocation))
        }.catch {e ->
            emit(BaseResult.Error(e.message.toString()))
        }.flowOn(Dispatchers.IO)
    }
}