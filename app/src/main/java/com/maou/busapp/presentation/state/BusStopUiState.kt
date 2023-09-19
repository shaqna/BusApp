package com.maou.busapp.presentation.state

import com.maou.busapp.domain.model.BusStop

sealed class BusStopUiState {
    object Init: BusStopUiState()
    data class Loading(val isLoading: Boolean): BusStopUiState()
    data class Success(val data: List<BusStop>): BusStopUiState()
    data class Error(val message: String): BusStopUiState()
}