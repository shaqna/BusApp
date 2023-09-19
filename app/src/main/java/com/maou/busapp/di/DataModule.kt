package com.maou.busapp.di

import com.maou.busapp.BuildConfig
import com.maou.busapp.data.controller.LocationControllerImpl
import com.maou.busapp.data.repository.RoutesRepositoryImpl
import com.maou.busapp.data.repository.TransportRepositoryImpl
import com.maou.busapp.data.source.service.ApiService
import com.maou.busapp.domain.repository.RoutesRepository
import com.maou.busapp.domain.repository.TransportRepository
import com.maou.busapp.domain.usecase.location.LocationController
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

val dataModule = module {
    singleOf(::LocationControllerImpl) {
        bind<LocationController>()
    }

    singleOf(::TransportRepositoryImpl) {
        bind<TransportRepository>()
    }

    singleOf(::RoutesRepositoryImpl) {
        bind<RoutesRepository>()
    }

    single {
        val retrofit = Retrofit.Builder().baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .build()
            ).build()

        retrofit.create(ApiService::class.java)
    }
}