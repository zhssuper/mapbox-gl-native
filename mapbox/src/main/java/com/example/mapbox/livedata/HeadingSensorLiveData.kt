package com.mapbox.mapboxsdk.wearapp.livedata

import androidx.lifecycle.LiveData
import com.mapbox.mapboxsdk.wearapp.mockdata.MockHeadingSensor
import kotlinx.coroutines.*
import javax.inject.Inject

private const val GPS_FIX_INTERVAL_MILLISEC = 1000L

class HeadingSensorLiveData @Inject constructor(
    private val mockHeadingSensor: MockHeadingSensor
) : LiveData<Float>() {

    private val job = Job()
    private val backgroundScope = CoroutineScope(Dispatchers.Default + job)

    override fun onActive() {
        super.onActive()
        backgroundScope.launch {
            while(true) {
                delay(GPS_FIX_INTERVAL_MILLISEC)
                withContext(Dispatchers.Main) {
                    value = mockHeadingSensor.generateNextValue()
                }
            }
        }
    }

    override fun onInactive() {
        job.cancel()
        super.onInactive()
    }
}
