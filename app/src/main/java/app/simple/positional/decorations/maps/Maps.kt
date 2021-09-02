package app.simple.positional.decorations.maps

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
import app.simple.positional.constants.LocationPins
import app.simple.positional.math.CompassAzimuth
import app.simple.positional.math.LowPassFilter
import app.simple.positional.math.Vector3
import app.simple.positional.preferences.GPSPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import app.simple.positional.util.BitmapHelper.toBitmap
import app.simple.positional.util.ConditionUtils.isNotNull
import app.simple.positional.util.ConditionUtils.isNull
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class Maps(context: Context, attributeSet: AttributeSet) : MapView(context, attributeSet),
        OnMapReadyCallback,
        SharedPreferences.OnSharedPreferenceChangeListener,
        SensorEventListener,
        CoroutineScope {

    private val accelerometerReadings = FloatArray(3)
    private val magnetometerReadings = FloatArray(3)
    private var readingsAlpha = 0.03f
    private var rotationAngle = 0f
    private var accuracy = -1

    private var haveAccelerometerSensor = false
    private var haveMagnetometerSensor = false

    private var accelerometer = Vector3.zero
    private var magnetometer = Vector3.zero
    private var sensorManager: SensorManager
    private lateinit var sensorAccelerometer: Sensor
    private lateinit var sensorMagneticField: Sensor

    private val cameraSpeed = 1000
    private var googleMap: GoogleMap? = null
    var location: Location? = null
    private var latLng: LatLng? = null
    private var mapsCallbacks: MapsCallbacks? = null
    private var marker: Bitmap? = null
    private val viewHandler = Handler(Looper.getMainLooper())

    private var isCustomCoordinate = false
    private var isBearingRotation = false
    private var isFirstLocation = true
    private var isCompassRotation = false

    private var customLatitude = 0.0
    private var customLongitude = 0.0

    val lastLatitude = MainPreferences.getLastCoordinates()[0].toDouble()
    val lastLongitude = MainPreferences.getLastCoordinates()[1].toDouble()

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    init {
        isCustomCoordinate = MainPreferences.isCustomCoordinate()
        isBearingRotation = GPSPreferences.isBearingRotation()
        isCompassRotation = GPSPreferences.isCompassRotation()

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

        latLng = if (isCustomCoordinate)
            LatLng(customLatitude, customLongitude)
        else
            LatLng(MainPreferences.getLastCoordinates()[0].toDouble(), MainPreferences.getLastCoordinates()[1].toDouble())

        googleMap.uiSettings.isCompassEnabled = false
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.uiSettings.isMyLocationButtonEnabled = false

        this.googleMap = googleMap

        addMarker(latLng!!)

        if (!isCustomCoordinate) {
            if (location.isNotNull()) {
                setFirstLocation(location!!)
            }
        }

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

        mapsCallbacks?.onMapInitialized()
        register()
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
        register()
    }

    fun destroy() {
        onDestroy()
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
        clearAnimation()
        viewHandler.removeCallbacksAndMessages(null)
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
                isCompassRotation = GPSPreferences.isCompassRotation()
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
                .bearing(if (isBearingRotation) bearing else 0F)
                .build()), cameraSpeed, null)
    }

    fun getCamera(): CameraPosition? {
        return googleMap?.cameraPosition
    }

    fun setCamera(cameraPosition: CameraPosition?) {
        cameraPosition ?: return
        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    fun setFirstLocation(location: Location?) {
        if (googleMap.isNotNull() && isFirstLocation) {
            this.location = location

            with(LatLng(location!!.latitude, location.longitude)) {
                addMarker(this)
                googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(
                        this,
                        GPSPreferences.getMapZoom(),
                        GPSPreferences.getMapTilt(),
                        0F)))

                latLng = this
                isFirstLocation = false
            }
        }
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

        if (isCompassRotation) {
            if (googleMap.isNotNull())
                with(googleMap!!) {
                    moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(
                            cameraPosition.target,
                            cameraPosition.zoom,
                            cameraPosition.tilt,
                            rotationAngle
                    )))
                }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        /* no-op */
    }

    private fun register() {
        if (haveAccelerometerSensor && haveMagnetometerSensor) {
            unregister()
            if (GPSPreferences.isCompassRotation()) {
                sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME)
                sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_GAME)

                if(googleMap.isNotNull()) {
                    with(googleMap!!.uiSettings) {
                        isScrollGesturesEnabled = false
                        isZoomGesturesEnabled = false
                        isRotateGesturesEnabled = false
                    }
                }
            }
        }
    }

    private fun unregister() {
        if (haveAccelerometerSensor && haveMagnetometerSensor) {
            sensorManager.unregisterListener(this, sensorAccelerometer)
            sensorManager.unregisterListener(this, sensorMagneticField)

            if(googleMap.isNotNull()) {
                with(googleMap!!.uiSettings) {
                    isScrollGesturesEnabled = true
                    isZoomGesturesEnabled = true
                    isRotateGesturesEnabled = true
                }
            }
        }
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
            GPSPreferences.pinSize,
            GPSPreferences.pinOpacity -> {
                if (latLng.isNotNull()) {
                    addMarker(latLng!!)
                }
            }
            GPSPreferences.compassRotation -> {
                if (GPSPreferences.isCompassRotation()) {
                    isCompassRotation = true
                    register()
                } else {
                    isCompassRotation = false
                    unregister()
                }
            }
            GPSPreferences.useBearingRotation -> {
                isBearingRotation = GPSPreferences.isBearingRotation()

                if (googleMap.isNotNull() && isBearingRotation) {
                    with(googleMap!!) {
                        moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(
                                cameraPosition.target,
                                cameraPosition.zoom,
                                cameraPosition.tilt,
                                if (location.isNotNull()) location!!.bearing else 0F
                        )))
                    }
                }
            }
            GPSPreferences.isNorthOnly -> {
                if (googleMap.isNotNull() && GPSPreferences.isNorthOnly()) {
                    with(googleMap!!) {
                        moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(
                                cameraPosition.target,
                                cameraPosition.zoom,
                                cameraPosition.tilt,
                                0F
                        )))
                    }
                }
            }
        }
    }
}
