package app.simple.positional.ui.panels

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import app.simple.positional.R
import app.simple.positional.activities.subactivity.DirectionsActivity
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.decorations.views.PhysicalRotationImageView
import app.simple.positional.dialogs.app.ErrorDialog
import app.simple.positional.dialogs.compass.CompassCalibration
import app.simple.positional.dialogs.direction.DirectionMenu
import app.simple.positional.extensions.fragment.ScopedFragment
import app.simple.positional.math.Angle.normalizeEulerAngle
import app.simple.positional.math.CompassAzimuth
import app.simple.positional.math.LowPassFilter
import app.simple.positional.math.MathExtensions.round
import app.simple.positional.math.UnitConverter.toFeet
import app.simple.positional.math.UnitConverter.toKilometers
import app.simple.positional.math.UnitConverter.toMiles
import app.simple.positional.math.Vector3
import app.simple.positional.preferences.DirectionPreferences
import app.simple.positional.preferences.GPSPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.DMSConverter
import app.simple.positional.util.HtmlHelper
import app.simple.positional.util.LocationExtension
import app.simple.positional.viewmodels.viewmodel.LocationViewModel
import com.google.android.gms.maps.model.LatLng
import kotlin.math.abs

class Direction : ScopedFragment(), SensorEventListener {

    private lateinit var direction: PhysicalRotationImageView
    private lateinit var dial: PhysicalRotationImageView
    private lateinit var degrees: TextView
    private lateinit var directionDegrees: TextView
    private lateinit var target: TextView
    private lateinit var bearing: TextView
    private lateinit var displacement: TextView
    private lateinit var latitude: TextView
    private lateinit var longitude: TextView
    private lateinit var azimuth: TextView
    private lateinit var menu: DynamicRippleImageButton
    private lateinit var targetSet: DynamicRippleImageButton
    private lateinit var calibrate: DynamicRippleImageButton

    private var calibrationDialog: CompassCalibration? = null
    private var targetLatLng: LatLng? = null
    private var location: Location? = null
    private lateinit var locationViewModel: LocationViewModel

    private val accelerometerReadings = FloatArray(3)
    private val magnetometerReadings = FloatArray(3)
    private val rotation = FloatArray(9)
    private val inclination = FloatArray(9)

    private var accelerometer = Vector3.zero
    private var magnetometer = Vector3.zero

    private var readingsAlpha = 0.03f
    private var rotationAngle = 0f
    private var directionAngle = 0F
    private val degreesPerRadian = 180 / Math.PI
    private val twoTimesPi = 2.0 * Math.PI
    private val degreeSymbol = "\u00B0"

    private var haveAccelerometerSensor = false
    private var haveMagnetometerSensor = false
    private var isGimbalLock = true

    private lateinit var sensorManager: SensorManager
    private lateinit var sensorAccelerometer: Sensor
    private lateinit var sensorMagneticField: Sensor

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_direction, container, false)

        direction = view.findViewById(R.id.direction)
        dial = view.findViewById(R.id.dial)
        degrees = view.findViewById(R.id.compass_degrees)
        directionDegrees = view.findViewById(R.id.degrees)
        target = view.findViewById(R.id.direction_target)
        bearing = view.findViewById(R.id.direction_bearing)
        displacement = view.findViewById(R.id.direction_displacement)
        latitude = view.findViewById(R.id.direction_latitude)
        longitude = view.findViewById(R.id.direction_longitude)
        azimuth = view.findViewById(R.id.direction_compass_azimuth)
        menu = view.findViewById(R.id.direction_menu)
        targetSet = view.findViewById(R.id.direction_target_btn)
        calibrate = view.findViewById(R.id.compass_calibrate)

        locationViewModel = ViewModelProvider(requireActivity())[LocationViewModel::class.java]

        setTargetCoordinates()
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        kotlin.runCatching {
            sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)!!
            sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
            haveMagnetometerSensor = true
            haveAccelerometerSensor = true
        }.getOrElse {
            haveAccelerometerSensor = false
            haveMagnetometerSensor = false

            ErrorDialog.newInstance(getString(R.string.sensor_error))
                    .show(childFragmentManager, "error_dialog")
        }

        setPhysicalProperties()
        setCoordinates()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTargetLabel()
        setTargetCoordinates()

        locationViewModel.getLocation().observe(viewLifecycleOwner) {
            location = it

            directionAngle = LocationExtension.calculateBearingAngle(
                    it.latitude,
                    it.longitude,
                    targetLatLng!!.latitude,
                    targetLatLng!!.longitude).toFloat()

            displacement.text = HtmlHelper.fromHtml(targetDisplacement(LatLng(it.latitude, it.longitude), targetLatLng!!).toString())
        }

        menu.setOnClickListener {
            DirectionMenu.newInstance()
                    .show(childFragmentManager, "direction_menu")
        }

        targetSet.setOnClickListener {
            startActivity(Intent(requireActivity(), DirectionsActivity::class.java))
        }

        calibrate.setOnClickListener {
            CompassCalibration.newInstance()
                    .show(parentFragmentManager, "calibration_dialog")
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

        rotationAngle = if (isGimbalLock) {
            val successfullyCalculatedRotationMatrix =
                    SensorManager.getRotationMatrix(rotation, inclination, accelerometerReadings, magnetometerReadings)

            if (successfullyCalculatedRotationMatrix) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotation, orientation)
                CompassAzimuth.adjustAzimuthForDisplayRotation(
                        ((orientation[0] + twoTimesPi) % twoTimesPi * degreesPerRadian).toFloat(),
                        requireActivity().windowManager).normalizeEulerAngle(inverseResult = false)
            } else {
                0F
            }
        } else {
            CompassAzimuth.calculate(gravity = accelerometer, magneticField = magnetometer,
                    requireActivity().windowManager).normalizeEulerAngle(false)
        }

        dial.rotationUpdate(rotationAngle * -1, true)
        direction.rotationUpdate(directionAngle.minus(rotationAngle).normalizeEulerAngle(false), true)

        degrees.text = StringBuilder().append(abs(dial.rotation.normalizeEulerAngle(true).toInt())).append(degreeSymbol)
        directionDegrees.text = StringBuilder().append(abs(direction.rotation.normalizeEulerAngle(false).toInt())).append(degreeSymbol)
        azimuth.text = HtmlHelper.fromHtml("<b>${getString(R.string.moon_azimuth)}</b> ${degrees.text}")
        bearing.text = HtmlHelper.fromHtml("<b>${getString(R.string.gps_bearing)}</b> ${directionDegrees.text}")
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        if (sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            when (accuracy) {
                SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                    openCalibrationDialog()
                    HtmlHelper.fromHtml("<b>${getString(R.string.magnetometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_unreliable)}")
                }

                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                    openCalibrationDialog()
                    HtmlHelper.fromHtml("<b>${getString(R.string.magnetometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_low)}")
                }

                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                    HtmlHelper.fromHtml("<b>${getString(R.string.magnetometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_medium)}")
                }

                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                    HtmlHelper.fromHtml("<b>${getString(R.string.magnetometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_high)}")
                }

                else -> {
                    HtmlHelper.fromHtml("<b>${getString(R.string.magnetometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_unreliable)}")
                }
            }
        }

        if (sensor.type == Sensor.TYPE_ACCELEROMETER) {
            when (accuracy) {
                SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                    openCalibrationDialog()
                    HtmlHelper.fromHtml("<b>${getString(R.string.accelerometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_unreliable)}")
                }

                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                    openCalibrationDialog()
                    HtmlHelper.fromHtml("<b>${getString(R.string.accelerometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_low)}")
                }

                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                    HtmlHelper.fromHtml("<b>${getString(R.string.accelerometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_medium)}")
                }

                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                    HtmlHelper.fromHtml("<b>${getString(R.string.accelerometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_high)}")
                }

                else -> {
                    HtmlHelper.fromHtml("<b>${getString(R.string.accelerometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_unreliable)}")
                }
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            DirectionPreferences.directionLatitude,
            DirectionPreferences.directionLongitude,
            -> {
                setTargetCoordinates()

                if (location != null) {
                    directionAngle = LocationExtension.calculateBearingAngle(
                            location!!.latitude,
                            location!!.longitude,
                            targetLatLng!!.latitude,
                            targetLatLng!!.longitude).toFloat()
                }

                setCoordinates()
            }

            DirectionPreferences.directionLabel,
            DirectionPreferences.useMapsTarget -> {
                setTargetLabel()
            }

            DirectionPreferences.directionGimbalLock -> {
                isGimbalLock = DirectionPreferences.isGimbalLock()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        register()
    }

    override fun onPause() {
        super.onPause()
        unregister()
    }

    private fun register() {
        if (haveAccelerometerSensor && haveMagnetometerSensor) {
            sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME)
            sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    private fun unregister() {
        if (haveAccelerometerSensor && haveMagnetometerSensor) {
            sensorManager.unregisterListener(this, sensorAccelerometer)
            sensorManager.unregisterListener(this, sensorMagneticField)
        }
    }

    private fun setPhysicalProperties() {
        dial.setPhysical(0.5F, 8F, 5000F)
        direction.setPhysical(1.0F, 8F, 5000F)
    }

    private fun openCalibrationDialog() {
        if (calibrationDialog == null) {
            calibrationDialog = CompassCalibration.newInstance()
            calibrationDialog!!.show(parentFragmentManager, "calibration_dialog")
        }
    }

    private fun setCoordinates() {
        kotlin.runCatching {
            with(DMSConverter.getFormattedCoordinates(targetLatLng!!, requireContext())) {
                latitude.text = HtmlHelper.fromHtml("<b>${getString(R.string.gps_latitude)}</b> ${this[0]}")
                longitude.text = HtmlHelper.fromHtml("<b>${getString(R.string.gps_longitude)}</b> ${this[1]}")
            }
        }
    }

    private fun setTargetCoordinates() {
        targetLatLng = if (DirectionPreferences.isUsingMapsTarget() && GPSPreferences.isTargetMarkerSet()) {
            LatLng(GPSPreferences.getTargetMarkerCoordinates()[0].toDouble(), GPSPreferences.getTargetMarkerCoordinates()[1].toDouble())
        } else {
            LatLng(DirectionPreferences.getTargetCoordinates()[0].toDouble(), DirectionPreferences.getTargetCoordinates()[1].toDouble())
        }
    }

    private fun setTargetLabel() {
        Log.d("Direction", "setTargetLabel: ${DirectionPreferences.getTargetLabel()}")
        if (DirectionPreferences.isUsingMapsTarget() && GPSPreferences.isTargetMarkerSet()) {
            target.text = HtmlHelper.fromHtml("<b>${getString(R.string.target)}:</b> ${getString(R.string.using_maps_target)}")
        } else {
            target.text = HtmlHelper.fromHtml("<b>${getString(R.string.target)}:</b> ${DirectionPreferences.getTargetLabel() ?: getString(R.string.not_available)}")
        }
    }

    private fun targetDisplacement(target: LatLng, current: LatLng): StringBuilder {
        return StringBuilder().also { stringBuilder ->
            stringBuilder.append("<b>${getString(R.string.gps_displacement)} </b>")

            kotlin.runCatching {
                val p0 = LocationExtension.measureDisplacement(arrayOf(target, current))

                if (MainPreferences.isMetric()) {
                    if (p0 < 1000) {
                        stringBuilder.append(p0.round(2))
                        stringBuilder.append(" ")
                        stringBuilder.append(requireContext().getString(R.string.meter))
                    } else {
                        stringBuilder.append(p0.toKilometers().round(2))
                        stringBuilder.append(" ")
                        stringBuilder.append(requireContext().getString(R.string.kilometer))
                    }
                } else {
                    if (p0 < 1000) {
                        stringBuilder.append(p0.toDouble().toFeet().toFloat().round(2))
                        stringBuilder.append(" ")
                        stringBuilder.append(requireContext().getString(R.string.feet))
                    } else {
                        stringBuilder.append(p0.toMiles().round(2))
                        stringBuilder.append(" ")
                        stringBuilder.append(requireContext().getString(R.string.miles))
                    }
                }
            }.getOrElse {
                stringBuilder.append(getString(R.string.not_available))
            }
        }
    }

    companion object {
        fun newInstance(): Direction {
            val args = Bundle()
            val fragment = Direction()
            fragment.arguments = args
            return fragment
        }
    }
}
