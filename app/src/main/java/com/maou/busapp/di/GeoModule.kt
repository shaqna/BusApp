package com.maou.busapp.di

import com.google.maps.GeoApiContext
import com.maou.busapp.R
import com.maou.busapp.data.source.service.GoogleMapsService
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val geoModule = module {
    single {
        GeoApiContext.Builder().apiKey(androidApplication().getString(R.string.lora_api_key)).build()
    }

    singleOf(::GoogleMapsService)
}