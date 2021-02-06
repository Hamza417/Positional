package app.simple.positional.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Process
import androidx.localbroadcastmanager.content.LocalBroadcastManager

/**
 * This service is outdated and not being used and only left here
 * for revision and learning purpose.
 */
@Suppress("unused")
class CompassService : Service(), SensorEventListener {

    private val accelerometerReadings = FloatArray(3)
    private val magnetometerReadings = FloatArray(3)

    private val orientation = FloatArray(3)
    private val rotation = FloatArray(9)
    private val inclination = FloatArray(9)

    private val readingsAlpha = 0.03f
    private val twoPI = 2.0 * Math.PI
    private val degreesPerRadian = 180 / Math.PI
    private var rotationAngle = 0f
    private var y: Float = 0f
    private var x: Float = 0f
    private var _xDelta = 0
    private var _yDelta = 0

    private lateinit var sensorManager: SensorManager
    private lateinit var sensorAccelerometer: Sensor
    private lateinit var sensorMagneticField: Sensor

    private lateinit var mSensorThread: HandlerThread
    private lateinit var mSensorHandler: Handler


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    /**
     *  Thread priority [Process.THREAD_PRIORITY_FOREGROUND] is used as this thread fetches the sensor value and
     *  simultaneously sending the broadcast to the UI that is updating the compass direction but needs not to run
     *  if the application is minimized or potentially closed
     */
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        sensorManager = baseContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        mSensorThread = HandlerThread("Sensor Thread", Process.THREAD_PRIORITY_FOREGROUND)
        mSensorThread.start()
        mSensorHandler = Handler(mSensorThread.looper)
        register()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregister()
    }

    /**
     *
     */
    override  fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> smoothAndSetReadings(accelerometerReadings, event.values)
            Sensor.TYPE_MAGNETIC_FIELD -> smoothAndSetReadings(magnetometerReadings, event.values)
        }

        val successfullyCalculatedRotationMatrix = SensorManager.getRotationMatrix(this.rotation, inclination, accelerometerReadings, magnetometerReadings)

        if (successfullyCalculatedRotationMatrix) {
            SensorManager.getOrientation(this.rotation, orientation)

            rotationAngle = -(((orientation[0] + twoPI) % twoPI * degreesPerRadian).toFloat() + 360) % 360

            Intent().also { intent ->
                intent.action = "compass_update"
                intent.putExtra("rotation", rotationAngle)
                LocalBroadcastManager.getInstance(baseContext).sendBroadcast(intent)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    private fun register() {
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME, mSensorHandler)
        sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_GAME, mSensorHandler)
    }

    private fun unregister() {
        mSensorThread.quitSafely()
        sensorManager.unregisterListener(this, sensorAccelerometer)
        sensorManager.unregisterListener(this, sensorMagneticField)
    }

    private fun smoothAndSetReadings(readings: FloatArray, newReadings: FloatArray) {
        readings[0] = readingsAlpha * newReadings[0] + (1 - readingsAlpha) * readings[0]
        readings[1] = readingsAlpha * newReadings[1] + (1 - readingsAlpha) * readings[1]
        readings[2] = readingsAlpha * newReadings[2] + (1 - readingsAlpha) * readings[2]
    }
}