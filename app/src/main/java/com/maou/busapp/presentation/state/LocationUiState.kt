package com.maou.busapp.presentation.state

import android.location.Location



sealed class LocationUiState {
    object Loading: LocationUiState()
    data class OnLocationResult(val location: Location? = null): LocationUiState()
}