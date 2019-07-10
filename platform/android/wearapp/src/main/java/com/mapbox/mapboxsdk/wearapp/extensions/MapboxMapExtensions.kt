package com.soy.android.maps.extensions

import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume

fun MapboxMap.isCameraInTrackingMode() = getActivatedLocationComponent()?.run {
    cameraMode == CameraMode.TRACKING_COMPASS ||
        cameraMode == CameraMode.TRACKING ||
        cameraMode == CameraMode.TRACKING_GPS ||
        cameraMode == CameraMode.TRACKING_GPS_NORTH
}

fun MapboxMap.getActivatedLocationComponent() = locationComponent.takeIf {
    it.isLocationComponentEnabled
} ?: run {
    Timber.w("Calling location component, but it is not enabled.")
    null
}
