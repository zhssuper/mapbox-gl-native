package com.asoy.wass.sdk.android.sensor

import android.location.Location
import android.location.LocationManager
import javax.inject.Inject

class MockLocationSensor
@Inject constructor() {

    private var currentPosition = 0
    private var step = 1

    fun generateNextValue(): Location {
        val lat = lats[currentPosition]
        val long = longs[currentPosition]
        currentPosition += step
        if (currentPosition >= lats.size) {
            step = -1
            currentPosition = lats.size - 1
        } else if (currentPosition <= 0) {
            step = 1
            currentPosition = 0
        }
        return Location(LocationManager.GPS_PROVIDER).also { location ->
            location.accuracy = 0.0f
            location.latitude = lat
            location.longitude = long
            location.altitude = 0.0
        }
    }
}
