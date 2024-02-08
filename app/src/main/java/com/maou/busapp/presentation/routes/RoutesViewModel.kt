package com.maou.busapp.presentation.routes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.maou.busapp.data.BaseResult
import com.maou.busapp.data.source.request.BusStopListRequest
import com.maou.busapp.data.source.request.PredictionBusPathRequest
import com.maou.busapp.domain.usecase.routes.RoutesUseCase
import com.maou.busapp.domain.usecase.transport.TransportUseCase
import com.maou.busapp.presentation.state.BusStopUiState
import com.maou.busapp.presentation.state.PredictionBusPathUiState
import com.maou.busapp.presentation.state.RoutesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModelOf

class RoutesViewModel(
    private val routesUseCase: RoutesUseCase,
    private val transportUseCase: TransportUseCase
) : ViewModel() {

    private val _routesState: MutableStateFlow<RoutesUiState> =
        MutableStateFlow(RoutesUiState.Init)
    val routesState: StateFlow<RoutesUiState> get() = _routesState

    private val _busStopState = MutableStateFlow<BusStopUiState>(BusStopUiState.Init)
    val busStopState: StateFlow<BusStopUiState> get() = _busStopState

    private val _predictionBusPathState: MutableStateFlow<PredictionBusPathUiState> =
        MutableStateFlow(PredictionBusPathUiState.Init)
    val predictionBusPathUiState: StateFlow<PredictionBusPathUiState> get() = _predictionBusPathState

    // for a meantime
    val usmLocation = LatLng(5.356001752593852, 100.30253648752455)
    val uskLocation = LatLng(5.570408134750771, 95.3697489110843)

    val origin = LatLng( 5.559776, 95.317605)
    val destination =  LatLng( 5.554201, 95.318301)


    fun getAllRoutes() {
        viewModelScope.launch {
            routesUseCase.getAllRoutes()
                .onStart {
                    _routesState.value = RoutesUiState.Loading(true)
                }
                .catch { t ->
                    _routesState.value = RoutesUiState.Loading(false)
                    _routesState.value = RoutesUiState.Error(t.message.toString())
                }
                .collect { result ->
                    _routesState.value = RoutesUiState.Loading(false)
                    when (result) {
                        is BaseResult.Error -> _routesState.value =
                            RoutesUiState.Error(result.message)

                        is BaseResult.Success -> _routesState.value =
                            RoutesUiState.Success(result.data)
                    }
                }
        }
    }

    fun fetchBusStops(request: BusStopListRequest) {
        viewModelScope.launch {
            transportUseCase.getBusStopList(request)
                .onStart {
                    _busStopState.value = BusStopUiState.Loading(true)
                }
                .catch { e ->
                    _busStopState.value = BusStopUiState.Loading(false)
                    _busStopState.value = BusStopUiState.Error(e.message.toString())
                }
                .collect { result ->
                    _busStopState.value = BusStopUiState.Loading(false)
                    when (result) {
                        is BaseResult.Error -> _busStopState.value =
                            BusStopUiState.Error(result.message)

                        is BaseResult.Success -> _busStopState.value =
                            BusStopUiState.Success(result.data)
                    }
                }
        }
    }

    fun fetchPredictionBusPath(request: PredictionBusPathRequest) {
        viewModelScope.launch {
            transportUseCase.getPredictionBusPath(request)
                .onStart {
                    _predictionBusPathState.value = PredictionBusPathUiState.Loading(true)
                }.catch { t ->
                    _predictionBusPathState.value = PredictionBusPathUiState.Loading(false)
                    _predictionBusPathState.value =
                        PredictionBusPathUiState.Error(t.message.toString())
                }
                .collect { result ->
                    _predictionBusPathState.value = PredictionBusPathUiState.Loading(false)
                    when (result) {
                        is BaseResult.Error -> _predictionBusPathState.value =
                            PredictionBusPathUiState.Error(result.message)

                        is BaseResult.Success -> _predictionBusPathState.value =
                            PredictionBusPathUiState.Success(result.data)
                    }
                }
        }
    }

    companion object {
        fun inject() = module {
            viewModelOf(::RoutesViewModel)
        }
    }
}