package com.maou.busapp.di

import com.maou.busapp.utils.BottomSheetHelper
import com.maou.busapp.utils.GpsHelper
import com.maou.busapp.utils.PermissionHelper
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val permissionHelper = module {
    singleOf(::PermissionHelper)
}

val gpsHelper = module {
    singleOf(::GpsHelper)
}

val bottomSheetHelper = module {
    singleOf(::BottomSheetHelper)
}