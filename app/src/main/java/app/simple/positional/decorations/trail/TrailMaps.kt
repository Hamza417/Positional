package app.simple.positional.decorations.trail

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
import app.simple.positional.decorations.maps.MapsCallbacks
import app.simple.positional.preferences.GPSPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import app.simple.positional.util.BitmapHelper.toBitmap
import app.simple.positional.util.ColorUtils.resolveAttrColor
import app.simple.positional.util.NullSafety.isNotNull
import app.simple.positional.util.NullSafety.isNull
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

class TrailMaps(context: Context, attributeSet: AttributeSet) : MapView(context, attributeSet),
                                                                OnMapReadyCallback, SharedPreferences.OnSharedPreferenceChangeListener,
                                                                CoroutineScope {

    private var googleMap: GoogleMap? = null
    var location: Location? = null
    private var latLng: LatLng? = null
    private var mapsCallbacks: MapsCallbacks? = null
    private var markerBitmap: Bitmap? = null
    private val viewHandler = Handler(Looper.getMainLooper())
    val lastLatitude = MainPreferences.getLastCoordinates()[0].toDouble()
    val lastLongitude = MainPreferences.getLastCoordinates()[1].toDouble()
    private var marker: Marker? = null
    var onMapClicked: () -> Unit = {}

    private var options: PolylineOptions? = null

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    init {
        if (GPSPreferences.isUsingVolumeKeys()) {
            this.isFocusableInTouchMode = true
            this.requestFocus()
        }

        options = PolylineOptions()
            .width(10f)
            .jointType(JointType.ROUND)
            .startCap(RoundCap())
            .endCap(RoundCap())
            .color(context.resolveAttrColor(R.attr.colorAppAccent))
            .geodesic(true)

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
        latLng = LatLng(MainPreferences.getLastCoordinates()[0].toDouble(), MainPreferences.getLastCoordinates()[1].toDouble())

        googleMap.uiSettings.isCompassEnabled = false
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.uiSettings.isMyLocationButtonEnabled = false

        this.googleMap = googleMap
        addMarker(latLng!!)
        setMapStyle(GPSPreferences.isLabelOn())
        setSatellite()
        setBuildings(GPSPreferences.getShowBuildingsOnMap())
        val list = arrayListOf(
                LatLng(55.0, 100.0),
                LatLng(55.0, 95.0),
                LatLng(50.0, 90.0)
        )

        addPolyline(list)

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
            onMapClicked.invoke()
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

    fun addMarker(latLng: LatLng) {
        launch {
            withContext(Dispatchers.Default) {
                if (context.isNotNull())
                    markerBitmap = if (location.isNotNull()) {
                        LocationPins.locationsPins[GPSPreferences.getPinSkin()].toBitmap(context, 100)
                    } else {
                        R.drawable.ic_place_historical.toBitmap(context, 100)
                    }
            }

            if (googleMap.isNotNull()) {
                marker?.remove()
                marker = googleMap?.addMarker(MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(markerBitmap!!)))
                invalidate()
            }
        }
    }

    fun addPolyline(coordinates: ArrayList<LatLng>) {
        for (latLng in coordinates) {
            options?.add(latLng)
        }

        googleMap?.addPolyline(options!!)
    }

    fun addPolyline(latLng: LatLng) {
        options?.add(latLng)
        googleMap?.addPolyline(options!!)
    }

    fun resetCamera(zoom: Float) {
        if (location != null) {
            moveMapCamera(LatLng(location!!.latitude, location!!.longitude), zoom)
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

    private val mapMoved = object : Runnable {
        override fun run() {
            if (GPSPreferences.getMapAutoCenter()) {
                val latLng = if (location.isNotNull()) {
                    LatLng(location!!.latitude, location!!.longitude)
                } else {
                    LatLng(lastLatitude, lastLongitude)
                }

                moveMapCamera(latLng, GPSPreferences.getMapZoom())
            }
            viewHandler.postDelayed(this, 6000L)
        }
    }

    private fun moveMapCamera(latLng: LatLng, zoom: Float) {
        if (googleMap.isNull()) return

        googleMap?.animateCamera(CameraUpdateFactory
                                     .newCameraPosition(CameraPosition.builder()
                                                            .target(latLng)
                                                            .tilt(GPSPreferences.getMapTilt())
                                                            .zoom(zoom)
                                                            .bearing(0F).build()), 3000, null)
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
        }
    }
}
