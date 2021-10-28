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
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import app.simple.positional.R
import app.simple.positional.constants.LocationPins
import app.simple.positional.math.CompassAzimuth
import app.simple.positional.math.LowPassFilter
import app.simple.positional.math.Vector3
import app.simple.positional.preferences.GPSPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import app.simple.positional.util.BitmapHelper.toBitmapKeepingSize
import app.simple.positional.util.ConditionUtils.isNotNull
import app.simple.positional.util.ConditionUtils.isNull
import app.simple.positional.util.LocationExtension
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

    private var haveAccelerometerSensor = false
    private var haveMagnetometerSensor = false
    private var isRegistered = false

    private var accelerometer = Vector3.zero
    private var magnetometer = Vector3.zero
    private var sensorManager: SensorManager
    private lateinit var sensorAccelerometer: Sensor
    private lateinit var sensorMagneticField: Sensor

    private var googleMap: GoogleMap? = null
    private var marker: Marker? = null
    private var circle: Circle? = null
    private var latLng: LatLng? = null
    private var mapsCallbacks: MapsCallbacks? = null
    private val viewHandler = Handler(Looper.getMainLooper())

    var location: Location? = null
    private var markerBitmap: Bitmap? = null
    var onTouch: ((event: MotionEvent, b: Boolean) -> Unit)? = null
    val sensorRegistrationRunnable = Runnable { register() }
    val cameraSpeed = 1000

    private var isCustomCoordinate = false
    private var isBearingRotation = false
    private var isFirstLocation = true
    private var isCompassRotation = false
    private var isNorthOnly = true

    private var customLatitude = 0.0
    private var customLongitude = 0.0

    private val lastLatitude = MainPreferences.getLastCoordinates()[0].toDouble()
    private val lastLongitude = MainPreferences.getLastCoordinates()[1].toDouble()

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    init {
        isCustomCoordinate = MainPreferences.isCustomCoordinate()
        isBearingRotation = GPSPreferences.isBearingRotation()
        isCompassRotation = GPSPreferences.isCompassRotation()
        isNorthOnly = GPSPreferences.isNorthOnly()

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
            LatLng(lastLatitude, lastLongitude)

        if (isCustomCoordinate) {
            addMarker(latLng!!)
        }

        googleMap.uiSettings.isCompassEnabled = false
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.uiSettings.isMyLocationButtonEnabled = false

        this.googleMap = googleMap

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
            viewHandler.removeCallbacks(mapAutoCenter)
        }

        this.googleMap?.setOnCameraIdleListener {
            GPSPreferences.setMapZoom(this.googleMap?.cameraPosition!!.zoom)
            GPSPreferences.setMapTilt(this.googleMap?.cameraPosition!!.tilt)
            viewHandler.removeCallbacks(mapAutoCenter)
            viewHandler.postDelayed(mapAutoCenter, 6000)
        }

        this.googleMap?.setOnMapClickListener {
            mapsCallbacks?.onMapClicked(this)
        }

        mapsCallbacks?.onMapInitialized()

        viewHandler.postDelayed(sensorRegistrationRunnable, 0L)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        with(super.dispatchTouchEvent(ev)) {
            onTouch?.invoke(ev, this)
            return this
        }
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
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
        viewHandler.postDelayed(sensorRegistrationRunnable, 250L)
    }

    fun destroy() {
        onDestroy()
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
        clearAnimation()
        viewHandler.removeCallbacks(mapAutoCenter)
        viewHandler.removeCallbacks(sensorRegistrationRunnable)
        viewHandler.removeCallbacksAndMessages(null)
        job.cancel()
    }

    fun resetCamera(zoom: Float) {
        if (isCustomCoordinate) {
            addMarker(LatLng(customLatitude, customLongitude))
            moveMapCamera(LatLng(customLatitude, customLongitude), zoom)
        } else
            if (location != null) {
                moveMapCamera(LatLng(location!!.latitude, location!!.longitude), zoom)
                addMarker(LatLng(location!!.latitude, location!!.longitude))
                viewHandler.removeCallbacks(mapAutoCenter)
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
                                    R.raw.maps_light_no_label
                                }
                            }
                            else -> 0
                        }
                ))
            }
        }
    }

    fun zoomIn() {
        unregister()
        googleMap?.animateCamera(CameraUpdateFactory.zoomIn(), object : GoogleMap.CancelableCallback {
            override fun onFinish() {
                registerWithRunnable()
            }

            override fun onCancel() {
                registerWithRunnable()
            }
        })
    }

    fun zoomOut() {
        unregister()
        googleMap?.animateCamera(CameraUpdateFactory.zoomOut(), object : GoogleMap.CancelableCallback {
            override fun onFinish() {
                registerWithRunnable()
            }

            override fun onCancel() {
                registerWithRunnable()
            }
        })
    }

    fun addMarker(latLng: LatLng) {
        launch {
            withContext(Dispatchers.Default) {
                if (isCustomCoordinate) {
                    markerBitmap = R.drawable.ic_place_custom
                            .toBitmapKeepingSize(
                                    context,
                                    GPSPreferences.getPinSize(),
                                    GPSPreferences.getPinOpacity())
                } else {
                    if (location.isNotNull()) {
                        if (!LocationExtension.getLocationStatus(context)) return@withContext

                        markerBitmap = LocationPins.locationsPins[GPSPreferences.getPinSkin()]
                                .toBitmapKeepingSize(
                                        context,
                                        GPSPreferences.getPinSize(),
                                        GPSPreferences.getPinOpacity())
                    }
                }
            }

            if (googleMap.isNotNull()) {
                try {
                    marker?.remove()
                    circle?.remove()

                    marker = googleMap?.addMarker(MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(markerBitmap!!)))

                    circle = googleMap?.addCircle(CircleOptions()
                            .center(latLng)
                            .radius(location?.accuracy?.toDouble() ?: 0.0)
                            .clickable(false)
                            .fillColor(ContextCompat.getColor(context, R.color.map_circle_color))
                            .strokeColor(ContextCompat.getColor(context, R.color.compass_pin_color))
                            .strokeWidth(3F))
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                invalidate()
            }
        }
    }

    fun clearMarkers() {
        if (!LocationExtension.getLocationStatus(context)) {
            markerBitmap?.recycle()
            marker?.remove()
            circle?.remove()
        }
    }

    private val mapAutoCenter = object : Runnable {
        override fun run() {
            if (GPSPreferences.isMapAutoCenter()) {
                if (isCustomCoordinate) {
                    moveMapCamera(LatLng(customLatitude, customLongitude), GPSPreferences.getMapZoom())
                } else {
                    if (location.isNotNull()) {
                        with(location!!) {
                            when {
                                isBearingRotation -> {
                                    moveMapCamera(LatLng(latitude, longitude), GPSPreferences.getMapZoom())
                                }
                                isCompassRotation -> {
                                    moveMapCamera(LatLng(latitude, longitude), GPSPreferences.getMapZoom())
                                }
                                isNorthOnly -> {
                                    moveMapCamera(LatLng(latitude, longitude), GPSPreferences.getMapZoom())
                                }
                            }
                        }
                    }
                }
            }

            viewHandler.postDelayed(this, 6000L)
        }
    }

    private fun moveMapCamera(latLng: LatLng, zoom: Float) {
        if (googleMap.isNull()) return

        if (isCompassRotation) {
            unregister()
        }

        val bearing = when {
            isCompassRotation -> {
                rotationAngle
            }
            isBearingRotation -> {
                location?.bearing
            }
            isNorthOnly -> {
                0F
            }
            else -> {
                0F
            }
        }

        googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(
                CameraPosition.builder()
                        .target(latLng)
                        .tilt(GPSPreferences.getMapTilt())
                        .zoom(zoom)
                        .bearing(bearing ?: 0F)
                        .build()),
                cameraSpeed,
                object : GoogleMap.CancelableCallback {
                    override fun onFinish() {
                        println("Finished")
                        viewHandler.postDelayed(sensorRegistrationRunnable, cameraSpeed.toLong())
                    }

                    override fun onCancel() {

                    }
                })
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

    fun registerWithRunnable() {
        viewHandler.removeCallbacks(sensorRegistrationRunnable)
        viewHandler.postDelayed(sensorRegistrationRunnable, 500L)
    }

    private fun register() {
        if (!isRegistered) {
            if (haveAccelerometerSensor && haveMagnetometerSensor) {
                unregister()
                if (isCompassRotation) {
                    sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME)
                    sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_GAME)

                    if (googleMap.isNotNull()) {
                        with(googleMap!!.uiSettings) {
                            isScrollGesturesEnabled = false
                            isZoomGesturesEnabled = false
                            isRotateGesturesEnabled = false
                        }
                    }

                    isRegistered = true

                    println("Registered")
                }
            }
        }
    }

    fun unregister() {
        viewHandler.removeCallbacks(sensorRegistrationRunnable)

        if (isRegistered) {
            if (haveAccelerometerSensor && haveMagnetometerSensor) {
                sensorManager.unregisterListener(this, sensorAccelerometer)
                sensorManager.unregisterListener(this, sensorMagneticField)

                isRegistered = false

                println("Unregistered")

                if (googleMap.isNotNull()) {
                    with(googleMap!!.uiSettings) {
                        isScrollGesturesEnabled = true
                        isZoomGesturesEnabled = true
                        isRotateGesturesEnabled = true
                    }
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
                viewHandler.removeCallbacks(mapAutoCenter)
                viewHandler.post(mapAutoCenter)
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
                    viewHandler.post(sensorRegistrationRunnable)
                } else {
                    isCompassRotation = false
                    unregister()
                }
            }
            GPSPreferences.useBearingRotation -> {
                isBearingRotation = GPSPreferences.isBearingRotation()

                if (googleMap.isNotNull() && isBearingRotation) {
                    with(googleMap!!) {
                        animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(
                                cameraPosition.target,
                                cameraPosition.zoom,
                                cameraPosition.tilt,
                                location?.bearing ?: 0F
                        )), cameraSpeed, null)
                    }
                }
            }
            GPSPreferences.isNorthOnly -> {
                isNorthOnly = GPSPreferences.isNorthOnly()

                if (googleMap.isNotNull() && isNorthOnly) {
                    with(googleMap!!) {
                        animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(
                                cameraPosition.target,
                                cameraPosition.zoom,
                                cameraPosition.tilt,
                                0F
                        )), cameraSpeed, null)
                    }
                }
            }
        }
    }
}
