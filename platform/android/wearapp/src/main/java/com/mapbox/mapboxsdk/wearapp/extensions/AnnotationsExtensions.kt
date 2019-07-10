package com.soy.android.maps.breadcrumb

import com.mapbox.mapboxsdk.wearapp.ui.PAUSE_LINE_PATTERN
import com.mapbox.mapboxsdk.wearapp.ui.START_SYMBOL_ICON_ID
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.plugins.annotation.*
import com.mapbox.mapboxsdk.style.layers.Property

private const val MAP_LINE_WIDTH = 4f // In pixels
private const val COLOR_MAP_LINE_REGULAR = "#FF5640"
private const val COLOR_MAP_LINE_PAUSE = "#00000000"
private const val MAP_LINE_PAUSE_OPACITY = 0.2f

internal fun SymbolManager.addStartExerciseSymbol(latLng: LatLng) {
    val option = SymbolOptions()
        .withLatLng(latLng)
        .withIconImage(START_SYMBOL_ICON_ID)
    this.create(option)
}

internal fun LineManager.createRecordingStateLine(recordingStateLatLngs: List<LatLng>): Line {
    lineCap = Property.LINE_CAP_BUTT
    val lineOptions = LineOptions()
        .withLatLngs(recordingStateLatLngs)
        .withLineJoin(Property.LINE_JOIN_ROUND)
        .withLineColor(COLOR_MAP_LINE_REGULAR)
        .withLineWidth(MAP_LINE_WIDTH)
    return this.create(lineOptions)
}

internal fun LineManager.createPausedStateLine(startEndPoints: List<LatLng>): Line {
    val lineOptions = LineOptions()
        .withLatLngs(startEndPoints)
        .withLineJoin(Property.LINE_JOIN_ROUND)
        .withLineColor(COLOR_MAP_LINE_PAUSE)
        .withLineWidth(MAP_LINE_WIDTH)
        .withLinePattern(PAUSE_LINE_PATTERN)
    return this.create(lineOptions)
}

internal fun LineManager.createStaleStateLine(startEndPoints: List<LatLng>): Line {
    val lineOptions = LineOptions()
        .withLatLngs(startEndPoints)
        .withLineJoin(Property.LINE_JOIN_ROUND)
        .withLineColor(COLOR_MAP_LINE_REGULAR)
        .withLineWidth(MAP_LINE_WIDTH)
    return this.create(lineOptions)
}