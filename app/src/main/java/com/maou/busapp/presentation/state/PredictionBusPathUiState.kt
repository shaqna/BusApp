package com.maou.busapp.presentation.state

import com.maou.busapp.domain.model.Points

sealed class PredictionBusPathUiState {

    object Init: PredictionBusPathUiState()
    data class Loading(val isLoading: Boolean): PredictionBusPathUiState()
    data class Error(val message: String): PredictionBusPathUiState()
    data class Success(val data: Points): PredictionBusPathUiState()

}

