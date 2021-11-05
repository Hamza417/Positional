package app.simple.positional.decorations.maps

import android.animation.ValueAnimator
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
import app.simple.positional.decorations.maputils.CircleUtils
import app.simple.positional.decorations.maputils.MarkerUtils
import app.simple.positional.math.CompassAzimuth
import app.simple.positional.math.LowPassFilter
import app.simple.positional.math.Vector3
import app.simple.positional.preferences.GPSPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import app.simple.positional.util.BitmapHelper.toBitmap
import app.simple.positional.util.BitmapHelper.toBitmapKeepingSize
import app.simple.positional.util.ColorUtils.resolveAttrColor
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

    var googleMap: GoogleMap? = null
    private var marker: Marker? = null
    private var circle: Circle? = null
    private var latLng: LatLng? = null
    private var polylineOptions: PolylineOptions? = null
    private var targetPolyline: Polyline? = null
    private var mapsCallbacks: MapsCallbacks? = null
    private val viewHandler = Handler(Looper.getMainLooper())

    var location: Location? = null
    private var markerBitmap: Bitmap? = null
    private var markerAnimator: ValueAnimator? = null
    private var circleAnimator: ValueAnimator? = null
    private var fillAnimator: ValueAnimator? = null
    private var strokeAnimator: ValueAnimator? = null
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

        polylineOptions = PolylineOptions()
            .width(10f)
            .jointType(JointType.ROUND)
            .color(context.resolveAttrColor(R.attr.colorAppAccent))
            .geodesic(false)

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

        this.googleMap?.setOnMapLongClickListener {
            mapsCallbacks?.onMapLongClicked(it)
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
        circleAnimator?.cancel()
        markerAnimator?.cancel()
        fillAnimator?.cancel()
        strokeAnimator?.cancel()
        job.cancel()
    }

    fun resetCamera(zoom: Float) {
        if (isCustomCoordinate) {
            addMarker(LatLng(customLatitude, customLongitude))
            moveMapCamera(LatLng(customLatitude, customLongitude), zoom)
        } else
            if (location.isNotNull()) {
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

                        markerBitmap = if (location!!.speed == 0.0F) {
                            LocationPins.getLocationPin()
                                .toBitmapKeepingSize(
                                        context,
                                        GPSPreferences.getPinSize(),
                                        GPSPreferences.getPinOpacity())
                        } else {
                            R.drawable.ic_bearing_marker
                                .toBitmapKeepingSize(
                                        context,
                                        GPSPreferences.getPinSize(),
                                        GPSPreferences.getPinOpacity())
                        }
                    }
                }
            }

            if (googleMap.isNotNull()) {
                runCatching {
                    if (isCustomCoordinate) {
                        throw IllegalStateException()
                    } else {
                        marker!!.apply {
                            setAnchor(0.5F, if (location!!.speed > 0F) 0.5F else 1F)
                            setIcon(BitmapDescriptorFactory.fromBitmap(markerBitmap!!))
                            isFlat = location!!.speed > 0F
                        }

                        markerAnimator?.cancel()
                        markerAnimator = MarkerUtils.animateMarker(location, marker)
                    }
                }.onFailure {
                    marker = if (isCustomCoordinate) {
                        googleMap?.addMarker(MarkerOptions().position(latLng)
                                                 .rotation(0F)
                                                 .flat(false)
                                                 .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap!!)))
                    } else {
                        googleMap?.addMarker(MarkerOptions().position(latLng)
                                                 .rotation(if (location!!.speed > 0F) location!!.bearing else 0F)
                                                 .anchor(0.5F, if (location!!.speed > 0F) 0.5F else 1F)
                                                 .flat(location!!.speed > 0F)
                                                 .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap!!)))
                    }
                }

                if (!isCustomCoordinate) {
                    runCatching {
                        circleAnimator?.cancel()
                        fillAnimator?.cancel()
                        strokeAnimator?.cancel()
                        circleAnimator = CircleUtils.animateCircle(location, circle)
                        fillAnimator = CircleUtils.animateFillColor(if (location!!.speed > 0F) ContextCompat.getColor(context, R.color.bearing_circle_color) else LocationPins.getCircleFillColor(), circle)
                        strokeAnimator = CircleUtils.animateStrokeColor(if (location!!.speed > 0F) ContextCompat.getColor(context, R.color.bearing_circle_stroke) else LocationPins.getCircleStrokeColor(), circle)
                    }.onFailure {
                        circle = googleMap?.addCircle(CircleOptions()
                                                          .center(latLng)
                                                          .radius(if (location!!.speed > 0F) location!!.speed.toDouble() else location?.accuracy?.toDouble()
                                                              ?: 0.0)
                                                          .clickable(false)
                                                          .fillColor(if (location!!.speed > 0F) ContextCompat.getColor(context, R.color.bearing_circle_color) else LocationPins.getCircleFillColor())
                                                          .strokeColor(if (location!!.speed > 0F) ContextCompat.getColor(context, R.color.bearing_circle_stroke) else LocationPins.getCircleStrokeColor())
                                                          .strokeWidth(2F))
                    }
                }

                drawMarkerToTargetPolyline()

                invalidate()
            }
        }
    }

    fun setTargetMarker(latLng: LatLng?) {
        if (googleMap.isNotNull()) {
            if (latLng.isNull()) {
                with(googleMap?.cameraPosition!!) {
                    GPSPreferences.setTargetMarkerLatitude(this.target.latitude.toFloat())
                    GPSPreferences.setTargetMarkerLongitude(this.target.longitude.toFloat())
                    GPSPreferences.setTargetMarker(true)
                }
            } else {
                GPSPreferences.setTargetMarkerLatitude(latLng!!.latitude.toFloat())
                GPSPreferences.setTargetMarkerLongitude(latLng.longitude.toFloat())
                GPSPreferences.setTargetMarker(true)
            }
        }
    }

    fun moveCameraToTarget() {
        with(GPSPreferences.getTargetMarkerCoordinates()) {
            moveMapCamera(
                    LatLng(this[0].toDouble(), this[1].toDouble()),
                    GPSPreferences.getMapZoom())
        }
    }

    private fun drawMarkerToTargetPolyline() {
        targetPolyline?.remove()
        polylineOptions?.points?.clear()

        if (GPSPreferences.isTargetMarkerSet()) {
            if (isCustomCoordinate) {
                polylineOptions?.add(LatLng(customLatitude, customLongitude))
            } else {
                polylineOptions?.add(LatLng(location?.latitude!!, location?.longitude!!))
            }
            polylineOptions?.add(LatLng(GPSPreferences.getTargetMarkerCoordinates()[0].toDouble(),
                                        GPSPreferences.getTargetMarkerCoordinates()[1].toDouble()))

            targetPolyline = googleMap?.addPolyline(polylineOptions!!)

            mapsCallbacks?.onTargetUpdated(polylineOptions?.points?.get(1), polylineOptions?.points?.get(0))

            polylineOptions?.startCap(CustomCap(BitmapDescriptorFactory.fromBitmap(R.drawable.ic_trail_start.toBitmap(context, 30))))
            polylineOptions?.endCap(CustomCap(BitmapDescriptorFactory.fromBitmap(R.drawable.seekbar_thumb.toBitmap(context, 30))))
        } else {
            mapsCallbacks?.onTargetUpdated(null, null)
        }
    }

    private val mapAutoCenter = object : Runnable {
        override fun run() {
            if (GPSPreferences.isMapAutoCenter() && !GPSPreferences.isTargetMarkerMode()) {
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
                                    if (!googleMap?.projection?.visibleRegion?.latLngBounds?.contains(LatLng(latitude, longitude))!!) {
                                        moveMapCamera(LatLng(latitude, longitude), GPSPreferences.getMapZoom())
                                    }
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
                accelerometer =
                    Vector3(accelerometerReadings[0], accelerometerReadings[1], accelerometerReadings[2])
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                LowPassFilter.smoothAndSetReadings(magnetometerReadings, event.values, readingsAlpha)
                magnetometer =
                    Vector3(magnetometerReadings[0], magnetometerReadings[1], magnetometerReadings[2])
            }
        }

        rotationAngle =
            CompassAzimuth.calculate(gravity = accelerometer, magneticField = magnetometer)

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
            GPSPreferences.mapTargetMarker -> {
                drawMarkerToTargetPolyline()
            }
        }
    }
}
