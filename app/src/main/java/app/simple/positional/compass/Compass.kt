package app.simple.positional.compass

import android.content.Context
import android.hardware.*
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class Compass(context: Context) {

    private val locationClient = FusedLocationProviderClient(context.applicationContext)
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    val accelerometerAccuracy: Flow<CompassAccuracy>
        get() = callbackFlow<CompassAccuracy> {
            if (!hasAccelerometer) return@callbackFlow
            val listener = object : SensorEventListener {
                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                    offer(when (accuracy) {
                        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> CompassAccuracy.HIGH
                        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> CompassAccuracy.MEDIUM
                        SensorManager.SENSOR_STATUS_ACCURACY_LOW -> CompassAccuracy.LOW
                        SensorManager.SENSOR_STATUS_UNRELIABLE -> CompassAccuracy.UNRELIABLE
                        else -> CompassAccuracy.UNUSABLE
                    })
                }

                override fun onSensorChanged(event: SensorEvent) {
                    // Don't do anything
                }
            }
            sensorManager.registerListener(listener, accelerometer, SENSOR_DELAY)
            awaitClose { sensorManager.unregisterListener(listener) }
        }.conflate()

    val azimuth: Flow<Float>
        get() = callbackFlow<Float> {
            if (!hasAccelerometer || !hasMagnetometer) return@callbackFlow
            val accelerometerReadings = FloatArray(3)
            val magnetometerReadings = FloatArray(3)
            val rotation = FloatArray(9)
            val inclination = FloatArray(9)
            val listener = object : SensorEventListener {
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    // Don't do anything
                }

                override fun onSensorChanged(event: SensorEvent) {
                    when (event.sensor) {
                        accelerometer -> smoothAndSetReadings(accelerometerReadings, event.values)
                        magnetometer -> smoothAndSetReadings(magnetometerReadings, event.values)
                    }
                    val successfullyCalculatedRotationMatrix = SensorManager.getRotationMatrix(
                            rotation,
                            inclination,
                            accelerometerReadings,
                            magnetometerReadings
                    )
                    if (successfullyCalculatedRotationMatrix) {
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(rotation, orientation)
                        offer((((orientation[0] + TWO_PI) % TWO_PI) * DEGREES_PER_RADIAN).toFloat())
                    }
                }
            }
            sensorManager.registerListener(listener, accelerometer, SENSOR_DELAY)
            sensorManager.registerListener(listener, magnetometer, SENSOR_DELAY)
            awaitClose { sensorManager.unregisterListener(listener) }
        }.conflate()

    val hasAccelerometer: Boolean
        get() = accelerometer != null

    val hasMagnetometer: Boolean
        get() = magnetometer != null

    val magneticDeclination: Flow<Float>
        get() = callbackFlow<Float> {
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    val location = locationResult?.lastLocation ?: return

                    val declination = GeomagneticField(
                            location.latitude.toFloat(),
                            location.longitude.toFloat(),
                            location.altitude.toFloat(),
                            location.time
                    ).declination
                    offer(declination)
                }

                override fun onLocationAvailability(locationAvailability: LocationAvailability) {

                }
            }

            try {
                val locationRequest = LocationRequest.create()
                        .setPriority(LOCATION_UPDATE_PRIORITY)
                        .setInterval(LOCATION_UPDATE_INTERVAL_MS)

                locationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                )
            } catch (e: SecurityException) {

            }

            awaitClose {

                locationClient.removeLocationUpdates(locationCallback)
            }
        }.distinctUntilChanged()

    val magnetometerAccuracy: Flow<CompassAccuracy>
        get() = callbackFlow<CompassAccuracy> {
            if (!hasMagnetometer) return@callbackFlow
            val listener = object : SensorEventListener {
                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                    offer(when (accuracy) {
                        SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> CompassAccuracy.HIGH
                        SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> CompassAccuracy.MEDIUM
                        SensorManager.SENSOR_STATUS_ACCURACY_LOW -> CompassAccuracy.LOW
                        SensorManager.SENSOR_STATUS_UNRELIABLE -> CompassAccuracy.UNRELIABLE
                        else -> CompassAccuracy.UNUSABLE
                    })
                }

                override fun onSensorChanged(event: SensorEvent) {
                    // Don't do anything
                }
            }
            sensorManager.registerListener(listener, magnetometer, SENSOR_DELAY)
            awaitClose { sensorManager.unregisterListener(listener) }
        }.conflate()

    fun smoothAndSetReadings(readings: FloatArray, newReadings: FloatArray) {
        readings[0] = READINGS_ALPHA * newReadings[0] + (1 - READINGS_ALPHA) * readings[0]
        readings[1] = READINGS_ALPHA * newReadings[1] + (1 - READINGS_ALPHA) * readings[1]
        readings[2] = READINGS_ALPHA * newReadings[2] + (1 - READINGS_ALPHA) * readings[2]
    }

    companion object {
        private const val DEGREES_PER_RADIAN = 180 / Math.PI
        private const val LOCATION_UPDATE_INTERVAL_MS = 300_000L // 5 minutes
        private const val LOCATION_UPDATE_PRIORITY = LocationRequest.PRIORITY_LOW_POWER
        private const val READINGS_ALPHA = 0.03f
        private const val SENSOR_DELAY = SensorManager.SENSOR_DELAY_GAME
        private const val TWO_PI = 2.0 * Math.PI
    }
}