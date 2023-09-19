package com.maou.busapp.presentation.state

import com.maou.busapp.domain.model.BusEta

sealed class BusEtaUiState {
    object Init: BusEtaUiState()
    data class Loading(val isLoading: Boolean): BusEtaUiState()
    data class Success(val data: List<BusEta>): BusEtaUiState()
    data class Error(val message: String): BusEtaUiState()
}
