package com.maou.busapp.di

import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import org.koin.dsl.module
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

val locationRequest = module {
    single {
        val request = LocationRequest.create().apply {
            interval = 1000
            priority = Priority.PRIORITY_HIGH_ACCURACY
            fastestInterval = 1000
        }

        request
    }
}