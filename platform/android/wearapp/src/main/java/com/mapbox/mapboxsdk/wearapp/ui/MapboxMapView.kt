package com.example.mapbox.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.location.Location
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import com.example.mapbox.R
import com.example.mapbox.breadcrumb.ExerciseEngineState
import com.example.mapbox.extensions.getBitmapFromDrawable
import com.example.mapbox.extensions.latLong
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener
import com.mapbox.mapboxsdk.location.OnLocationCameraTransitionListener
import com.mapbox.mapboxsdk.location.OnLocationStaleListener
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.*
import com.soy.android.maps.breadcrumb.Breadcrumb
import com.soy.android.maps.compass.WassCompassEngine
import com.soy.android.maps.extensions.getActivatedLocationComponent
import com.soy.android.maps.extensions.isCameraInTrackingMode
import timber.log.Timber
import kotlin.math.roundToInt

internal const val INITIAL_CAMERA_ZOOM_LEVEL_INDEX_WITH_LOCATION = 14
/**
 * Map is centered at Helsinki when we don't have user's current or any last location
 */
private const val DEFAULT_INITIAL_CAMERA_POSITION_LATITUDE = 60.1699
private const val DEFAULT_INITIAL_CAMERA_POSITION_LONGITUDE = 24.9384

private const val CAMERA_ANIMATION_DURATION = 200L

internal fun buildCameraPosition(
    target: LatLng = LatLng(DEFAULT_INITIAL_CAMERA_POSITION_LATITUDE, DEFAULT_INITIAL_CAMERA_POSITION_LONGITUDE),
    zoomIndex: Int = INITIAL_CAMERA_ZOOM_LEVEL_INDEX_WITH_LOCATION
) = CameraPosition.Builder().target(target).zoom(zoomIndex.toDouble()).build()

/**
 * [MapboxMapOptions] can either be applied via xml or programmatically when [MapView] is
 * constructed. This function provides a predefined set of options.
 */
private fun createMapboxMapOptions(resources: Resources, location: Location?): MapboxMapOptions {
    val cameraPosition = if (location == null) {
        // Use default location and zoom level
        buildCameraPosition()
    } else {
        buildCameraPosition(LatLng(location.latitude, location.longitude), INITIAL_CAMERA_ZOOM_LEVEL_INDEX_WITH_LOCATION)
    }
    return MapboxMapOptions().apply {
        val compassImageTopMargin = resources.getDimension(R.dimen.size_spacing_xxsmall).roundToInt()
        compassMargins(intArrayOf(0, compassImageTopMargin, 0, 0))
        compassEnabled(true)
        compassImage(resources.getDrawable(R.drawable.ic_shape_north_indicator, null))
        compassGravity(Gravity.CENTER)
        textureMode(true)
        attributionEnabled(false)
        logoEnabled(false)
        zoomGesturesEnabled(true)
        compassFadesWhenFacingNorth(false)
        rotateGesturesEnabled(false)
        camera(cameraPosition)
    }
}

internal const val START_SYMBOL_ICON_ID = "id-start-icon"
internal const val PAUSE_LINE_PATTERN = "id-stale-line-pattern"

/**
 * This class extends [MapView] and is suggested to use when creating map programmatically with a
 * given cameraPosition which is used to center the camera on map. This view doesn't allow touch
 * interactions for the child views of [MapView] such as compass, logo, attribute.
 */
@SuppressLint("ViewConstructor")
class MapboxMapView(
    context: Context,
    location: Location? = null,
    private val isBreadcrumbEnabled: Boolean = true
) : MapView(context, createMapboxMapOptions(context.resources, location)), OnLocationStaleListener,
    OnCameraTrackingChangedListener, MapboxMap.OnMapClickListener, OnMapReadyCallback, MapboxMap.OnMapLongClickListener {

    init {
        id = View.generateViewId()
    }

    private var map: MapboxMap? = null

    private var breadcrumb: Breadcrumb? = null

    private var exerciseEngineState = ExerciseEngineState.Recording

    /**
     * A boolean which keeps track of location stale status. Initially location is assigned to be stale since there is
     * no gps fix at start up. The value changes in two places:
     * 1- When [onStaleStateChange] is invoked, the value is updated based on the parameter
     * 2- Whenever there is new gps fix it is set to be false
     */
    private var isLocationStale = true

    internal var compassEngine: WassCompassEngine? = null
    @CameraMode.Mode
    private var lastCameraTrackingMode = CameraMode.TRACKING_COMPASS
    private var cameraZoomLevelIndex = INITIAL_CAMERA_ZOOM_LEVEL_INDEX_WITH_LOCATION
    private var isDefaultCameraPositionInUse = false

    override fun onMapReady(mapboxMap: MapboxMap) {
        map = mapboxMap.apply {
            setStyle(Style.Builder()
                .withImage(START_SYMBOL_ICON_ID, resources.getBitmapFromDrawable(R.drawable.ic_map_start_pin))
                .withImage(PAUSE_LINE_PATTERN, resources.getBitmapFromDrawable(R.drawable.ic_stale_track_dot))
                .fromUrl(context.getString(R.string.asoy_mapbox_style))
            ) { style ->
                startLocationComponent(mapboxMap, style)
                if (isBreadcrumbEnabled) {
                    breadcrumb = Breadcrumb(this@MapboxMapView, mapboxMap, style)
                }
            }
            addOnMapLongClickListener(this@MapboxMapView)
            addOnMapClickListener(this@MapboxMapView)
        }
    }

    // Location
    @SuppressLint("MissingPermission")
    private fun startLocationComponent(mapboxMap: MapboxMap, style: Style) {
        mapboxMap.locationComponent.apply {
            // Activate with options. If location engine is null, push location updates to
            // the component without any internal engine management. No engine is going to
            // be initialized and you can push location updates with
            // [LocationComponent#forceLocationUpdate(Location)].
            activateLocationComponent(
                context,
                style,
                null,
                LocationComponentOptions.createFromAttributes(context, R.style.mapbox_location)
            )
            compassEngine = this@MapboxMapView.compassEngine
            addOnCameraTrackingChangedListener(this@MapboxMapView)
            isLocationComponentEnabled = true
            Timber.d("Mapbox Location Component is enabled")
            addOnLocationStaleListener(this@MapboxMapView)
            renderMode = RenderMode.GPS
            cameraMode = lastCameraTrackingMode
        }
    }

    internal fun onNewLocation(location: Location) {
        isLocationStale = false
        map?.run {
            getActivatedLocationComponent()?.run {
                if (isDefaultCameraPositionInUse) {
                    updateMapCameraZoom(INITIAL_CAMERA_ZOOM_LEVEL_INDEX_WITH_LOCATION)
                    isDefaultCameraPositionInUse = false
                }
                forceLocationUpdate(location)
                breadcrumb?.onNewLocation(location.latLong(), exerciseEngineState)
            }
        }
    }

    override fun onStaleStateChange(isStale: Boolean) {
        Timber.d("Location stale state change. Is Stale: $isStale")
        isLocationStale = isStale
        map?.getActivatedLocationComponent()?.run {
            renderMode = if (isStale) RenderMode.NORMAL else RenderMode.GPS
            val locationComponentStyle = if (isStale) R.style.mapbox_stale_location else R.style.mapbox_location
            applyStyle(LocationComponentOptions.createFromAttributes(context, locationComponentStyle))
        }
    }

    // Camera
    private fun updateMapCameraZoom(zoomLevelIndex: Int) = map?.getActivatedLocationComponent()?.run {
        cameraZoomLevelIndex = zoomLevelIndex
        if (cameraMode != CameraMode.NONE) {
            zoomWhileTracking(getCameraZoomLevel(), CAMERA_ANIMATION_DURATION)
        } else {
            map?.easeCamera(CameraUpdateFactory.zoomTo(getCameraZoomLevel()))
        }
    }

    private fun getCameraZoomLevel(): Double = map?.cameraPosition?.zoom ?: 0.0

    override fun onCameraTrackingChanged(currentMode: Int) {
        Timber.d("Camera tracking mode changed. Current mode: $currentMode")
        if (map?.isCameraInTrackingMode() == true) {
            lastCameraTrackingMode = currentMode
        }
    }

    /**
     * Invoked whenever camera tracking is broken.
     */
    override fun onCameraTrackingDismissed() {
        Timber.d("Exiting from camera tracking mode")
        updateCameraMode(CameraMode.NONE_COMPASS)
    }

    private fun updateCameraMode(@CameraMode.Mode mode: Int) {
        // We need to
        map?.getActivatedLocationComponent()?.apply {
            if (cameraMode != CameraMode.NONE) {
                setCameraMode(mode, object : OnLocationCameraTransitionListener {
                    override fun onLocationCameraTransitionFinished(cameraMode: Int) {
                        zoomWhileTracking(getCameraZoomLevel(), CAMERA_ANIMATION_DURATION)
                    }

                    override fun onLocationCameraTransitionCanceled(cameraMode: Int) {
                        // No impl.
                    }
                })
            } else {
                cameraMode = mode
            }
        }
    }

    // Gestures
    /**
     * When [MapboxMap.OnMapClickListener.onMapClick] is called camera mode is updated:
     * There are two camera tracking modes enabled:
     * The [CameraMode.TRACKING_GPS_NORTH] sets the camera always to the north and the [CameraMode.TRACKING_COMPASS]
     * sets the location indicator to north and map rotates according to heading
     * If camera mode is [CameraMode.TRACKING_GPS_NORTH], onClick switches the mode to [CameraMode.TRACKING_COMPASS]
     * If camera mode is [CameraMode.TRACKING_COMPASS]  onClick switches the mode to [CameraMode.TRACKING_GPS_NORTH]
     * If user starts panning while in tracking mode, camera switches to [CameraMode.NONE_COMPASS] mode and user is not
     * tracked by camera. On click switches the camera back to last used tracking mode
     */
    override fun onMapClick(point: LatLng): Boolean {
        map?.getActivatedLocationComponent()?.apply {
            when (cameraMode) {
                CameraMode.TRACKING_GPS_NORTH -> updateCameraMode(CameraMode.TRACKING_COMPASS)
                CameraMode.TRACKING_COMPASS -> updateCameraMode(CameraMode.TRACKING_GPS_NORTH)
                CameraMode.NONE_COMPASS,
                CameraMode.NONE,
                CameraMode.NONE_GPS -> updateCameraMode(lastCameraTrackingMode)
                else -> Timber.w("Using unsupported camera mode")
            }
        }
        return true
    }

    /**
     * Used for mocking exercise pause / resmume controls
     */
    override fun onMapLongClick(point: LatLng): Boolean {
        if (exerciseEngineState == ExerciseEngineState.Paused) {
            exerciseEngineState = ExerciseEngineState.Recording
        } else if (exerciseEngineState == ExerciseEngineState.Recording) {
            exerciseEngineState = ExerciseEngineState.Paused
        }
        return true
    }

    /**
     * By overriding this method, we prevent compass view component of the [MapView] from consuming the clicks
     */
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        super.onInterceptTouchEvent(ev)
        return true
    }

    override fun onResume() {
        super.onResume()
        breadcrumb?.onResume()
    }

    override fun onStop() {
        breadcrumb?.onStop()
        super.onStop()
    }

    override fun onDestroy() {
        breadcrumb?.onDestroy()
        super.onDestroy()
    }
}
