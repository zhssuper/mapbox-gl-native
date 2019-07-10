package com.soy.android.maps.livedata

import android.location.Location
import androidx.lifecycle.LiveData
import com.asoy.wass.sdk.android.sensor.MockLocationSensor
import kotlinx.coroutines.*
import javax.inject.Inject

private const val HEADING_INTERVAL_MILLISEC = 500L

class LocationSensorLiveData
@Inject constructor(
    private val mockLocationSensor: MockLocationSensor
) : LiveData<Location>() {

    private val job = Job()
    private val backgroundScope = CoroutineScope(Dispatchers.Default + job)

    override fun onActive() {
        super.onActive()
        backgroundScope.launch {
            while (true) {
                delay(HEADING_INTERVAL_MILLISEC)
                withContext(Dispatchers.Main) {
                    value = mockLocationSensor.generateNextValue()
                }
            }
        }
    }

    override fun onInactive() {
        job.cancel()
        super.onInactive()
    }
}
