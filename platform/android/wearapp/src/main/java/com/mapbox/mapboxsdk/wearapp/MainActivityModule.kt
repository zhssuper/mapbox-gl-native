package com.mapbox.mapboxsdk.wearapp

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MainActivityModule {
    @ContributesAndroidInjector
    internal abstract fun mainActivity(): MainActivity
}
