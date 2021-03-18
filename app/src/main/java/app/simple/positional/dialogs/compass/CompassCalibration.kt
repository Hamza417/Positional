package app.simple.positional.dialogs.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.simple.positional.R
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.util.HtmlHelper.fromHtml

class CompassCalibration : CustomBottomSheetDialogFragment(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var sensorAccelerometer: Sensor
    private lateinit var sensorMagneticField: Sensor
    private val handlerThread = Handler(Looper.getMainLooper())

    private var haveAccelerometerSensor = false
    private var haveMagnetometerSensor = false
    private var someValue = 1

    private lateinit var calibrateAccuracy: TextView

    fun newInstance(): CompassCalibration {
        return CompassCalibration()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.dialog_compass_calibration, container, false)
        calibrateAccuracy = v.findViewById(R.id.calibrate_accuracy)

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null && sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            haveMagnetometerSensor = true
            haveAccelerometerSensor = true
        } else {
            haveAccelerometerSensor = false
            haveMagnetometerSensor = false
        }

        return v
    }

    override fun onResume() {
        super.onResume()
        register()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregister()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        /* no-op */
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor == sensorAccelerometer || sensor == sensorMagneticField) {
            when (accuracy) {
                SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                    calibrateAccuracy.text = fromHtml("Accuracy: <b>Unreliable, immediate calibration required</b>")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                    calibrateAccuracy.text = fromHtml("Accuracy: <b>Low, calibration required</b>")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                    calibrateAccuracy.text = fromHtml("Accuracy: <b>Medium</b>")
                    handlerThread.removeCallbacks(runnable)
                    handlerThread.post(runnable)
                }
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                    calibrateAccuracy.text = fromHtml("Accuracy: <b>High</b>")
                    handlerThread.removeCallbacks(runnable)
                    handlerThread.post(runnable)
                }
            }
        }
    }

    private fun register() {
        if (context == null) return
        if (haveAccelerometerSensor && haveMagnetometerSensor) {
            sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME)
            sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    private fun unregister() {
        if (context == null) return
        if (haveAccelerometerSensor && haveMagnetometerSensor) {
            sensorManager.unregisterListener(this, sensorAccelerometer)
            sensorManager.unregisterListener(this, sensorMagneticField)
        }
        handlerThread.removeCallbacks(runnable)
    }

    private val runnable = object : Runnable {
        override fun run() {
            if (someValue == 0) {
                this@CompassCalibration.dialog?.dismiss()
            } else {
                someValue -= 1
            }
            handlerThread.postDelayed(this, 3000)
        }
    }
}