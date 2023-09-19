package com.maou.busapp.domain.usecase.routes

import com.maou.busapp.data.BaseResult
import com.maou.busapp.domain.model.Routes
import com.maou.busapp.domain.repository.RoutesRepository
import kotlinx.coroutines.flow.Flow

class RoutesInteractor(private val repository: RoutesRepository): RoutesUseCase {
    override fun getAllRoutes(): Flow<BaseResult<List<Routes>, String>> {
        return repository.getAllRoutes()
    }
}