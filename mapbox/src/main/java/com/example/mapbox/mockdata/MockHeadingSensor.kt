package com.mapbox.mapboxsdk.wearapp.mockdata

import javax.inject.Inject

class MockHeadingSensor
@Inject constructor() {

    private var heading: Float = 0f

    fun generateNextValue(): Float {
        // Rotate 90deg in 10 seconds
        heading += 0.1570796326794f
        if (heading >= 2.0f * Math.PI.toFloat()) {
            heading = 0.0f
        }
        return heading
    }
}
