package com.arsinde.flexkioskapp

import android.app.Application

import com.arsinde.flexkioskapp.core.datastore.coreDataStoreModule
import com.arsinde.flexkioskapp.feature.home.di.homeModule
import com.arsinde.flexkioskapp.core.network.coreNetworkModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(module {}, homeModule, coreDataStoreModule, coreNetworkModule)
        }
    }
}