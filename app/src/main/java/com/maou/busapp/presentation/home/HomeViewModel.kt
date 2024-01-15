package com.maou.busapp.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.maou.busapp.data.BaseResult
import com.maou.busapp.data.source.request.BusEtaRequest
import com.maou.busapp.data.source.request.BusStopListRequest
import com.maou.busapp.domain.model.BusStop
import com.maou.busapp.domain.usecase.location.LocationController
import com.maou.busapp.domain.usecase.transport.TransportUseCase
import com.maou.busapp.presentation.maps.MapsActivity
import com.maou.busapp.presentation.state.BusEtaUiState
import com.maou.busapp.presentation.state.BusStopUiState
import com.maou.busapp.presentation.state.BusUiState
import com.maou.busapp.presentation.state.LocationUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModelOf

class HomeViewModel(
    private val locationController: LocationController,
    private val transportUseCase: TransportUseCase
): ViewModel() {

    private val _locationState = MutableStateFlow<LocationUiState>(LocationUiState.Loading)
    val locationState: StateFlow<LocationUiState> = _locationState

    private val _busState = MutableStateFlow<BusUiState>(BusUiState.Init)
    val busState: StateFlow<BusUiState> get() = _busState

    private val _busStopState = MutableStateFlow<BusStopUiState>(BusStopUiState.Init)
    val busStopState: StateFlow<BusStopUiState> get() = _busStopState

    private val _busEtaState = MutableStateFlow<BusEtaUiState>(BusEtaUiState.Init)
    val busEtaState: StateFlow<BusEtaUiState> get() = _busEtaState

    var busStop: BusStop? = null
    var stateProvince: String? = null

    // for a meantime
    val usmLocation = LatLng(5.356001752593852, 100.30253648752455)
    val uskLocation = LatLng(5.570408134750771, 95.3697489110843)

    fun startRequestLocationUpdates() {
        Log.d(MapsActivity.TAG, "viewModel start")
        locationController.startRequestLocationUpdates()
        viewModelScope.launch {
            locationController.locationState.collectLatest {
                Log.d(MapsActivity.TAG, "state: $it")
                it?.let {location ->
                    _locationState.value = LocationUiState.OnLocationResult(location)
                }
            }
        }
    }

    fun stopRequestLocationUpdates() {
        locationController.stopRequestLocationUpdates()
    }

    fun searchBusByBusStop(busStopName: String) {

    }

    fun fetchBusStops(request: BusStopListRequest) {
        viewModelScope.launch {
            transportUseCase.getBusStopList(request)
                .onStart {
                    _busStopState.value = BusStopUiState.Loading(true)
                }
                .catch {e->
                    _busStopState.value = BusStopUiState.Loading(false)
                    _busStopState.value = BusStopUiState.Error(e.message.toString())
                }
                .collect { result ->
                    _busStopState.value = BusStopUiState.Loading(false)
                    when(result) {
                        is BaseResult.Error -> _busStopState.value = BusStopUiState.Error(result.message)
                        is BaseResult.Success -> _busStopState.value = BusStopUiState.Success(result.data)

                    }
                }
        }
    }

    fun fetchBusEta(request: BusEtaRequest) {
        viewModelScope.launch {
            transportUseCase.getBusEtaInfo(request)
                .onStart {
                    _busEtaState.value = BusEtaUiState.Loading(true)
                }
                .catch { e->
                    _busEtaState.value = BusEtaUiState.Loading(false)
                    _busEtaState.value = BusEtaUiState.Error(e.message.toString())

                }
                .collect { result ->
                    _busEtaState.value = BusEtaUiState.Loading(false)
                    when(result) {
                        is BaseResult.Error -> _busEtaState.value = BusEtaUiState.Error(result.message)
                        is BaseResult.Success -> _busEtaState.value = BusEtaUiState.Success(result.data)
                    }
                }
        }
    }

    companion object {
        fun inject() = module {
            viewModelOf(::HomeViewModel)
        }
    }
}