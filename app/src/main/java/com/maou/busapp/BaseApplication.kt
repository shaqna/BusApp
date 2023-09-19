package com.maou.busapp

import android.app.Application
import com.maou.busapp.di.bottomSheetHelper
import com.maou.busapp.di.dataModule
import com.maou.busapp.di.domainModule
import com.maou.busapp.di.gpsHelper
import com.maou.busapp.di.locationRequest
import com.maou.busapp.di.permissionHelper
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class BaseApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(level = Level.ERROR)
            androidContext(this@BaseApplication)
            loadKoinModules(
                listOf(
                    permissionHelper,
                    bottomSheetHelper,
                    gpsHelper,
                    dataModule,
                    locationRequest,
                    domainModule
                )
            )
        }
    }
}