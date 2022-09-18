package app.simple.positional.dialogs.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.simple.positional.R
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.decorations.views.CustomDialogFragment
import app.simple.positional.util.HtmlHelper.fromHtml

class CompassCalibration : CustomDialogFragment(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var sensorAccelerometer: Sensor
    private lateinit var sensorMagneticField: Sensor

    private var haveAccelerometerSensor = false
    private var haveMagnetometerSensor = false

    private lateinit var magAccuracy: TextView
    private lateinit var accAccuracy: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.dialog_compass_calibration, container, false)
        magAccuracy = v.findViewById(R.id.mag_accuracy)
        accAccuracy = v.findViewById(R.id.acc_accuracy)

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
        if (sensor!!.type == Sensor.TYPE_MAGNETIC_FIELD) {
            magAccuracy.text = when (accuracy) {
                SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                    fromHtml("<b>${getString(R.string.magnetometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_unreliable)}")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                    fromHtml("<b>${getString(R.string.magnetometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_low)}")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                    fromHtml("<b>${getString(R.string.magnetometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_medium)}")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                    fromHtml("<b>${getString(R.string.magnetometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_high)}")
                }
                else -> {
                    fromHtml("<b>${getString(R.string.magnetometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_unreliable)}")
                }
            }
        }

        if (sensor.type == Sensor.TYPE_ACCELEROMETER) {
            accAccuracy.text = when (accuracy) {
                SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                    fromHtml("<b>${getString(R.string.accelerometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_unreliable)}")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                    fromHtml("<b>${getString(R.string.accelerometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_low)}")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                    fromHtml("<b>${getString(R.string.accelerometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_medium)}")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                    fromHtml("<b>${getString(R.string.accelerometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_high)}")
                }
                else -> {
                    fromHtml("<b>${getString(R.string.accelerometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_unreliable)}")
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
    }

    companion object {
        fun newInstance(): CompassCalibration {
            return CompassCalibration()
        }
    }
}
