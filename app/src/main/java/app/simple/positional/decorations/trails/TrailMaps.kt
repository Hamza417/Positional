package app.simple.positional.decorations.trails

import android.animation.ValueAnimator
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import app.simple.positional.R
import app.simple.positional.constants.TrailIcons
import app.simple.positional.decorations.utils.CircleUtils
import app.simple.positional.decorations.utils.MarkerUtils
import app.simple.positional.extensions.maps.CustomMaps
import app.simple.positional.math.CompassAzimuth
import app.simple.positional.math.LowPassFilter
import app.simple.positional.math.Vector3
import app.simple.positional.model.TrailPoint
import app.simple.positional.preferences.TrailPreferences
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
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrailMaps(context: Context, attributeSet: AttributeSet) : CustomMaps(context, attributeSet),
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
    private var sensorManager: SensorManager
    private lateinit var sensorAccelerometer: Sensor
    private lateinit var sensorMagneticField: Sensor

    private var markerBitmap: Bitmap? = null
    private var marker: Marker? = null
    private var circle: Circle? = null
    private var markerAnimator: ValueAnimator? = null
    private var circleAnimator: ValueAnimator? = null

    private val currentPolyline = arrayListOf<LatLng>()
    private val flagMarkers = arrayListOf<Marker>()
    private val polylines = arrayListOf<Polyline>()
    private var trailData = arrayListOf<TrailPoint>()

    private var isWrapped = false
    private var isFirstLocation = true
    private var isCompassRotation = true
    private var lastZoom = 20F
    private var lastTilt = 0F
    private val incrementFactor = 2
    private var polylineOptions: PolylineOptions? = null

    init {
        polylineOptions = PolylineOptions()
            .width(10f)
            .jointType(JointType.ROUND)
            .color(context.resolveAttrColor(R.attr.colorAppAccent))
            .geodesic(TrailPreferences.isTrailGeodesic())

        latLng = LatLng(lastLatitude, lastLongitude)
        isCompassRotation = TrailPreferences.isCompassRotation()
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
        setMapStyle(TrailPreferences.isLabelOn(), TrailPreferences.isSatelliteOn(), TrailPreferences.getHighContrastMap())
        setBuildings(TrailPreferences.getShowBuildingsOnMap())

        if (location.isNotNull()) {
            setFirstLocation(location)
        }

        if (!TrailPreferences.arePolylinesWrapped()) {
            this.googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(
                latLng!!,
                TrailPreferences.getMapZoom(),
                TrailPreferences.getMapTilt(),
                TrailPreferences.getMapBearing())))
        }

        this.googleMap?.setOnCameraIdleListener {
            TrailPreferences.setMapZoom(this.googleMap?.cameraPosition!!.zoom)
            TrailPreferences.setMapTilt(this.googleMap?.cameraPosition!!.tilt)
            TrailPreferences.setMapBearing(this.googleMap?.cameraPosition!!.bearing)
            viewHandler.removeCallbacks(mapMoved)
            viewHandler.postDelayed(mapMoved, autoCenterDelay)
        }

        this.googleMap?.setOnCameraMoveListener {
            viewHandler.removeCallbacks(mapMoved)
        }

        mapsCallbacks?.onMapInitialized()
        registerSensors()
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
        viewHandler.removeCallbacksAndMessages(null)
        viewHandler.removeCallbacks(mapMoved)
    }

    fun clear() {
        polylines.clear()
        flagMarkers.clear()
        polylineOptions!!.points.clear()
        googleMap?.clear()
        invalidate()
    }

    fun addMarker(latLng: LatLng) {
        launch {
            withContext(Dispatchers.Main) {
                if (context.isNotNull() && googleMap.isNotNull())
                    markerBitmap = if (location.isNotNull()) {
                        if (TrailPreferences.isCompassRotation()) {
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
                            .rotation(if (TrailPreferences.isCompassRotation()) rotationAngle else location?.bearing
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

    private fun updatePolylines(arrayList: ArrayList<TrailPoint>) {
        googleMap?.clear()
        polylines.clear()
        currentPolyline.clear()
        flagMarkers.clear()
        polylineOptions?.points?.clear()

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
            polylineOptions?.add(latLng)
            polylines.add(googleMap?.addPolyline(polylineOptions!!)!!)
        }

        invalidate()

        polylineOptions?.startCap(CustomCap(BitmapDescriptorFactory.fromBitmap(R.drawable.ic_trail_start.toBitmap(context, 30))))
        polylineOptions?.endCap(CustomCap(BitmapDescriptorFactory.fromBitmap(R.drawable.seekbar_thumb.toBitmap(context, 30))))

        mapsCallbacks?.onLineCountChanged(polylineOptions!!.points.size)

        if (TrailPreferences.arePolylinesWrapped()) {
            wrap(false)
        }
    }

    fun addPolylines(arrayList: ArrayList<TrailPoint>) {
        for (i in polylines) {
            i.remove()
        }

        for (i in flagMarkers) {
            i.remove()
        }

        currentPolyline.clear()
        flagMarkers.clear()
        trailData.clear()
        polylineOptions?.points?.clear()

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
            polylineOptions?.add(latLng)
            polylines.add(googleMap?.addPolyline(polylineOptions!!)!!)
        }

        invalidate()

        polylineOptions?.startCap(CustomCap(BitmapDescriptorFactory.fromBitmap(R.drawable.ic_trail_start.toBitmap(context, 30))))
        polylineOptions?.endCap(CustomCap(BitmapDescriptorFactory.fromBitmap(R.drawable.seekbar_thumb.toBitmap(context, 30))))

        trailData.addAll(arrayList)
        mapsCallbacks?.onLineCountChanged(polylineOptions!!.points.size)

        if (TrailPreferences.arePolylinesWrapped()) {
            wrap(false)
        }
    }

    fun addPolyline(trailPoint: TrailPoint) {
        val latLng = LatLng(trailPoint.latitude, trailPoint.longitude)

        currentPolyline.add(latLng)

        val marker = googleMap?.addMarker(
            MarkerOptions()
                .position(latLng)
                .icon(BitmapDescriptorFactory.fromBitmap(
                    TrailIcons.icons[trailPoint.iconPosition]
                        .toBitmap(context, 50))))

        this.trailData.add(trailPoint)
        flagMarkers.add(marker!!)
        polylineOptions?.add(latLng)
        polylines.add(googleMap?.addPolyline(polylineOptions!!)!!)

        mapsCallbacks?.onLineCountChanged(polylineOptions!!.points.size)

        invalidate()

        if (TrailPreferences.arePolylinesWrapped()) {
            wrap(true)
        } else {
            moveMapCamera(latLng, TrailPreferences.getMapZoom(), TrailPreferences.getMapTilt())
        }
    }

    fun removePolyline() {
        polylines.lastOrNull()?.remove()
        flagMarkers.lastOrNull()?.remove()
        currentPolyline.removeLastOrNull()
        polylineOptions?.points?.removeLastOrNull()
        mapsCallbacks?.onLineDeleted(trailData.lastOrNull())
        mapsCallbacks?.onLineCountChanged(polylineOptions!!.points.size)
        trailData.removeLastOrNull()

        kotlin.runCatching {
            if (TrailPreferences.arePolylinesWrapped()) {
                wrap(true)
            } else {
                with(trailData.lastOrNull()!!) {
                    moveMapCamera(LatLng(latitude, longitude), TrailPreferences.getMapZoom(), TrailPreferences.getMapTilt())
                }
            }
        }.getOrElse {
            moveMapCamera(latLng!!, TrailPreferences.getMapZoom(), TrailPreferences.getMapTilt())
        }

        invalidate()
    }

    fun wrapUnwrap() {
        if (isWrapped) {
            moveMapCamera(latLng!!, lastZoom, lastTilt)
        } else {
            wrap(true)
        }
    }

    private fun wrap(animate: Boolean) {
        kotlin.runCatching {
            lastZoom = TrailPreferences.getMapZoom()
            lastTilt = TrailPreferences.getMapTilt()

            val builder = LatLngBounds.Builder()
            for (latLng in currentPolyline) {
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

            TrailPreferences.setWrapStatus(true)
            isWrapped = true
        }.onFailure {
            isWrapped = TrailPreferences.arePolylinesWrapped()
        }
    }

    fun resetCamera(zoom: Float) {
        if (location != null) {
            moveMapCamera(LatLng(location!!.latitude, location!!.longitude), zoom, TrailPreferences.getMapTilt())
        }
    }

    fun moveMapCamera(latLng: LatLng, zoom: Float, tilt: Float) {
        if (googleMap.isNull() && latLng.isNull()) return

        animateCamera(latLng, zoom, tilt, TrailPreferences.getMapBearing())

        isWrapped = false
        TrailPreferences.setWrapStatus(false)
    }

    fun setFirstLocation(location: Location?) {
        if (googleMap.isNotNull() && isFirstLocation) {
            this.location = location

            with(LatLng(location!!.latitude, location.longitude)) {
                addMarker(this)
                googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(
                    this,
                    TrailPreferences.getMapZoom(),
                    TrailPreferences.getMapTilt(),
                    TrailPreferences.getMapBearing())))

                latLng = this
                isFirstLocation = false
            }
        }
    }

    private val mapMoved = object : Runnable {
        override fun run() {
            if (TrailPreferences.getMapAutoCenter() && !isWrapped) {
                val latLng = if (location.isNotNull()) {
                    LatLng(location!!.latitude, location!!.longitude)
                } else {
                    LatLng(lastLatitude, lastLongitude)
                }

                moveMapCamera(latLng, TrailPreferences.getMapZoom(), TrailPreferences.getMapTilt())
            }
            viewHandler.postDelayed(this, autoCenterDelay)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            TrailPreferences.trailHighContrastMap,
            TrailPreferences.trailLabelMode,
            TrailPreferences.trailSatellite -> {
                setMapStyle(TrailPreferences.isLabelOn(), TrailPreferences.isSatelliteOn(), TrailPreferences.getHighContrastMap())
            }

            TrailPreferences.trailShowBuilding -> {
                setBuildings(TrailPreferences.getShowBuildingsOnMap())
            }

            TrailPreferences.geodesic -> {
                polylineOptions!!.geodesic(TrailPreferences.isTrailGeodesic())
                updatePolylines(trailData)
                invalidate()
            }

            TrailPreferences.mapAutoCenter -> {
                viewHandler.removeCallbacks(mapMoved)
                viewHandler.post(mapMoved)
            }

            TrailPreferences.compass -> {
                if (TrailPreferences.isCompassRotation()) {
                    isCompassRotation = true
                    registerSensors()
                } else {
                    isCompassRotation = false
                    unregisterSensors()
                }
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
            CompassAzimuth.calculate(gravity = accelerometer, magneticField = magnetometer, windowManager)

        if (isCompassRotation) {
            marker?.rotation = rotationAngle
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        accuracy = p1
    }

    private fun registerSensors() {
        if (haveAccelerometerSensor && haveMagnetometerSensor) {
            unregisterSensors()
            if (TrailPreferences.isCompassRotation()) {
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
}
