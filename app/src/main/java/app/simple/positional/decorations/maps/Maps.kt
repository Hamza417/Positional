package app.simple.positional.decorations.maps

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import app.simple.positional.R
import app.simple.positional.constants.LocationPins
import app.simple.positional.preferences.GPSPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import app.simple.positional.util.BitmapHelper.toBitmap
import app.simple.positional.util.NullSafety.isNotNull
import app.simple.positional.util.NullSafety.isNull
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class Maps(context: Context, attributeSet: AttributeSet) : MapView(context, attributeSet),
                                                           OnMapReadyCallback, SharedPreferences.OnSharedPreferenceChangeListener,
                                                           CoroutineScope {
    private var googleMap: GoogleMap? = null
    var location: Location? = null
    private var latLng: LatLng? = null
    private var mapsCallbacks: MapsCallbacks? = null
    private var marker: Bitmap? = null
    private val viewHandler = Handler(Looper.getMainLooper())

    private var isCustomCoordinate = false
    private var isBearingRotation = false
    private var customLatitude = 0.0
    private var customLongitude = 0.0
    val lastLatitude = GPSPreferences.getLastCoordinates()[0].toDouble()
    val lastLongitude = GPSPreferences.getLastCoordinates()[1].toDouble()

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    init {
        isCustomCoordinate = MainPreferences.isCustomCoordinate()
        isBearingRotation = GPSPreferences.isBearingRotationOn()

        if (isCustomCoordinate) {
            customLatitude = MainPreferences.getCoordinates()[0].toDouble()
            customLongitude = MainPreferences.getCoordinates()[1].toDouble()
        }

        if (GPSPreferences.isUsingVolumeKeys()) {
            this.isFocusableInTouchMode = true
            this.requestFocus()
        }

        viewHandler.postDelayed({
            /**
             * This prevents the lag when fragment is switched
             */
            this.alpha = 0F
            getMapAsync(this)
        }, 500)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        /**
         * Workaround for flashing of view when map is
         * Initialized
         */
        this.animate().alpha(1F).setDuration(500).start()

        latLng = if (isCustomCoordinate)
            LatLng(customLatitude, customLongitude)
        else
            LatLng(GPSPreferences.getLastCoordinates()[0].toDouble(), GPSPreferences.getLastCoordinates()[1].toDouble())

        googleMap.uiSettings.isCompassEnabled = false
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.uiSettings.isMyLocationButtonEnabled = false

        this.googleMap = googleMap

        addMarker(latLng!!)
        setMapStyle(GPSPreferences.isLabelOn())
        setSatellite()
        setBuildings(GPSPreferences.getShowBuildingsOnMap())

        this.googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(
                latLng!!,
                GPSPreferences.getMapZoom(),
                GPSPreferences.getMapTilt(),
                0F)))

        this.googleMap?.setOnCameraMoveListener {
            viewHandler.removeCallbacks(mapMoved)
        }

        this.googleMap?.setOnCameraIdleListener {
            GPSPreferences.setMapZoom(this.googleMap?.cameraPosition!!.zoom)
            GPSPreferences.setMapTilt(this.googleMap?.cameraPosition!!.tilt)
            viewHandler.removeCallbacks(mapMoved)
            viewHandler.postDelayed(mapMoved, 6000)
        }

        this.googleMap?.setOnMapClickListener {
            mapsCallbacks?.onMapClicked(this)
        }
    }

    fun pause() {
        onPause()
    }

    fun lowMemory() {
        onLowMemory()
    }

    fun resume() {
        onResume()
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
    }

    fun destroy() {
        onDestroy()
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
        clearAnimation()
        viewHandler.removeCallbacks(mapMoved)
        job.cancel()
    }

    fun resetCamera(zoom: Float) {
        if (isCustomCoordinate) {
            addMarker(LatLng(customLatitude, customLongitude))
            moveMapCamera(LatLng(customLatitude, customLongitude), zoom, 0F)
        } else
            if (location != null) {
                moveMapCamera(LatLng(location!!.latitude, location!!.longitude), zoom, location!!.bearing)
                addMarker(LatLng(location!!.latitude, location!!.longitude))
                viewHandler.removeCallbacks(mapMoved)
            }
    }

    private fun setSatellite() {
        if (googleMap.isNotNull())
            googleMap?.mapType = if (GPSPreferences.isSatelliteOn()) {
                if (GPSPreferences.isLabelOn()) {
                    GoogleMap.MAP_TYPE_HYBRID
                } else {
                    GoogleMap.MAP_TYPE_SATELLITE
                }
            } else {
                GoogleMap.MAP_TYPE_NORMAL
            }
    }

    private fun setBuildings(value: Boolean) {
        googleMap!!.isBuildingsEnabled = value
    }

    private fun setMapStyle(value: Boolean) {

        setSatellite()

        if (!googleMap.isNull()) {
            if (GPSPreferences.getHighContrastMap()) {
                googleMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                        context,
                        if (value) {
                            R.raw.map_high_contrast_labelled
                        } else {
                            R.raw.map_high_contrast_non_labelled
                        }
                ))

            } else {
                googleMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                        context,
                        when (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                            Configuration.UI_MODE_NIGHT_YES -> {
                                if (value) {
                                    R.raw.maps_dark_labelled
                                } else {
                                    R.raw.maps_dark_no_label
                                }
                            }
                            Configuration.UI_MODE_NIGHT_NO -> {
                                if (value) {
                                    R.raw.maps_light_labelled
                                } else {
                                    R.raw.maps_no_label
                                }
                            }
                            else -> 0
                        }
                ))
            }
        }
    }

    fun zoomIn() {
        googleMap?.animateCamera(CameraUpdateFactory.zoomIn())
    }

    fun zoomOut() {
        googleMap?.animateCamera(CameraUpdateFactory.zoomOut())
    }

    fun addMarker(latLng: LatLng) {
        launch {
            withContext(Dispatchers.Default) {

                if (context.isNotNull())
                    marker = if (isCustomCoordinate) {
                        R.drawable.ic_place_custom.toBitmap(context, GPSPreferences.getPinSize(), GPSPreferences.getPinOpacity())
                    } else {
                        if (location.isNotNull()) {
                            LocationPins.locationsPins[GPSPreferences.getPinSkin()].toBitmap(context, GPSPreferences.getPinSize(), GPSPreferences.getPinOpacity())
                        } else {
                            R.drawable.ic_place_historical.toBitmap(context, GPSPreferences.getPinSize(), GPSPreferences.getPinOpacity())
                        }
                    }
            }

            if (googleMap.isNotNull()) {
                googleMap?.clear()
                googleMap?.addMarker(MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(marker!!)))
                invalidate()
            }
        }
    }

    private val mapMoved = object : Runnable {
        override fun run() {
            if (GPSPreferences.getMapAutoCenter()) {
                val bearing: Float
                val latLng = if (isCustomCoordinate) {
                    bearing = 0F
                    LatLng(customLatitude, customLongitude)
                } else {
                    if (location.isNotNull()) {
                        bearing = location!!.bearing
                        LatLng(location!!.latitude, location!!.longitude)
                    } else {
                        bearing = 0F
                        LatLng(lastLatitude, lastLongitude)
                    }
                }

                moveMapCamera(latLng, GPSPreferences.getMapZoom(), bearing)
            }
            viewHandler.postDelayed(this, 6000L)
        }
    }

    private fun moveMapCamera(latLng: LatLng, zoom: Float, bearing: Float) {
        if (googleMap.isNull()) return

        googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder()
                .target(latLng)
                .tilt(GPSPreferences.getMapTilt())
                .zoom(zoom)
                .bearing(if (isBearingRotation) bearing else 0F).build()), 3000, null)
    }

    fun setOnMapsCallbackListener(mapsCallbacks: MapsCallbacks) {
        this.mapsCallbacks = mapsCallbacks
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            GPSPreferences.highContrastMap, GPSPreferences.GPSLabelMode -> {
                setMapStyle(GPSPreferences.isLabelOn())
            }
            GPSPreferences.GPSSatellite -> {
                setSatellite()
            }
            GPSPreferences.showBuilding -> {
                setBuildings(GPSPreferences.getShowBuildingsOnMap())
            }
            GPSPreferences.mapAutoCenter -> {
                viewHandler.removeCallbacks(mapMoved)
                viewHandler.post(mapMoved)
            }
            GPSPreferences.useBearingRotation -> {
                isBearingRotation = GPSPreferences.isBearingRotationOn()
            }
            GPSPreferences.pinSize,
            GPSPreferences.pinOpacity -> {
                if (latLng.isNotNull()) {
                    addMarker(latLng!!)
                }
            }
        }
    }
}
