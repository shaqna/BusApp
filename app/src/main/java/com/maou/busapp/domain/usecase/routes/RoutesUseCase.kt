package com.maou.busapp.domain.usecase.routes

import com.maou.busapp.data.BaseResult
import com.maou.busapp.domain.model.Routes
import kotlinx.coroutines.flow.Flow

interface RoutesUseCase {
    fun getAllRoutes(): Flow<BaseResult<List<Routes>, String>>
}