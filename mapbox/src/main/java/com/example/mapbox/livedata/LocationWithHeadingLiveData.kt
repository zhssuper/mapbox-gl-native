package com.mapbox.mapboxsdk.wearapp.livedata

import android.location.Location
import androidx.lifecycle.MediatorLiveData
import com.soy.android.maps.livedata.LocationSensorLiveData
import javax.inject.Inject

/**
 * [MediatorLiveData] wrapper used to expose the [AndroidLocation].
 *
 *  The value is mediated based on the last recorded values of [LocationSensorLiveData]
 *  and [HeadingSensorLiveData], and each time [LocationSensorLiveData] emits a value
 *  a new [AndroidLocation] value is emitted by this mediator live data. Note that if
 */
class LocationWithHeadingLiveData
@Inject constructor(
    headingSensorLiveData: HeadingSensorLiveData,
    locationSensorLiveData: LocationSensorLiveData
) : MediatorLiveData<Location>() {
    init {
        addSource(locationSensorLiveData) {
            headingSensorLiveData.value?.let {heading ->
                // Convert radian to degree
                it.bearing = Math.toDegrees(heading.toDouble()).toFloat()
                value = it
            }
        }
        addSource(headingSensorLiveData) {}
    }
}
