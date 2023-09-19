package com.maou.busapp.domain.usecase.transport

import com.maou.busapp.data.BaseResult
import com.maou.busapp.data.source.request.BusEtaRequest
import com.maou.busapp.data.source.request.BusStopListRequest
import com.maou.busapp.data.source.request.BusStopRequest
import com.maou.busapp.data.source.request.PredictionBusPathRequest
import com.maou.busapp.domain.model.BusEta
import com.maou.busapp.domain.model.BusStop
import com.maou.busapp.domain.model.Points
import com.maou.busapp.domain.repository.TransportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransportInteractor(private val repository: TransportRepository) : TransportUseCase {
    override suspend fun getBusStopList(request: BusStopListRequest): Flow<BaseResult<List<BusStop>, String>> {
        val response = repository.getBusStopList(request)
        return response.map { result ->
            when (result) {
                is BaseResult.Error -> result
                is BaseResult.Success -> {
                    val filteredBusStop = result.data.filter { busStop ->
                        busStop.latitude.isNotEmpty() && busStop.longitude.isNotEmpty()
                    }

                    BaseResult.Success(filteredBusStop)
                }
            }
        }
    }

    override fun getBusEtaInfo(request: BusEtaRequest): Flow<BaseResult<List<BusEta>, String>> {
        return repository.getBusEtaInfo(request)
    }

    override suspend fun getBusStopInfo(request: BusStopRequest): Flow<BaseResult<BusStop, String>> {
        return repository.getBusStopInfo(request)
    }

    override fun getPredictionBusPath(request: PredictionBusPathRequest): Flow<BaseResult<Points, String>> {
        return repository.getPredictionBusPath(request)
    }
}