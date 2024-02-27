package com.maou.busapp.domain.repository

import com.google.android.gms.maps.model.LatLng
import com.maou.busapp.data.BaseResult
import com.maou.busapp.domain.model.Points
import com.maou.busapp.domain.model.Routes
import kotlinx.coroutines.flow.Flow

interface RoutesRepository {
    fun getAllRoutes(): Flow<BaseResult<List<Routes>, String>>
    fun getShelterRoutes(): Flow<BaseResult<List<Routes>, String>>
    fun getDirections(origin: LatLng, destination: LatLng): Flow<BaseResult<List<LatLng>, String>>
}