package com.maou.busapp.di

import com.maou.busapp.domain.usecase.routes.RoutesInteractor
import com.maou.busapp.domain.usecase.routes.RoutesUseCase
import com.maou.busapp.domain.usecase.transport.TransportInteractor
import com.maou.busapp.domain.usecase.transport.TransportUseCase
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val domainModule = module {
    singleOf(::TransportInteractor) {
        bind<TransportUseCase>()
    }

    singleOf(::RoutesInteractor) {
        bind<RoutesUseCase>()
    }
}