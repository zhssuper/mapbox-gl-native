package com.soy.android.maps.compass

import android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH
import androidx.lifecycle.Observer
import com.example.mapbox.livedata.HeadingSensorLiveData
import com.mapbox.mapboxsdk.location.CompassEngine
import com.mapbox.mapboxsdk.location.CompassListener

class WassCompassEngine(
    private val headingSensorLiveData: HeadingSensorLiveData?
) : CompassEngine {

    private val compassListeners = mutableListOf<CompassListener>()

    private var lastHeading: Float = 0.0f

    private val headingObserver: Observer<Float> = Observer {
        lastHeading = Math.toDegrees(it.toDouble()).toFloat()
        notifyCompassChangeListeners(lastHeading)
    }

    /**
     * Always returns [SENSOR_STATUS_ACCURACY_HIGH] and should not mean anything in this context
     */
    override fun getLastAccuracySensorStatus() = SENSOR_STATUS_ACCURACY_HIGH

    override fun addCompassListener(compassListener: CompassListener) {
        compassListeners.add(compassListener)
    }

    override fun removeCompassListener(compassListener: CompassListener) {
        compassListeners.remove(compassListener)
    }

    override fun getLastHeading() = lastHeading

    fun requestUpdates() = headingSensorLiveData?.observeForever(headingObserver)

    fun removeUpdates() = headingSensorLiveData?.removeObserver(headingObserver)

    private fun notifyCompassChangeListeners(heading: Float) {
        lastHeading = heading.apply {
            for (compassListener in compassListeners) {
                compassListener.onCompassChanged(this)
            }
        }
    }
}
