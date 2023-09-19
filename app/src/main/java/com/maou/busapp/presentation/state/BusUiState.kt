package com.maou.busapp.presentation.state

import com.maou.busapp.domain.model.Bus

sealed class BusUiState {
    object Init : BusUiState()
    data class Loading(val isLoading: Boolean): BusUiState()
    data class Success(val data: List<Bus>): BusUiState()
    data class Error(val message: String): BusUiState()
}
