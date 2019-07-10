package com.soy.android.maps.breadcrumb

import android.content.res.Resources
import com.mapbox.mapboxsdk.wearapp.breadcrumb.ExerciseEngineState
import com.mapbox.mapboxsdk.wearapp.ui.MapboxMapView
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.OnLocationStaleListener
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.Line
import com.mapbox.mapboxsdk.plugins.annotation.LineManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import timber.log.Timber

/**
 * Note that this is not public API and taken from [com.mapbox.mapboxsdk.location.LocationComponentConstants]. Needed
 * to be hardcoded to have the line layer below the location indicator
 */
private const val LOCATION_INDICATOR_BACKGROUND_LAYER_ID = "mapbox-location-stroke-layer"

class Breadcrumb(
    mapView: MapboxMapView,
    private val map: MapboxMap,
    style: Style
) : OnLocationStaleListener {

    private val lineManager: LineManager = LineManager(mapView, map, style, LOCATION_INDICATOR_BACKGROUND_LAYER_ID)
    private val symbolManager: SymbolManager = SymbolManager(mapView, map, style, LOCATION_INDICATOR_BACKGROUND_LAYER_ID)
    private val resources: Resources

    init {
        map.locationComponent.addOnLocationStaleListener(this)
        resources = mapView.resources
    }

    /**
     * Flag that represents whether start symbol is created
     */
    private var startSymbolCreated = false

    /**
     * Holds [LatLng] list of current recording state line. A recording state line is the line which holds the list of
     * [LatLng] recorded during [Recording] state. When state is [Paused] last recording state line does not receive
     * updates and when the state is [Recording] after [Paused] state a new line is created and that line is updated
     * according to [currentRecordingStateLatLngs]
     */
    private var currentRecordingStateLatLngs = mutableListOf<LatLng>()

    /**
     * A recording state line is the line which is created and updated during [Recording] state.
     *
     * When state is [Paused] last recording state line does not receive updates.
     *
     * [currentRecordingStateLine] is the line which is created either at the beginning of the exercise or when the
     * state is [Recording] again after last [Paused] state.
     */
    private var currentRecordingStateLine: Line? = null

    /**
     * The [ExerciseEngineState] which belongs to the previous [com.soy.android.maps.livedata.StatefulLocation]
     */
    private var previousExerciseEngineState: ExerciseEngineState? = null

    private var fromStaleState = false

    private var isMapViewVisible = true

    internal fun onNewLocation(latLng: LatLng, exerciseEngineState: ExerciseEngineState?) {
        if (!startSymbolCreated) {
            // We create a start symbol when the exercise is started
            symbolManager.addStartExerciseSymbol(latLng)
            startSymbolCreated = true
            previousExerciseEngineState = exerciseEngineState
            return
        }
        when {
            exerciseRecording(exerciseEngineState) -> {
                if (fromStaleState) {
                    fromStaleState = false
                    map.locationComponent.addOnLocationStaleListener(this)
                    lineManager.createStaleStateLine(listOf(currentRecordingStateLatLngs.last(), latLng))
                    // clear the recording state latlng list and add the start point for the new line
                    currentRecordingStateLatLngs.clear()
                    currentRecordingStateLatLngs.add(latLng)
                } else {
                    currentRecordingStateLatLngs.add(latLng)
                    createOrUpdateRecordingLine()
                }
            }
            exercisePaused(exerciseEngineState) -> {
                // add the end point for currentRecordingStateLine
                currentRecordingStateLatLngs.add(latLng)
                createOrUpdateRecordingLine()
                // add the start point of the pause line
            }
            exerciseResumed(exerciseEngineState) -> {
                // add the end point of the pause line
                // create the pause line
                lineManager.createPausedStateLine(listOf(currentRecordingStateLatLngs.last(), latLng))
                Timber.d("Pause state line is created")
                // after we create the pause line we clear pauseLinePoints for next possible pause line
                // clear the recording state latlng list and add the start point for the new line
                currentRecordingStateLatLngs.clear()
                currentRecordingStateLatLngs.add(latLng)
                createOrUpdateRecordingLine()
            }
        }
        previousExerciseEngineState = exerciseEngineState
    }

    private fun createOrUpdateRecordingLine() {
        currentRecordingStateLine?.let {
            // we update the latlng list for currentRecordingStateLine
            it.latLngs = currentRecordingStateLatLngs
            // We update the currentRecordingStateLine only when the mapview is visible
            if (isMapViewVisible) {
                lineManager.update(it)
                Timber.d("Recording state line is updated")
            }
        } ?: run {
            if (currentRecordingStateLatLngs.size > 1) {
                Timber.d("Recording state line is created")
                currentRecordingStateLine = lineManager.createRecordingStateLine(currentRecordingStateLatLngs)
            }
        }
    }

    private fun exerciseResumed(exerciseEngineState: ExerciseEngineState?) =
        previousExerciseEngineState == ExerciseEngineState.Paused && exerciseEngineState == ExerciseEngineState.Recording

    private fun exercisePaused(exerciseEngineState: ExerciseEngineState?) =
        previousExerciseEngineState == ExerciseEngineState.Recording && exerciseEngineState == ExerciseEngineState.Paused

    private fun exerciseRecording(exerciseEngineState: ExerciseEngineState?) =
        previousExerciseEngineState == ExerciseEngineState.Recording && exerciseEngineState == ExerciseEngineState.Recording

    internal fun onDestroy() {
        lineManager.onDestroy()
        symbolManager.onDestroy()
    }

    internal fun onResume() {
        isMapViewVisible = true
        createOrUpdateRecordingLine()
    }

    internal fun onStop() {
        isMapViewVisible = false
    }

    override fun onStaleStateChange(isStale: Boolean) {
        if (isStale) {
            map.locationComponent.removeOnLocationStaleListener(this)
            fromStaleState = isStale
        }
    }
}