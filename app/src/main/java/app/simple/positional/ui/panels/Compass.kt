package app.simple.positional.ui.panels

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spanned
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.NestedScrollView
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.constants.CompassBloom.compassBloomRes
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.decorations.views.PhysicalRotationImageView
import app.simple.positional.dialogs.app.ErrorDialog
import app.simple.positional.dialogs.compass.CompassCalibration
import app.simple.positional.dialogs.compass.CompassMenu
import app.simple.positional.extensions.fragment.ScopedFragment
import app.simple.positional.math.Angle.getAngle
import app.simple.positional.math.Angle.normalizeEulerAngle
import app.simple.positional.math.CompassAzimuth
import app.simple.positional.math.LowPassFilter.smoothAndSetReadings
import app.simple.positional.math.MathExtensions.round
import app.simple.positional.math.Vector3
import app.simple.positional.preferences.CompassPreferences
import app.simple.positional.util.ColorUtils.animateColorChange
import app.simple.positional.util.ColorUtils.resolveAttrColor
import app.simple.positional.util.ConditionUtils.invert
import app.simple.positional.util.Direction.getDirectionCodeFromAzimuth
import app.simple.positional.util.Direction.getDirectionNameFromAzimuth
import app.simple.positional.util.HtmlHelper.fromHtml
import app.simple.positional.util.ImageLoader.loadImage
import app.simple.positional.util.LocaleHelper
import app.simple.positional.viewmodels.viewmodel.LocationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

class Compass : ScopedFragment(), SensorEventListener {

    private var handler = Handler(Looper.getMainLooper())
    private var objectAnimator: ObjectAnimator? = null
    private var calibrationDialog: CompassCalibration? = null
    private lateinit var locationViewModel: LocationViewModel

    private val accelerometerReadings = FloatArray(3)
    private val magnetometerReadings = FloatArray(3)
    private val rotation = FloatArray(9)
    private val inclination = FloatArray(9)

    private var haveAccelerometerSensor = false
    private var haveMagnetometerSensor = false
    private var showDirectionCode = true
    private var isUserRotatingDial = false
    private var isAnimated = true
    private var isGimbalLock = true

    private var accelerometer = Vector3.zero
    private var magnetometer = Vector3.zero

    private var readingsAlpha = 0.03f
    private var rotationAngle = 0f
    private var flowerBloom = 0
    private var lastDialAngle = 0F
    private var startAngle = 0F
    private val degreesPerRadian = 180 / Math.PI
    private val twoTimesPi = 2.0 * Math.PI
    private val degreeSymbol = "\u00B0"

    private lateinit var sensorManager: SensorManager
    private lateinit var sensorAccelerometer: Sensor
    private lateinit var sensorMagneticField: Sensor

    private lateinit var compassScrollView: NestedScrollView
    private lateinit var accuracyAccelerometer: TextView
    private lateinit var accuracyMagnetometer: TextView
    private lateinit var accelerometerX: TextView
    private lateinit var accelerometerY: TextView
    private lateinit var accelerometerZ: TextView
    private lateinit var magnetometerX: TextView
    private lateinit var magnetometerY: TextView
    private lateinit var magnetometerZ: TextView
    private lateinit var inclinationTextView: TextView
    private lateinit var declination: TextView
    private lateinit var fieldStrength: TextView
    private lateinit var degrees: TextView
    private lateinit var direction: TextView

    private lateinit var dial: PhysicalRotationImageView
    private lateinit var flowerOne: PhysicalRotationImageView
    private lateinit var flowerTwo: PhysicalRotationImageView
    private lateinit var flowerThree: PhysicalRotationImageView
    private lateinit var flowerFour: PhysicalRotationImageView

    private lateinit var menu: DynamicRippleImageButton
    private lateinit var calibrate: DynamicRippleImageButton
    private lateinit var dialContainer: FrameLayout
    private lateinit var toolbar: ConstraintLayout

    @SuppressLint("WrongViewCast")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_compass, container, false)

        locationViewModel = ViewModelProvider(requireActivity())[LocationViewModel::class.java]

        compassScrollView = view.findViewById(R.id.compass_scroll_view)
        accuracyAccelerometer = view.findViewById(R.id.compass_accuracy_accelerometer)
        accuracyMagnetometer = view.findViewById(R.id.compass_accuracy_magnetometer)
        accelerometerX = view.findViewById(R.id.accelerometer_x)
        accelerometerY = view.findViewById(R.id.accelerometer_y)
        accelerometerZ = view.findViewById(R.id.accelerometer_z)
        magnetometerX = view.findViewById(R.id.magnetometer_x)
        magnetometerY = view.findViewById(R.id.magnetometer_y)
        magnetometerZ = view.findViewById(R.id.magnetometer_z)
        inclinationTextView = view.findViewById(R.id.compass_inclination)
        declination = view.findViewById(R.id.compass_declination)
        fieldStrength = view.findViewById(R.id.compass_field_strength)
        degrees = view.findViewById(R.id.degrees)
        direction = view.findViewById(R.id.direction)

        dial = view.findViewById(R.id.dial)
        flowerOne = view.findViewById(R.id.flower_one)
        flowerTwo = view.findViewById(R.id.flower_two)
        flowerThree = view.findViewById(R.id.flower_three)
        flowerFour = view.findViewById(R.id.flower_four)

        menu = view.findViewById(R.id.compass_menu)
        calibrate = view.findViewById(R.id.compass_calibrate)
        dialContainer = view.findViewById(R.id.dial_container)
        toolbar = view.findViewById(R.id.compass_appbar)

        showDirectionCode = CompassPreferences.getDirectionCode()
        isGimbalLock = CompassPreferences.isUsingGimbalLock()

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

        flowerBloom = CompassPreferences.getFlowerBloomTheme()
        isAnimated = CompassPreferences.isUsingPhysicalProperties()
        setPhysicalProperties()
        setFlower(CompassPreferences.isFlowerBloomOn())

        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadImage(R.drawable.compass_dial, dial, requireContext(), 0)

        dialContainer.setOnTouchListener(MyOnTouchListener())

        menu.setOnClickListener {
            CompassMenu().show(parentFragmentManager, "compass_menu")
        }

        calibrate.setOnClickListener {
            CompassCalibration.newInstance()
                    .show(parentFragmentManager, "calibration_dialog")
        }

        locationViewModel.getLocation().observe(viewLifecycleOwner) { location ->
            var declination: Spanned
            var inclination: Spanned
            var fieldStrength: Spanned

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                val geomagneticField = GeomagneticField(
                        location.latitude.toFloat(),
                        location.longitude.toFloat(),
                        location.altitude.toFloat(),
                        location.time
                )

                declination = fromHtml("<b>${getString(R.string.compass_declination)}</b> ${round(geomagneticField.declination.toDouble(), 2)}$degreeSymbol")
                inclination = fromHtml("<b>${getString(R.string.compass_inclination)}</b> ${round(geomagneticField.inclination.toDouble(), 2)}$degreeSymbol")
                fieldStrength = fromHtml("<b>${getString(R.string.compass_field_strength)}</b> ${round(geomagneticField.fieldStrength.toDouble(), 2)} nT")

                withContext(Dispatchers.Main) {
                    this@Compass.declination.text = declination
                    this@Compass.inclinationTextView.text = inclination
                    this@Compass.fieldStrength.text = fieldStrength
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        register()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(compassDialAnimationRunnable)
        objectAnimator?.removeAllListeners()
        objectAnimator?.cancel()
        dial.clearAnimation()
        handler.removeCallbacksAndMessages(null)
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

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                smoothAndSetReadings(accelerometerReadings, event.values, readingsAlpha)
                accelerometer = Vector3(accelerometerReadings[0], accelerometerReadings[1], accelerometerReadings[2])
            }

            Sensor.TYPE_MAGNETIC_FIELD -> {
                smoothAndSetReadings(magnetometerReadings, event.values, readingsAlpha)
                magnetometer = Vector3(magnetometerReadings[0], magnetometerReadings[1], magnetometerReadings[2])
            }
        }

        val angle = if (isGimbalLock) {
            val successfullyCalculatedRotationMatrix =
                    SensorManager.getRotationMatrix(rotation, inclination, accelerometerReadings, magnetometerReadings)

            if (successfullyCalculatedRotationMatrix) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotation, orientation)
                CompassAzimuth.adjustAzimuthForDisplayRotation(
                        ((orientation[0] + twoTimesPi) % twoTimesPi * degreesPerRadian).toFloat(), requireActivity().windowManager)
            } else {
                0F
            }
        } else {
            CompassAzimuth.calculate(gravity = accelerometer, magneticField = magnetometer, requireActivity().windowManager)
        }

        run {
            if (BuildConfig.DEBUG.invert()) { // ANR on debug builds
                accelerometerX.text = fromHtml("<b>X:</b> ${round(accelerometerReadings[0].toDouble(), 3)}")
                accelerometerY.text = fromHtml("<b>Y:</b> ${round(accelerometerReadings[1].toDouble(), 3)}")
                accelerometerZ.text = fromHtml("<b>Z:</b> ${round(accelerometerReadings[2].toDouble(), 3)}")

                magnetometerX.text = fromHtml("<b>X:</b> ${round(magnetometerReadings[0].toDouble(), 3)}")
                magnetometerY.text = fromHtml("<b>Y:</b> ${round(magnetometerReadings[1].toDouble(), 3)}")
                magnetometerZ.text = fromHtml("<b>Z:</b> ${round(magnetometerReadings[2].toDouble(), 3)}")
            }
        }

        if (!isUserRotatingDial) {
            rotationAngle = angle.normalizeEulerAngle(false)
            viewRotation(rotationAngle, isAnimated)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        if (sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            accuracyMagnetometer.text = when (accuracy) {
                SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                    openCalibrationDialog()
                    fromHtml("<b>${getString(R.string.magnetometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_unreliable)}")
                }

                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                    openCalibrationDialog()
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
            accuracyAccelerometer.text = when (accuracy) {
                SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                    openCalibrationDialog()
                    fromHtml("<b>${getString(R.string.accelerometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_unreliable)}")
                }

                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                    openCalibrationDialog()
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

    private fun viewRotation(rotationAngle: Float, animate: Boolean) {
        dial.rotationUpdate(rotationAngle * -1, animate)

        if (CompassPreferences.isFlowerBloomOn()) {
            flowerOne.rotationUpdate((rotationAngle * 2).normalizeEulerAngle(false), animate)
            flowerTwo.rotationUpdate((rotationAngle * -3 + 45).normalizeEulerAngle(false), animate)
            flowerThree.rotationUpdate((rotationAngle * 1 + 90).normalizeEulerAngle(false), animate)
            flowerFour.rotationUpdate((rotationAngle * -4 + 135).normalizeEulerAngle(false), animate)
        }

        degrees.text = StringBuilder().append(abs(dial.rotation.normalizeEulerAngle(true).toInt())).append(degreeSymbol)

        direction.text = if (showDirectionCode) {
            getDirectionCodeFromAzimuth(requireContext(), azimuth = rotationAngle.toDouble()).uppercase(LocaleHelper.getAppLocale())
        } else {
            getDirectionNameFromAzimuth(requireContext(), azimuth = rotationAngle.toDouble()).uppercase(LocaleHelper.getAppLocale())
        }
    }

    private fun setPhysicalProperties() {
        val inertia = CompassPreferences.getRotationalInertia()
        val damping = CompassPreferences.getDampingCoefficient()
        val magnetic = CompassPreferences.getMagneticCoefficient()

        dial.setPhysical(inertia, damping, magnetic)
        flowerOne.setPhysical(inertia, damping, magnetic)
        flowerTwo.setPhysical(inertia, damping, magnetic)
        flowerThree.setPhysical(inertia, damping, magnetic)
        flowerFour.setPhysical(inertia, damping, magnetic)
    }

    private fun setFlower(value: Boolean) {
        CompassPreferences.setFlowerBloom(value)
        val x = compassBloomRes[CompassPreferences.getFlowerBloomTheme()]
        if (value) {
            loadImage(x, flowerOne, requireContext(), 0)
            loadImage(x, flowerTwo, requireContext(), 50)
            loadImage(x, flowerThree, requireContext(), 100)
            loadImage(x, flowerFour, requireContext(), 150)
            degrees.animateColorChange(Color.parseColor("#ffffff"))
        } else {
            loadImage(0, flowerOne, requireContext(), 150)
            loadImage(0, flowerTwo, requireContext(), 100)
            loadImage(0, flowerThree, requireContext(), 50)
            loadImage(0, flowerFour, requireContext(), 0)
            degrees.animateColorChange(requireContext().resolveAttrColor(R.attr.colorAppAccent))
        }
    }

    private fun setFlowerTheme(value: Int) {
        CompassPreferences.setFlowerBloom(value)
        setFlower(value = CompassPreferences.isFlowerBloomOn())
        flowerBloom = value
    }

    private inner class MyOnTouchListener : View.OnTouchListener {

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    compassScrollView.requestDisallowInterceptTouchEvent(true)
                    isUserRotatingDial = true
                    objectAnimator?.removeAllListeners()
                    objectAnimator?.cancel()
                    dial.clearAnimation()
                    flowerOne.clearAnimation()
                    flowerTwo.clearAnimation()
                    flowerThree.clearAnimation()
                    flowerFour.clearAnimation()
                    handler.removeCallbacks(compassDialAnimationRunnable)
                    lastDialAngle = dial.rotation //if (dial.rotation < -180) abs(dial.rotation) else dial.rotation
                    startAngle = getAngle(event.x.toDouble(), event.y.toDouble(), dialContainer.width.toFloat(), dialContainer.height.toFloat())
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    val currentAngle = getAngle(event.x.toDouble(), event.y.toDouble(), dialContainer.width.toFloat(), dialContainer.height.toFloat())
                    val finalAngle = currentAngle - startAngle + lastDialAngle
                    viewRotation(abs(finalAngle.normalizeEulerAngle(inverseResult = true)), animate = false)
                    return true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    compassScrollView.requestDisallowInterceptTouchEvent(false)
                    handler.postDelayed(compassDialAnimationRunnable, 1000)
                    return true
                }
            }
            return true
        }
    }

    private val compassDialAnimationRunnable = Runnable {
        if (isAnimated) {
            dial.clearAnimation()
            isUserRotatingDial = false
        } else {
            objectAnimator = ObjectAnimator.ofFloat(dial, "rotation", dial.rotation, rotationAngle * -1)
            objectAnimator!!.duration = 1000L
            objectAnimator!!.interpolator = LinearOutSlowInInterpolator()
            objectAnimator!!.setAutoCancel(true)
            objectAnimator!!.addUpdateListener { animation -> viewRotation(abs(animation.getAnimatedValue("rotation") as Float), false) }
            objectAnimator!!.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    isUserRotatingDial = false
                }
            })
            objectAnimator!!.start()
        }
    }

    private fun openCalibrationDialog() {
        if (calibrationDialog == null) {
            calibrationDialog = CompassCalibration.newInstance()
            calibrationDialog!!.show(parentFragmentManager, "calibration_dialog")
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            CompassPreferences.dampingCoefficient,
            CompassPreferences.magneticCoefficient,
            CompassPreferences.rotationalInertia -> {
                setPhysicalProperties()
            }

            CompassPreferences.direction_code -> {
                showDirectionCode = CompassPreferences.getDirectionCode()
            }

            CompassPreferences.flowerBloomTheme -> {
                setFlowerTheme(CompassPreferences.getFlowerBloomTheme())
            }

            CompassPreferences.flowerBloom -> {
                setFlower(CompassPreferences.isFlowerBloomOn())
            }

            CompassPreferences.usePhysicalProperties -> {
                isAnimated = CompassPreferences.isUsingPhysicalProperties()
            }

            CompassPreferences.useGimbalLock -> {
                isGimbalLock = CompassPreferences.isUsingGimbalLock()
            }
        }
    }

    companion object {
        fun newInstance(): Compass {
            val args = Bundle()
            val fragment = Compass()
            fragment.arguments = args
            return fragment
        }
    }
}
