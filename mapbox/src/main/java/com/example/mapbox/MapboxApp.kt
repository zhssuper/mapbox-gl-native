package com.mapbox.mapboxsdk.wearapp

import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class MapboxApp : DaggerApplication() {
    private val appComponent: AndroidInjector<MapboxApp> by lazy {
        DaggerAppComponent
            .builder()
            .create(this)
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return appComponent
    }
}