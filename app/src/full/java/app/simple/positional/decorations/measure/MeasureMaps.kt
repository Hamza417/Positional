package app.simple.positional.decorations.measure

import android.animation.ValueAnimator
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.AttributeSet
import android.util.Log
import androidx.core.content.ContextCompat
import app.simple.positional.R
import app.simple.positional.decorations.utils.CircleUtils
import app.simple.positional.decorations.utils.MarkerUtils
import app.simple.positional.extensions.maps.CustomMaps
import app.simple.positional.math.CompassAzimuth
import app.simple.positional.math.LowPassFilter
import app.simple.positional.math.Vector3
import app.simple.positional.model.Measure
import app.simple.positional.model.MeasurePoint
import app.simple.positional.preferences.MeasurePreferences
import app.simple.positional.util.ArrayHelper.secondLastOrNull
import app.simple.positional.util.BitmapHelper.toBitmap
import app.simple.positional.util.BitmapHelper.toBitmapKeepingSize
import app.simple.positional.util.ColorUtils.resolveAttrColor
import app.simple.positional.util.ConditionUtils.isNotNull
import app.simple.positional.util.ConditionUtils.isNull
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.CustomCap
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PatternItem
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MeasureMaps(context: Context, attrs: AttributeSet) : CustomMaps(context, attrs),
    SensorEventListener {

    private val accelerometerReadings = FloatArray(3)
    private val magnetometerReadings = FloatArray(3)
    private var readingsAlpha = 0.03f
    private var rotationAngle = 0f
    private var accuracy = -1

    private var haveAccelerometerSensor = false
    private var haveMagnetometerSensor = false

    private var accelerometer = Vector3.zero
    private var magnetometer = Vector3.zero
    private val sensorManager: SensorManager
    private lateinit var sensorAccelerometer: Sensor
    private lateinit var sensorMagneticField: Sensor

    private var markerBitmap: Bitmap? = null
    private var marker: Marker? = null
    private var circle: Circle? = null
    private var markerAnimator: ValueAnimator? = null
    private var circleAnimator: ValueAnimator? = null
    private var polylineAnimator: ValueAnimator? = null

    private var isWrapped = false
    private var isFirstLocation = true
    private var isCompassRotation = true
    private var lastZoom = 20F
    private var lastTilt = 0F
    private var lastBearing = 0F
    private val incrementFactor = 2

    private var polylineOptions: PolylineOptions? = null
    private val currentPolylines = arrayListOf<LatLng>()
    private val polylines = arrayListOf<Polyline>()
    private var lastPolyline: Polyline? = null
    private var cameraTargetPolyline: Polyline? = null
    private val textPolylines = mutableListOf<TextPolyline>()
    private var measurePoints = arrayListOf<MeasurePoint>()

    private val pattern: List<PatternItem> = listOf(
        Dash(20f), Gap(10f), Dash(30f), Gap(20f), Dash(40f), Gap(30f))

    init {
        polylineOptions = PolylineOptions()
                .width(10f)
                .jointType(JointType.ROUND)
                .color(context.resolveAttrColor(R.attr.colorAppAccent))

        latLng = MeasurePreferences.getLastLatLng()
        isCompassRotation = MeasurePreferences.isCompassRotation()
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

        addMarker(latLng!!)
        setMapStyle(MeasurePreferences.isLabelOn(),
            MeasurePreferences.isSatelliteOn(),
            MeasurePreferences.getHighContrastMap())
        setBuildings(MeasurePreferences.getShowBuildingsOnMap())
        mapsCallbacks?.onCameraDistance(googleMap?.cameraPosition?.target!!)

        if (!MeasurePreferences.arePolylinesWrapped()) {
            this.googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(
                latLng!!,
                MeasurePreferences.getMapZoom(),
                MeasurePreferences.getMapTilt(),
                MeasurePreferences.getMapBearing())))
        }

        this.googleMap?.setOnCameraIdleListener {
            MeasurePreferences.setMapZoom(this.googleMap?.cameraPosition!!.zoom)
            MeasurePreferences.setMapTilt(this.googleMap?.cameraPosition!!.tilt)
            MeasurePreferences.setMapBearing(this.googleMap?.cameraPosition!!.bearing)
            MeasurePreferences.setLastLatitude(this.googleMap?.cameraPosition!!.target.latitude)
            MeasurePreferences.setLastLongitude(this.googleMap?.cameraPosition!!.target.longitude)
        }

        this.googleMap?.setOnCameraMoveListener {
            val lastPoint = currentPolylines.lastOrNull()
            val cameraTarget = googleMap?.cameraPosition?.target
            mapsCallbacks?.onCameraDistance(cameraTarget!!)

            if (lastPoint != null && cameraTarget != null) {
                if (cameraTargetPolyline == null) {
                    initCameraTargetPolyline()
                } else {
                    cameraTargetPolyline?.points = listOf(lastPoint, cameraTarget)
                }
            }
        }

        mapsCallbacks?.onMapInitialized()
        registerSensors()
    }

    private fun initCameraTargetPolyline() {
        val lastPoint = currentPolylines.lastOrNull() ?: return
        val cameraTarget = googleMap?.cameraPosition?.target ?: return

        Log.d("MeasureMaps", "initCameraTargetPolyline: $lastPoint, $cameraTarget")

        val newPolylineOptions = PolylineOptions()
                .add(lastPoint)
                .add(cameraTarget)
                .width(7f)
                .color(Color.BLACK)
                .pattern(pattern)
                .startCap(CustomCap(BitmapDescriptorFactory.fromBitmap(R.drawable.ic_trail_start.toBitmap(context, 30))))
                .endCap(CustomCap(BitmapDescriptorFactory.fromBitmap(R.drawable.seekbar_thumb.toBitmap(context, 30))))

        cameraTargetPolyline = googleMap?.addPolyline(newPolylineOptions)

        Log.i("MeasureMaps", "initCameraTargetPolyline: $cameraTargetPolyline")
    }

    fun addMarker(latLng: LatLng) {
        launch {
            withContext(Dispatchers.Main) {
                if (context.isNotNull() && googleMap.isNotNull())
                    markerBitmap = if (location.isNotNull()) {
                        if (MeasurePreferences.isCompassRotation()) {
                            when (accuracy) {
                                SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                                    R.drawable.ic_pin_unreliable.toBitmapKeepingSize(context, incrementFactor)
                                }

                                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                                    R.drawable.ic_pin_low.toBitmapKeepingSize(context, incrementFactor)
                                }

                                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                                    R.drawable.ic_pin_medium.toBitmapKeepingSize(context, incrementFactor)
                                }

                                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                                    R.drawable.ic_pin_high.toBitmapKeepingSize(context, incrementFactor)
                                }

                                else -> {
                                    R.drawable.ic_pin_unreliable.toBitmapKeepingSize(context, incrementFactor)
                                }
                            }
                        } else {
                            if (location!!.speed == 0.0F) {
                                R.drawable.ic_pin_no_speed.toBitmapKeepingSize(context, incrementFactor)
                            } else {
                                R.drawable.ic_pin_bearing.toBitmapKeepingSize(context, incrementFactor)
                            }
                        }
                    } else {
                        R.drawable.ic_place_historical.toBitmap(context, 60)
                    }

                withContext(Dispatchers.Main) {
                    runCatching {
                        marker!!.apply {
                            setIcon(BitmapDescriptorFactory.fromBitmap(markerBitmap!!))
                        }

                        markerAnimator?.cancel()
                        markerAnimator = MarkerUtils.animateMarker(
                            location,
                            marker,
                            location?.bearing ?: 0F,
                            isCompassRotation)
                    }.onFailure {
                        marker = googleMap?.addMarker(MarkerOptions()
                                .position(latLng)
                                .rotation(if (MeasurePreferences.isCompassRotation()) rotationAngle else location?.bearing
                                                                                                         ?: 0F)
                                .anchor(0.5F, 0.5F)
                                .flat(true)
                                .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap!!)))
                    }

                    runCatching {
                        circleAnimator?.cancel()
                        circleAnimator = CircleUtils.animateCircle(location, circle)
                    }.onFailure {
                        circle = googleMap?.addCircle(CircleOptions()
                                .center(latLng)
                                .radius(location?.accuracy?.toDouble() ?: 0.0)
                                .clickable(false)
                                .fillColor(ContextCompat.getColor(context, R.color.map_circle_color))
                                .strokeColor(ContextCompat.getColor(context, R.color.compass_pin_color))
                                .strokeWidth(3F))
                    }

                    invalidate()
                }
            }
        }
    }

    fun setFirstLocation(latLng: LatLng) {
        if (googleMap.isNotNull() && isFirstLocation) {
            addMarker(latLng)
            googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(
                MeasurePreferences.getLastLatLng(),
                MeasurePreferences.getMapZoom(),
                MeasurePreferences.getMapTilt(),
                MeasurePreferences.getMapBearing())))

            this.latLng = latLng
            isFirstLocation = false
        }
    }

    fun resetCamera(zoom: Float) {
        if (location != null) {
            moveMapCamera(
                LatLng(location!!.latitude, location!!.longitude),
                zoom,
                MeasurePreferences.getMapTilt(),
                MeasurePreferences.getMapBearing())
        }
    }

    fun moveMapCamera(latLng: LatLng, zoom: Float, tilt: Float, bearing: Float) {
        if (googleMap.isNull() && latLng.isNull()) return

        animateCamera(latLng, zoom, tilt, bearing)

        isWrapped = false
        MeasurePreferences.setPolylinesWrapped(false)
    }

    fun wrapUnwrap() {
        if (isWrapped) {
            moveMapCamera(latLng!!, lastZoom, lastTilt, lastBearing)
        } else {
            wrap(true)
        }
    }

    private fun wrap(animate: Boolean) {
        kotlin.runCatching {
            clearAnimation()
            latLng = googleMap?.cameraPosition?.target
            lastZoom = googleMap?.cameraPosition?.zoom!!
            lastTilt = googleMap?.cameraPosition?.tilt ?: 0F
            lastBearing = googleMap?.cameraPosition?.bearing ?: 0F

            val builder = LatLngBounds.Builder()
            for (latLng in currentPolylines) {
                builder.include(latLng)
            }

            val bounds = builder.build()

            //BOUND_PADDING is an int to specify padding of bound.. try 100.
            if (animate) {
                googleMap!!.animateCamera(CameraUpdateFactory
                        .newLatLngBounds(bounds, 250))
            } else {
                googleMap!!.moveCamera(CameraUpdateFactory
                        .newLatLngBounds(bounds, 250))
            }

            isWrapped = true
        }.onFailure {
            isWrapped = MeasurePreferences.arePolylinesWrapped()
        }
    }

    private fun unwrap() {
        clearAnimation()
        moveMapCamera(latLng!!, lastZoom, lastTilt, lastBearing)
    }

    private fun updatePolylines(points: ArrayList<MeasurePoint>) {
        googleMap?.clear() // Will clear all markers, polylines, and circles
        marker = null
        cameraTargetPolyline = null
        polylines.clear()
        currentPolylines.clear()
        polylineOptions?.points?.clear()
        textPolylines.clear()

        polylineOptions?.startCap(CustomCap(BitmapDescriptorFactory.fromBitmap(R.drawable.ic_trail_start.toBitmap(context, 30))))
        polylineOptions?.endCap(CustomCap(BitmapDescriptorFactory.fromBitmap(R.drawable.seekbar_thumb.toBitmap(context, 30))))

        for (point in points) {
            val latLng = LatLng(point.latitude, point.longitude)
            currentPolylines.add(latLng)
            polylineOptions?.add(latLng)
            polylines.add(googleMap?.addPolyline(polylineOptions!!)!!)
        }

        invalidate()
        lastPolyline = polylines.lastOrNull()

        mapsCallbacks?.onLineCountChanged(polylineOptions!!.points.size)

        if (MeasurePreferences.arePolylinesWrapped()) {
            wrap(false)
        }

        initCameraTargetPolyline()
    }

    fun removeRecentPolyline() {
        cameraTargetPolyline?.remove()
        polylines.remove(cameraTargetPolyline)
        lastPolyline = polylines.secondLastOrNull()
        polylines.lastOrNull()?.remove()
        polylines.removeLastOrNull()
        currentPolylines.removeLastOrNull()
        polylineOptions?.points?.removeLastOrNull()
        mapsCallbacks?.onLineDeleted(measurePoints.lastOrNull())
        mapsCallbacks?.onLineCountChanged(polylineOptions!!.points.size)
        measurePoints.removeLastOrNull()
        cameraTargetPolyline?.remove()
        cameraTargetPolyline = null

//        kotlin.runCatching {
//            if (MeasurePreferences.arePolylinesWrapped()) {
//                wrap(true)
//            } else {
//                with(measurePoints.lastOrNull()!!) {
//                    moveMapCamera(latLng, MeasurePreferences.getMapZoom(), MeasurePreferences.getMapTilt(), cameraSpeed)
//                }
//            }
//        }.getOrElse {
//            moveMapCamera(latLng!!, MeasurePreferences.getMapZoom(), MeasurePreferences.getMapTilt(), cameraSpeed)
//        }

        initCameraTargetPolyline()
        if (currentPolylines.isNotEmpty()) {
            polylines.add(cameraTargetPolyline!!)
        }
        invalidate()
        mapsCallbacks?.onCameraDistance(googleMap?.cameraPosition?.target!!)
    }

    // Function to add text markers along a polyline
    private fun addTextMarkersAlongPolyline(polyline: Polyline, text: String) {
        val points = polyline.points
        for (point in points) {
            googleMap?.addMarker(
                MarkerOptions()
                        .position(point)
                        .title(text)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )
        }
    }

    private fun registerSensors() {
        if (haveAccelerometerSensor && haveMagnetometerSensor) {
            unregisterSensors()
            if (MeasurePreferences.isCompassRotation()) {
                sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME)
                sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_GAME)
            }
        }
    }

    private fun unregisterSensors() {
        if (haveAccelerometerSensor && haveMagnetometerSensor) {
            sensorManager.unregisterListener(this, sensorAccelerometer)
            sensorManager.unregisterListener(this, sensorMagneticField)
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
            CompassAzimuth.calculate(gravity = accelerometer, magneticField = magnetometer, windowManager)

        if (isCompassRotation) {
            marker?.rotation = rotationAngle
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        accuracy = p1
    }

    override fun onPause() {
        super.onPause()
        unregisterSensors()
    }

    override fun onResume() {
        super.onResume()
        registerSensors()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterSensors()
        circleAnimator?.cancel()
        markerAnimator?.cancel()
        polylineAnimator?.cancel()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            MeasurePreferences.LABEL_MODE,
            MeasurePreferences.SATELLITE_MAP,
            MeasurePreferences.HIGH_CONTRAST_MAP -> {
                setMapStyle(MeasurePreferences.isLabelOn(), MeasurePreferences.isSatelliteOn(), MeasurePreferences.getHighContrastMap())
            }

            MeasurePreferences.SHOW_BUILDINGS -> {
                setBuildings(MeasurePreferences.getShowBuildingsOnMap())
            }

            MeasurePreferences.COMPASS_ROTATION -> {
                isCompassRotation = MeasurePreferences.isCompassRotation()
                if (isCompassRotation) {
                    registerSensors()
                } else {
                    unregisterSensors()
                }
            }

            MeasurePreferences.POLYLINES_WRAPPED -> {
                if (MeasurePreferences.arePolylinesWrapped()) {
                    wrap(true)
                } else {
                    unwrap()
                }
            }
        }
    }

    fun createMeasurePolylines(measure: Measure?) {
        try {
            measurePoints.clear()
            measurePoints.addAll(measure!!.measurePoints!!)
            removeAllPolylines()
            updatePolylines(measurePoints)
        } catch (e: NullPointerException) {
            Log.e("MeasureMaps", "createMeasurePolylines: $e")
        }
    }

    private fun removeAllPolylines() {
        polylines.forEach { it.remove() }
        polylines.clear()
        currentPolylines.clear()
        polylineOptions?.points?.clear()
        textPolylines.clear()
        cameraTargetPolyline?.remove()
        cameraTargetPolyline = null
        invalidate()
    }

    fun addPolyline() {
        val latLng = googleMap?.cameraPosition?.target!!
        val lastPoint = currentPolylines.lastOrNull() ?: latLng
        polylineOptions?.add(lastPoint)
        currentPolylines.add(latLng)
        polylines.add(googleMap?.addPolyline(polylineOptions!!)!!)
        val measurePoint = MeasurePoint(latLng.latitude, latLng.longitude, measurePoints.size + 1)
        measurePoints.add(measurePoint)
        cameraTargetPolyline?.remove()
        cameraTargetPolyline = null
        initCameraTargetPolyline()
        invalidate()
        mapsCallbacks?.onLineAdded(measurePoint)
        mapsCallbacks?.onCameraDistance(latLng)

        polylineAnimator = MarkerUtils.animatePolyline(lastPoint, latLng, polylines.lastOrNull()!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (textPolyline in textPolylines) {
            textPolyline.draw(canvas)
        }
    }

    fun getMeasurePoints(): ArrayList<MeasurePoint> {
        return measurePoints
    }

    fun clear() {
        removeAllPolylines()
        marker?.remove()
        marker = null
        circle?.remove()
        circle = null
        markerBitmap = null
        markerAnimator?.cancel()
        circleAnimator?.cancel()
        polylineAnimator?.cancel()
        cameraTargetPolyline?.remove()
        cameraTargetPolyline = null
        googleMap?.clear()
        invalidate()
    }

    inner class TextPolyline(private val polyline: Polyline, private val text: String) {
        private val paint = Paint().apply {
            color = Color.BLACK
            textSize = 40f
            isAntiAlias = true
        }

        fun draw(canvas: Canvas) {
            val points = polyline.points
            for (i in 0 until points.size - 1) {
                val start = points[i]
                val end = points[i + 1]
                val midPoint = LatLng(
                    (start.latitude + end.latitude) / 2,
                    (start.longitude + end.longitude) / 2
                )
                val screenPoint = googleMap?.projection?.toScreenLocation(midPoint)!!
                canvas.drawText(text, screenPoint.x.toFloat(), screenPoint.y.toFloat(), paint)
            }
        }
    }
}
