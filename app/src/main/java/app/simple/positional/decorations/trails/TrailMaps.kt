package app.simple.positional.decorations.trails

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import app.simple.positional.R
import app.simple.positional.constants.TrailIcons
import app.simple.positional.math.CompassAzimuth
import app.simple.positional.math.LowPassFilter
import app.simple.positional.math.Vector3
import app.simple.positional.model.TrailData
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.preferences.TrailPreferences
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import app.simple.positional.util.BitmapHelper
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
                                                                CoroutineScope,
                                                                SensorEventListener {

    private val accelerometerReadings = FloatArray(3)
    private val magnetometerReadings = FloatArray(3)
    private var readingsAlpha = 0.03f
    private var rotationAngle = 0f

    private var haveAccelerometerSensor = false
    private var haveMagnetometerSensor = false

    private var accelerometer = Vector3.zero
    private var magnetometer = Vector3.zero
    private var sensorManager: SensorManager
    private lateinit var sensorAccelerometer: Sensor
    private lateinit var sensorMagneticField: Sensor

    private val cameraSpeed: Int = 1000
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
    private var lastTilt = 0F
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

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        kotlin.runCatching {
            sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            haveMagnetometerSensor = true
            haveAccelerometerSensor = true
        }.getOrElse {
            haveAccelerometerSensor = false
            haveMagnetometerSensor = false
        }
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
            TrailPreferences.setMapBearing(this.googleMap?.cameraPosition!!.bearing)
            viewHandler.removeCallbacks(mapMoved)
            viewHandler.postDelayed(mapMoved, 6000)
        }

        this.googleMap?.setOnCameraMoveListener {
            viewHandler.removeCallbacks(mapMoved)
        }

        this.googleMap?.setOnMapClickListener {
            trailMapCallbacks.onMapClicked()
        }

        trailMapCallbacks.onMapInitialized()
        register()
    }

    fun pause() {
        unregister()
        onPause()
    }

    fun lowMemory() {
        onLowMemory()
    }

    fun resume() {
        onResume()
        register()
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
    }

    fun destroy() {
        onDestroy()
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
        clearAnimation()
        viewHandler.removeCallbacksAndMessages(null)
        viewHandler.removeCallbacks(mapMoved)
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
                        BitmapHelper.rotateBitmap(
                                R.drawable.ic_location_arrow.toBitmap(context, 100),
                                if (TrailPreferences.isCompassRotation()) rotationAngle else location?.bearing ?: 0F)
                    } else {
                        R.drawable.ic_place_historical.toBitmap(context, 60)
                    }
            }

            if (googleMap.isNotNull()) {
                marker?.remove()
                marker = googleMap?.addMarker(MarkerOptions()
                                                  .position(latLng)
                                                  .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap!!)))
                invalidate()
            }
        }
    }

    fun addPolylines(arrayList: ArrayList<TrailData>) {
        googleMap?.clear()
        polylines.clear()
        currentPolyline.clear()
        flagMarkers.clear()
        trailData.clear()
        options?.points?.clear()

        for (trailData in arrayList) {
            val latLng = LatLng(trailData.latitude, trailData.longitude)

            currentPolyline.add(latLng)

            val marker = googleMap?.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromBitmap(
                                TrailIcons.icons[trailData.iconPosition]
                                    .toBitmap(context, 50))))

            flagMarkers.add(marker!!)
            options?.add(latLng)
            polylines.add(googleMap?.addPolyline(options!!)!!)
        }

        invalidate()

        options?.startCap(CustomCap(BitmapDescriptorFactory.fromBitmap(R.drawable.ic_circle_stroke.toBitmap(context, 30))))
        options?.endCap(CustomCap(BitmapDescriptorFactory.fromBitmap(R.drawable.ic_circle_stroke.toBitmap(context, 30))))

        trailData.addAll(arrayList)
        trailMapCallbacks.onLineCountChanged(options!!.points.size)
    }

    fun addPolyline(trailData: TrailData) {
        val latLng = LatLng(trailData.latitude, trailData.longitude)

        currentPolyline.add(latLng)

        val marker = googleMap?.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(
                            TrailIcons.icons[trailData.iconPosition]
                                .toBitmap(context, 50))))

        this.trailData.add(trailData)
        flagMarkers.add(marker!!)
        options?.add(latLng)
        polylines.add(googleMap?.addPolyline(options!!)!!)

        trailMapCallbacks.onLineCountChanged(options!!.points.size)

        invalidate()

        if (TrailPreferences.arePolylinesWrapped()) {
            wrap()
        } else {
            moveMapCamera(latLng, TrailPreferences.getMapZoom(), cameraSpeed)
        }
    }

    fun removePolyline() {
        polylines.lastOrNull()?.remove()
        flagMarkers.lastOrNull()?.remove()
        currentPolyline.removeLastOrNull()
        options?.points?.removeLastOrNull()
        trailMapCallbacks.onLineDeleted(trailData.lastOrNull())
        trailMapCallbacks.onLineCountChanged(options!!.points.size)
        trailData.removeLastOrNull()
        invalidate()
    }

    fun wrapUnwrap() {
        if (isWrapped) {
            moveMapCamera(latLng!!, lastZoom, cameraSpeed)
        } else {
            wrap()
        }
    }

    private fun wrap() {
        kotlin.runCatching {
            lastZoom = googleMap?.cameraPosition?.zoom ?: 15F
            lastTilt = googleMap?.cameraPosition?.tilt ?: 0F

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
            moveMapCamera(LatLng(location!!.latitude, location!!.longitude), zoom, cameraSpeed)
        }
    }

    private fun setSatellite() {
        if (googleMap.isNotNull())
            googleMap?.mapType = if (TrailPreferences.isSatelliteOn()) {
                if (TrailPreferences.isLabelOn()) {
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
            if (TrailPreferences.getHighContrastMap()) {
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
                                                            .tilt(lastTilt)
                                                            .zoom(zoom)
                                                            .bearing(TrailPreferences.getMapBearing())
                                                            .build()), duration, null)
        isWrapped = false
        TrailPreferences.setWrapStatus(false)
    }

    fun getCamera(): CameraPosition = googleMap?.cameraPosition!!

    fun setCamera(cameraPosition: CameraPosition?) {
        cameraPosition ?: return
        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    private val mapMoved = object : Runnable {
        override fun run() {
            if (TrailPreferences.getMapAutoCenter() && !isWrapped) {
                val latLng = if (location.isNotNull()) {
                    LatLng(location!!.latitude, location!!.longitude)
                } else {
                    LatLng(lastLatitude, lastLongitude)
                }

                moveMapCamera(latLng, TrailPreferences.getMapZoom(), cameraSpeed)
            }
            viewHandler.postDelayed(this, 6000L)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            TrailPreferences.trailHighContrastMap, TrailPreferences.trailLabelMode -> {
                setMapStyle(TrailPreferences.isLabelOn())
            }
            TrailPreferences.trailSatellite -> {
                setSatellite()
            }
            TrailPreferences.trailShowBuilding -> {
                setBuildings(TrailPreferences.getShowBuildingsOnMap())
            }
            TrailPreferences.geodesic -> {
                options!!.geodesic(TrailPreferences.isTrailGeodesic())
                invalidate()
            }
            TrailPreferences.mapAutoCenter -> {
                viewHandler.removeCallbacks(mapMoved)
                viewHandler.post(mapMoved)
            }
            TrailPreferences.compass -> {
                if (TrailPreferences.isCompassRotation()) {
                    register()
                } else {
                    unregister()
                }
            }
        }
    }

    fun setOnTrailMapCallbackListener(trailMapCallbacks: TrailMapCallbacks) {
        this.trailMapCallbacks = trailMapCallbacks
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                LowPassFilter.smoothAndSetReadings(accelerometerReadings, event.values, readingsAlpha)
                accelerometer = Vector3(accelerometerReadings[0], accelerometerReadings[1], accelerometerReadings[2])
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                LowPassFilter.smoothAndSetReadings(magnetometerReadings, event.values, readingsAlpha)
                magnetometer = Vector3(magnetometerReadings[0], magnetometerReadings[1], magnetometerReadings[2])
            }
        }

        rotationAngle = CompassAzimuth.calculate(gravity = accelerometer, magneticField = magnetometer)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    private fun register() {
        if (haveAccelerometerSensor && haveMagnetometerSensor) {
            unregister()
            if (TrailPreferences.isCompassRotation()) {
                sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME)
                sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_GAME)
            }
        }
    }

    private fun unregister() {
        if (haveAccelerometerSensor && haveMagnetometerSensor) {
            sensorManager.unregisterListener(this, sensorAccelerometer)
            sensorManager.unregisterListener(this, sensorMagneticField)
        }
    }
}
