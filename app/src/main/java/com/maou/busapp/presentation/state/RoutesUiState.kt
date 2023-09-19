package com.maou.busapp.presentation.state

import com.maou.busapp.domain.model.Routes

sealed class RoutesUiState {
    object Init: RoutesUiState()
    data class Loading(val isLoading: Boolean): RoutesUiState()
    data class Error(val message: String): RoutesUiState()
    data class Success(val routes: List<Routes>): RoutesUiState()
}
