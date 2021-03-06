package app.simple.positional.decorations.trails

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import app.simple.positional.R
import app.simple.positional.constants.TrailIcons
import app.simple.positional.model.TrailData
import app.simple.positional.preferences.GPSPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.preferences.TrailPreferences
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import app.simple.positional.util.BitmapHelper.toBitmap
import app.simple.positional.util.ColorUtils.resolveAttrColor
import app.simple.positional.util.ConditionUtils.isNotNull
import app.simple.positional.util.ConditionUtils.isNull
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class TrailMaps(context: Context, attributeSet: AttributeSet) : MapView(context, attributeSet),
                                                                OnMapReadyCallback,
                                                                SharedPreferences.OnSharedPreferenceChangeListener,
                                                                CoroutineScope {

    private var googleMap: GoogleMap? = null
    private var latLng: LatLng? = null
    private var markerBitmap: Bitmap? = null
    private val viewHandler = Handler(Looper.getMainLooper())
    private var marker: Marker? = null
    private val currentPolyline = arrayListOf<LatLng>()
    private val flagMarkers = arrayListOf<Marker>()
    private val polylines = arrayListOf<Polyline>()
    private var trailData = arrayListOf<TrailData>()
    private var isWrapped = false
    private var lastZoom = 20F
    private var options: PolylineOptions? = null

    var location: Location? = null
    val lastLatitude = MainPreferences.getLastCoordinates()[0].toDouble()
    val lastLongitude = MainPreferences.getLastCoordinates()[1].toDouble()

    private lateinit var trailMapCallbacks: TrailMapCallbacks

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    init {
        options = PolylineOptions()
            .width(10f)
            .jointType(JointType.ROUND)
            .startCap(RoundCap())
            .endCap(RoundCap())
            .jointType(JointType.ROUND)
            .color(context.resolveAttrColor(R.attr.colorAppAccent))
            .geodesic(TrailPreferences.isTrailGeodesic())

        latLng = LatLng(MainPreferences.getLastCoordinates()[0].toDouble(),
                        MainPreferences.getLastCoordinates()[1].toDouble())

        if (TrailPreferences.arePolylinesWrapped()) {
            wrap()
        } else {
            moveMapCamera(latLng!!, TrailPreferences.getMapZoom(), 3000)
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
        latLng = LatLng(MainPreferences.getLastCoordinates()[0].toDouble(), MainPreferences.getLastCoordinates()[1].toDouble())

        googleMap.uiSettings.isCompassEnabled = false
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.uiSettings.isMyLocationButtonEnabled = false

        this.googleMap = googleMap
        addMarker(latLng!!)
        setMapStyle(TrailPreferences.isLabelOn())
        setSatellite()
        setBuildings(TrailPreferences.getShowBuildingsOnMap())

        this.googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(
                latLng!!,
                TrailPreferences.getMapZoom(),
                TrailPreferences.getMapTilt(),
                0F)))

        this.googleMap?.setOnCameraIdleListener {
            TrailPreferences.setMapZoom(this.googleMap?.cameraPosition!!.zoom)
            TrailPreferences.setMapTilt(this.googleMap?.cameraPosition!!.tilt)
        }

        this.googleMap?.setOnMapClickListener {
            trailMapCallbacks.onMapClicked()
        }

        trailMapCallbacks.onMapInitialized()
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
        job.cancel()
    }

    fun clear() {
        polylines.clear()
        flagMarkers.clear()
        options!!.points.clear()
        googleMap?.clear()
        invalidate()
    }

    fun addMarker(latLng: LatLng) {
        launch {
            withContext(Dispatchers.Default) {
                if (context.isNotNull())
                    markerBitmap = if (location.isNotNull()) {
                        R.drawable.ic_pin_01.toBitmap(context, 60)
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

    fun addPolylines(arrayList: ArrayList<TrailData>) {
        googleMap?.clear()
        polylines.clear()
        currentPolyline.clear()
        flagMarkers.clear()
        options?.points?.clear()

        for (trailData in arrayList) {
            val latLng = LatLng(trailData.latitude, trailData.longitude)

            currentPolyline.add(latLng)

            val marker = googleMap?.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromBitmap(TrailIcons.icons[trailData.iconPosition].toBitmap(context, 50))))

            flagMarkers.add(marker!!)
            options?.add(latLng)
            polylines.add(googleMap?.addPolyline(options!!)!!)
        }

        invalidate()

        trailData.addAll(arrayList)
        trailMapCallbacks.onLineCountChanged(options!!.points.size)
    }

    fun addPolyline(trailData: TrailData) {
        val latLng = LatLng(trailData.latitude, trailData.longitude)

        currentPolyline.add(latLng)

        val marker = googleMap?.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(TrailIcons.icons[trailData.iconPosition].toBitmap(context, 50))))

        flagMarkers.add(marker!!)
        options?.add(latLng)
        polylines.add(googleMap?.addPolyline(options!!)!!)

        trailMapCallbacks.onLineCountChanged(options!!.points.size)

        invalidate()
    }

    fun removePolyline() {
        polylines.lastOrNull()?.remove()
        flagMarkers.lastOrNull()?.remove()
        currentPolyline.removeLastOrNull()
        polylines.removeLastOrNull()
        flagMarkers.removeLastOrNull()
        options?.points?.removeLastOrNull()
        trailMapCallbacks.onLineDeleted(trailData.lastOrNull())
        trailMapCallbacks.onLineCountChanged(options!!.points.size)
        trailData.removeLastOrNull()
        invalidate()
    }

    fun wrapUnwrap() {
        if (isWrapped) {
            moveMapCamera(latLng!!, lastZoom, 500)
        } else {
            wrap()
        }
    }

    private fun wrap() {
        kotlin.runCatching {
            lastZoom = googleMap?.cameraPosition?.zoom ?: 15F

            val builder = LatLngBounds.Builder()
            for (latLng in currentPolyline) {
                builder.include(latLng)
            }

            val bounds = builder.build()

            //BOUND_PADDING is an int to specify padding of bound.. try 100.
            googleMap!!.animateCamera(CameraUpdateFactory
                                          .newLatLngBounds(bounds, 250))

            TrailPreferences.setWrapStatus(true)
            isWrapped = true
        }.onFailure {
            isWrapped = false
            TrailPreferences.setWrapStatus(false)
        }
    }

    fun resetCamera(zoom: Float) {
        if (location != null) {
            moveMapCamera(LatLng(location!!.latitude, location!!.longitude), zoom, 500)
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

    fun moveMapCamera(latLng: LatLng, zoom: Float, duration: Int) {
        if (googleMap.isNull() && latLng.isNull()) return

        googleMap?.animateCamera(CameraUpdateFactory
                                     .newCameraPosition(CameraPosition.builder()
                                                            .target(latLng)
                                                            .tilt(GPSPreferences.getMapTilt())
                                                            .zoom(zoom)
                                                            .bearing(0F).build()), duration, null)
        isWrapped = false
        TrailPreferences.setWrapStatus(false)
    }

    fun getCamera(): CameraPosition = googleMap?.cameraPosition!!

    fun setCamera(cameraPosition: CameraPosition?) {
        cameraPosition ?: return
        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            TrailPreferences.trailHighContrastMap, TrailPreferences.trailLabelMode -> {
                setMapStyle(GPSPreferences.isLabelOn())
            }
            TrailPreferences.trailSatellite -> {
                setSatellite()
            }
            TrailPreferences.trailShowBuilding -> {
                setBuildings(GPSPreferences.getShowBuildingsOnMap())
            }
            TrailPreferences.geodesic -> {
                options!!.geodesic(TrailPreferences.isTrailGeodesic())
                invalidate()
            }
        }
    }

    fun setOnTrailMapCallbackListener(trailMapCallbacks: TrailMapCallbacks) {
        this.trailMapCallbacks = trailMapCallbacks
    }
}
