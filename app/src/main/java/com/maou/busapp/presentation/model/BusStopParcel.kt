package com.maou.busapp.presentation.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BusStopParcel(
    val deviceID: String,
    val busStopID: String,
    val busStopName: String,
    val latitude: String,
    val longitude: String
): Parcelable
