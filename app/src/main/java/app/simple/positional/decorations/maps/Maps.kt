package app.simple.positional.decorations.maps

import android.animation.ValueAnimator
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.util.AttributeSet
import android.util.Log
import androidx.core.content.ContextCompat
import app.simple.positional.R
import app.simple.positional.constants.LocationPins
import app.simple.positional.decorations.utils.CircleUtils
import app.simple.positional.decorations.utils.MarkerUtils
import app.simple.positional.extensions.maps.CustomMaps
import app.simple.positional.math.CompassAzimuth
import app.simple.positional.math.LowPassFilter
import app.simple.positional.math.Vector3
import app.simple.positional.preferences.GPSPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.BitmapHelper.addLinearGradient
import app.simple.positional.util.BitmapHelper.toBitmap
import app.simple.positional.util.BitmapHelper.toBitmapKeepingSize
import app.simple.positional.util.ColorUtils.resolveAttrColor
import app.simple.positional.util.ConditionUtils.invert
import app.simple.positional.util.ConditionUtils.isNotNull
import app.simple.positional.util.ConditionUtils.isNull
import app.simple.positional.util.LocationExtension
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.CustomCap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Maps(context: Context, attributeSet: AttributeSet) : CustomMaps(context, attributeSet),
    SensorEventListener {

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

    private var marker: Marker? = null
    private var circle: Circle? = null
    private var polylineOptions: PolylineOptions? = null
    private var targetPolyline: Polyline? = null

    private var markerBitmap: Bitmap? = null
    private var markerAnimator: ValueAnimator? = null
    private var circleAnimator: ValueAnimator? = null
    private var fillAnimator: ValueAnimator? = null
    private var strokeAnimator: ValueAnimator? = null

    private val sensorRegistrationRunnable = Runnable { register() }

    private var isCustomCoordinate = false
    private var isBearingRotation = false
    private var isFirstLocation = true
    private var isCompassRotation = false
    private var isNorthOnly = true

    private var customLatitude = 0.0
    private var customLongitude = 0.0

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

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        kotlin.runCatching {
            sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!!
            sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
            haveMagnetometerSensor = true
            haveAccelerometerSensor = true
        }.getOrElse {
            haveAccelerometerSensor = false
            haveMagnetometerSensor = false
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        super.onMapReady(p0)

        latLng = if (isCustomCoordinate)
            LatLng(customLatitude, customLongitude)
        else
            LatLng(lastLatitude, lastLongitude)

        if (isCustomCoordinate) {
            addMarker(latLng!!)
        } else {
            if (location.isNotNull()) {
                setFirstLocation(location!!)
            }
        }

        setMapStyle(GPSPreferences.isLabelOn())
        setSatellite()
        setTraffic(GPSPreferences.isTrafficShown())
        setBuildings(GPSPreferences.getShowBuildingsOnMap())

        this.googleMap?.moveCamera(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition(
                    latLng!!,
                    GPSPreferences.getMapZoom(),
                    GPSPreferences.getMapTilt(),
                    GPSPreferences.getMapBearing()
                )
            )
        )

        this.googleMap?.setOnCameraMoveStartedListener { reason ->
            when (reason) {
                GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE -> {
                    isMapMovementEnabled = false
                    clearAnimation()
                    Log.i("Maps", "Camera Move Started by Gesture")
                }
            }
        }

        this.googleMap?.setOnCameraIdleListener {
            GPSPreferences.setMapZoom(this.googleMap?.cameraPosition!!.zoom)
            GPSPreferences.setMapTilt(this.googleMap?.cameraPosition!!.tilt)
            GPSPreferences.setMapBearing(this.googleMap?.cameraPosition!!.bearing)

            viewHandler.postDelayed(sensorRegistrationRunnable, 100L)
            isAnimating = false
            clearAnimation()
        }

        this.googleMap?.setOnCameraMoveCanceledListener {
            clearAnimation()
            reestablishSensorRegistration()
            isAnimating = false
        }

        viewHandler.postDelayed(sensorRegistrationRunnable, 0L)

        mapsCallbacks?.onMapInitialized()
    }

    override fun onPause() {
        super.onPause()
        unregister()
    }

    override fun onResume() {
        super.onResume()
        viewHandler.postDelayed(sensorRegistrationRunnable, 250L)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewHandler.removeCallbacks(sensorRegistrationRunnable)
        viewHandler.removeCallbacksAndMessages(null)
        circleAnimator?.cancel()
        markerAnimator?.cancel()
        fillAnimator?.cancel()
        strokeAnimator?.cancel()
    }

    private fun reestablishSensorRegistration() {
        viewHandler.postDelayed(sensorRegistrationRunnable, 100L)
    }

    fun resetCamera(zoom: Float) {
        when {
            isCustomCoordinate -> {
                moveMapCamera(LatLng(customLatitude, customLongitude), zoom)
                // addMarker(LatLng(customLatitude, customLongitude))
            }

            location.isNotNull() -> {
                moveMapCamera(LatLng(location!!.latitude, location!!.longitude), zoom)
                // addMarker(LatLng(location!!.latitude, location!!.longitude))
            }

            else               -> {
                kotlin.runCatching {
                    moveMapCamera(googleMap?.cameraPosition?.target!!, zoom)
                }.getOrElse {
                    it.printStackTrace()
                }
            }
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

    private fun setMapStyle(value: Boolean) {
        setSatellite()

        if (!googleMap.isNull()) {
            if (GPSPreferences.getHighContrastMap()) {
                googleMap?.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        context,
                        if (value) {
                            R.raw.map_high_contrast_labelled
                        } else {
                            R.raw.map_high_contrast_non_labelled
                        }
                    )
                )
            } else {
                googleMap?.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        context,
                        when (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                            Configuration.UI_MODE_NIGHT_YES -> {
                                if (value) {
                                    R.raw.maps_dark_labelled
                                } else {
                                    R.raw.maps_dark_no_label
                                }
                            }

                            Configuration.UI_MODE_NIGHT_NO  -> {
                                if (value) {
                                    R.raw.maps_light_labelled
                                } else {
                                    R.raw.maps_light_no_label
                                }
                            }

                            else                            -> 0
                        }
                    )
                )
            }
        }
    }

    fun zoomIn() {
        unregister()
        isAnimating = true
        googleMap?.animateCamera(
            CameraUpdateFactory.zoomIn(),
            object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                    registerWithRunnable()
                    isAnimating = false
                }

                override fun onCancel() {
                    registerWithRunnable()
                    isAnimating = false
                }
            })
    }

    fun zoomOut() {
        unregister()
        isAnimating = true
        googleMap?.animateCamera(
            CameraUpdateFactory.zoomOut(),
            object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                    registerWithRunnable()
                    isAnimating = false
                }

                override fun onCancel() {
                    registerWithRunnable()
                    isAnimating = false
                }
            })
    }

    fun addMarker(latLng: LatLng) {
        launch {
            this@Maps.latLng = latLng

            withContext(Dispatchers.Default) {
                if (isCustomCoordinate) {
                    markerBitmap = LocationPins.getLocationPin()
                            .toBitmapKeepingSize(
                                context,
                                GPSPreferences.getPinSize(),
                                GPSPreferences.getPinOpacity()
                            ).addLinearGradient(
                                intArrayOf(
                                    Color.parseColor("#FF1B50"),
                                    Color.parseColor("#e11677")))
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
                    if (marker.isNull()) {
                        marker = if (isCustomCoordinate) {
                            googleMap?.addMarker(
                                MarkerOptions().position(latLng)
                                        .rotation(0F)
                                        .flat(false)
                                        .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap!!))
                            )
                        } else {
                            googleMap?.addMarker(
                                MarkerOptions().position(latLng)
                                        .rotation(if (location!!.speed > 0F) location!!.bearing else 0F)
                                        .anchor(0.5F, if (location!!.speed > 0F) 0.5F else 1F)
                                        .flat(location!!.speed > 0F)
                                        .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap!!))
                            )
                        }
                    } else {
                        if (isCustomCoordinate) {
                            marker!!.apply {
                                setAnchor(0.5F, 1F)
                                setIcon(BitmapDescriptorFactory.fromBitmap(markerBitmap!!))
                                isFlat = false
                            }
                        } else {
                            marker!!.apply {
                                setAnchor(0.5F, if (location!!.speed > 0F) 0.5F else 1F)
                                setIcon(BitmapDescriptorFactory.fromBitmap(markerBitmap!!))
                                isFlat = location!!.speed > 0F
                            }

                            markerAnimator?.cancel()
                            markerAnimator = MarkerUtils.animateMarker(location, marker)
                        }
                    }
                }

                if (!isCustomCoordinate) {
                    runCatching {
                        circleAnimator?.cancel()
                        fillAnimator?.cancel()
                        strokeAnimator?.cancel()
                        circleAnimator = CircleUtils.animateCircle(location, circle)
                        fillAnimator = CircleUtils.animateFillColor(
                            if (location!!.speed > 0F) ContextCompat.getColor(
                                context,
                                R.color.bearing_circle_color
                            ) else LocationPins.getCircleFillColor(), circle
                        )
                        strokeAnimator = CircleUtils.animateStrokeColor(
                            if (location!!.speed > 0F) ContextCompat.getColor(
                                context,
                                R.color.bearing_circle_stroke
                            ) else LocationPins.getCircleStrokeColor(), circle
                        )
                    }.onFailure {
                        circle = googleMap?.addCircle(
                            CircleOptions()
                                    .center(latLng)
                                    .radius(
                                        if (location!!.speed > 0F) location!!.speed.toDouble() else location?.accuracy?.toDouble()
                                                                                                    ?: 0.0
                                    )
                                    .clickable(false)
                                    .fillColor(
                                        if (location!!.speed > 0F) ContextCompat.getColor(
                                            context,
                                            R.color.bearing_circle_color
                                        ) else LocationPins.getCircleFillColor()
                                    )
                                    .strokeColor(
                                        if (location!!.speed > 0F) ContextCompat.getColor(
                                            context,
                                            R.color.bearing_circle_stroke
                                        ) else LocationPins.getCircleStrokeColor()
                                    )
                                    .strokeWidth(2F)
                        )
                    }
                }

                drawMarkerToTargetPolyline()
                invalidate()

                if (isCompassRotation) {
                    if (isMapMovementEnabled && !isAnimating && !isCameraWithinBounds(latLng)) moveMap()
                } else {
                    if (isMapMovementEnabled && !isAnimating) moveMap()
                }
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
                GPSPreferences.getMapZoom()
            )
        }
    }

    private fun drawMarkerToTargetPolyline() {
        targetPolyline?.remove()
        polylineOptions?.points?.clear()

        if (GPSPreferences.isTargetMarkerSet()) {
            if (isCustomCoordinate) {
                polylineOptions?.add(LatLng(customLatitude, customLongitude))
            } else {
                if (location.isNotNull()) {
                    polylineOptions?.add(LatLng(GPSPreferences.getTargetMarkerStartCoordinates()[0].toDouble(),
                        GPSPreferences.getTargetMarkerStartCoordinates()[1].toDouble()))
                    polylineOptions?.add(LatLng(location?.latitude!!, location?.longitude!!))
                }
            }

            polylineOptions?.add(LatLng(GPSPreferences.getTargetMarkerCoordinates()[0].toDouble(),
                GPSPreferences.getTargetMarkerCoordinates()[1].toDouble()))

            targetPolyline = googleMap?.addPolyline(polylineOptions!!)

            if (location.isNotNull() || isCustomCoordinate) {
                mapsCallbacks?.onTargetUpdated(
                    polylineOptions?.points?.get(1),
                    polylineOptions?.points?.get(0),
                    location?.speed ?: 0F)
            }

            polylineOptions?.startCap(
                CustomCap(BitmapDescriptorFactory.fromBitmap(
                    R.drawable.ic_trail_start.toBitmap(
                        context,
                        30))))

            polylineOptions?.endCap(
                CustomCap(BitmapDescriptorFactory.fromBitmap(
                    R.drawable.seekbar_thumb.toBitmap(
                        context,
                        30))))
        } else {
            mapsCallbacks?.onTargetUpdated(null, null, 0F)
        }
    }

    private fun moveMap() {
        if (!GPSPreferences.isTargetMarkerMode()) {
            if (isCustomCoordinate) {
                moveMapCamera(LatLng(customLatitude, customLongitude), GPSPreferences.getMapZoom())
            } else {
                if (location.isNotNull()) {
                    with(location!!) {
                        moveMapCamera(LatLng(latitude, longitude), GPSPreferences.getMapZoom())
                    }
                }
            }
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

            else        -> {
                0F
            }
        }

        isAnimating = true
        animateCamera(latLng, zoom, GPSPreferences.getMapTilt(), bearing = bearing ?: 0F)
    }

    fun setFirstLocation(location: Location?) {
        if (googleMap.isNotNull() && isFirstLocation) {
            this.location = location

            with(LatLng(location!!.latitude, location.longitude)) {
                addMarker(this)
                googleMap?.moveCamera(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition(
                            this,
                            GPSPreferences.getMapZoom(),
                            GPSPreferences.getMapTilt(),
                            GPSPreferences.getMapBearing()
                        )
                    )
                )

                latLng = this
                isFirstLocation = false
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER  -> {
                LowPassFilter.smoothAndSetReadings(
                    accelerometerReadings,
                    event.values,
                    readingsAlpha
                )
                accelerometer = Vector3(
                    accelerometerReadings[0],
                    accelerometerReadings[1],
                    accelerometerReadings[2]
                )
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                LowPassFilter.smoothAndSetReadings(
                    magnetometerReadings,
                    event.values,
                    readingsAlpha
                )
                magnetometer = Vector3(
                    magnetometerReadings[0],
                    magnetometerReadings[1],
                    magnetometerReadings[2]
                )
            }
        }

        rotationAngle =
            CompassAzimuth.calculate(gravity = accelerometer, magneticField = magnetometer, windowManager)

        if (isCompassRotation) {
            if (googleMap.isNotNull())
                with(googleMap!!) {
                    moveCamera(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition(
                                cameraPosition.target,
                                cameraPosition.zoom,
                                cameraPosition.tilt,
                                rotationAngle
                            )
                        )
                    )
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
                    sensorManager.registerListener(
                        this,
                        sensorAccelerometer,
                        SensorManager.SENSOR_DELAY_GAME
                    )
                    sensorManager.registerListener(
                        this,
                        sensorMagneticField,
                        SensorManager.SENSOR_DELAY_GAME
                    )

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
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            GPSPreferences.highContrastMap, GPSPreferences.GPSLabelMode -> {
                setMapStyle(GPSPreferences.isLabelOn())
            }

            GPSPreferences.GPSSatellite       -> {
                setSatellite()
            }

            GPSPreferences.showBuilding       -> {
                setBuildings(GPSPreferences.getShowBuildingsOnMap())
            }

            GPSPreferences.pinSkin,
            GPSPreferences.pinSize,
            GPSPreferences.pinOpacity         -> {
                Log.d("Maps", "Pin Skin, Size or Opacity Changed")
                if (isCustomCoordinate.invert()) {
                    if (location.isNotNull()) {
                        if (latLng.isNotNull()) {
                            addMarker(latLng!!)
                            Log.d("Maps", "Marker Added")
                        }
                    }
                } else {
                    if (latLng.isNotNull()) {
                        addMarker(latLng!!)
                        Log.d("Maps", "Custom marker Added")
                    }
                }
            }

            GPSPreferences.compassRotation    -> {
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
                        isAnimating = true
                        animateCamera(CameraUpdateFactory.newCameraPosition(
                            CameraPosition(
                                cameraPosition.target,
                                cameraPosition.zoom,
                                cameraPosition.tilt,
                                location?.bearing ?: 0F
                            )
                        ), CAMERA_SPEED, object : GoogleMap.CancelableCallback {
                            override fun onCancel() {
                                isAnimating = false
                            }

                            override fun onFinish() {
                                isAnimating = false
                            }
                        })
                    }
                }
            }

            GPSPreferences.isNorthOnly        -> {
                isNorthOnly = GPSPreferences.isNorthOnly()

                if (googleMap.isNotNull() && isNorthOnly) {
                    with(googleMap!!) {
                        isAnimating = true
                        animateCamera(CameraUpdateFactory.newCameraPosition(
                            CameraPosition(
                                cameraPosition.target,
                                cameraPosition.zoom,
                                cameraPosition.tilt,
                                0F
                            )
                        ), CAMERA_SPEED, object : GoogleMap.CancelableCallback {
                            override fun onFinish() {
                                isAnimating = false
                            }

                            override fun onCancel() {
                                isAnimating = false
                            }
                        })
                    }
                }
            }

            GPSPreferences.mapTargetMarker    -> {
                drawMarkerToTargetPolyline()
            }

            GPSPreferences.trafficMode        -> {
                setTraffic(GPSPreferences.isTrafficShown())
            }
        }
    }
}
