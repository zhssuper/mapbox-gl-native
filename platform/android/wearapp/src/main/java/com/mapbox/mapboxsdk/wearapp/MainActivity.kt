package com.mapbox.mapboxsdk.wearapp

import android.os.Bundle
import android.support.wearable.activity.WearableActivity
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : WearableActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Mapbox.getInstance(this, "INSERT ACCESSTOKEN")
    setContentView(R.layout.activity_main)

    // Enables Always-on
    setAmbientEnabled()
    mapView.onCreate(savedInstanceState)
    mapView.getMapAsync{
      it.setStyle(Style.MAPBOX_STREETS)
    }
  }

  override fun onStart() {
    super.onStart()
    mapView.onStart()
  }

  override fun onResume() {
    super.onResume()
    mapView.onResume()
  }

  override fun onPause() {
    super.onPause()
    mapView.onPause()
  }

  override fun onStop() {
    super.onStop()
    mapView.onStop()
  }

  override fun onLowMemory() {
    super.onLowMemory()
    mapView.onLowMemory()
  }

  override fun onDestroy() {
    super.onDestroy()
    mapView.onDestroy()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    mapView.onSaveInstanceState(outState)
  }

}
