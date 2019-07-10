package com.mapbox.mapboxsdk.wearapp

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.SystemClock
import android.support.constraint.ConstraintLayout
import android.support.wearable.activity.WearableActivity
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.Observer
import com.mapbox.mapboxsdk.wearapp.ui.MapboxMapView
import com.mapbox.mapboxsdk.wearapp.ui.extensions.addViewAndMatchItToParent
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.soy.android.maps.compass.WassCompassEngine
import com.soy.android.maps.livedata.MapViewSensors
import dagger.android.AndroidInjection
import javax.inject.Inject

class MainActivity : WearableActivity(), PermissionsListener, OnMapReadyCallback {
    override fun onMapReady(mapboxMap: MapboxMap) {
        measureAndPrintTimeMillis("onStart.requestSensorUpdates") {
            requestSensorUpdates()
        }
        mapboxMapView.onMapReady(mapboxMap)
    }

    private lateinit var mapboxMapView: MapboxMapView

    @Inject
    lateinit var sensors: MapViewSensors

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            createMapboxMapView()
        } else {
            PermissionsManager(this).requestLocationPermissions(this)
        }
        setAmbientEnabled()
    }

    private fun createMapboxMapView() {
        Mapbox.getInstance(this, getString(R.string.mapbox_api_key))
        val location = sensors.locationLiveData.value
        val root = findViewById<ConstraintLayout>(R.id.content)
        mapboxMapView = MapboxMapView(this, location).apply {
            compassEngine = WassCompassEngine(sensors.headingLiveData)
            root.addViewAndMatchItToParent(this)
            onCreate(null)
            getMapAsync(this@MainActivity)
        }
    }

    public override fun onResume() {
        super.onResume()
        measureAndPrintTimeMillis("onResume") {
            mapboxMapView.onResume()
        }
    }

    public override fun onPause() {
        super.onPause()
        measureAndPrintTimeMillis("onPause") {
            mapboxMapView.onPause()
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        measureAndPrintTimeMillis("onLowMemory") {
            mapboxMapView.onLowMemory()
        }
    }

    override fun onStart() {
        super.onStart()
        measureAndPrintTimeMillis("onStart.mapOnStart") {
            mapboxMapView.onStart()
        }
    }

    override fun onStop() {
        super.onStop()
        removeSensorUpdates()
        measureAndPrintTimeMillis("onStop") {
            mapboxMapView.onStop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        measureAndPrintTimeMillis("onDestroy") {
            mapboxMapView.onDestroy()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        measureAndPrintTimeMillis("onSaveInstanceState") {
            mapboxMapView.onSaveInstanceState(outState)
        }
    }

    private fun requestSensorUpdates() {
        sensors.requestUpdates(locationObserver)
        mapboxMapView.compassEngine?.requestUpdates()
    }

    private fun removeSensorUpdates() {
        sensors.removeUpdates(locationObserver)
        mapboxMapView.compassEngine?.removeUpdates()
    }

    // Sensors
    private val locationObserver: Observer<Location> = Observer { location ->
        measureAndPrintTimeMillis("onNewLocation") {
            mapboxMapView.onNewLocation(location)
        }
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            createMapboxMapView()
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show()
        }
    }
}

/**
 * Executes the given [block] and returns elapsed time in milliseconds.
 */
public inline fun <T> measureAndPrintTimeMillis(blockName: String, block: () -> T): T {
    val start = SystemClock.elapsedRealtime()
    val t = block()
    val elapsedTime = SystemClock.elapsedRealtime() - start
    Log.d("MapExample", "It took $elapsedTime ms to run [$blockName]")
    return t
}

