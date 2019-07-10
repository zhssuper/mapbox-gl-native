package com.example.mapbox.extensions

import android.location.Location
import com.mapbox.mapboxsdk.geometry.LatLng

fun Location.latLong(): LatLng {
    return LatLng(latitude, longitude)
}