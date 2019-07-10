package com.soy.android.maps.livedata

import android.location.Location
import androidx.lifecycle.Observer
import com.example.mapbox.livedata.HeadingSensorLiveData
import com.example.mapbox.livedata.LocationWithHeadingLiveData
import javax.inject.Inject

class MapViewSensors
@Inject constructor(
    val locationLiveData: LocationWithHeadingLiveData,
    val headingLiveData: HeadingSensorLiveData
) {
    fun requestUpdates(
        locationObserver: Observer<Location>? = null,
        headingObserver: Observer<Float>? = null
    ) {
        locationObserver?.let {
            locationLiveData.observeForever(it)
        }
        headingObserver?.let {
            headingLiveData.observeForever(it)
        }
    }

    fun removeUpdates(
        locationObserver: Observer<Location>? = null,
        headingObserver: Observer<Float>? = null
    ) {
        locationObserver?.let {
            locationLiveData.removeObserver(it)
        }
        headingObserver?.let {
            headingLiveData.removeObserver(it)
        }
    }
}
