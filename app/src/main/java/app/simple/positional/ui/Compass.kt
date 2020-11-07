package app.simple.positional.ui

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import app.simple.positional.R
import app.simple.positional.constants.compassDialSkins
import app.simple.positional.constants.compassNeedleSkins
import app.simple.positional.dialogs.compass.CompassMenu
import app.simple.positional.parallax.ParallaxView
import app.simple.positional.preference.CompassPreference
import app.simple.positional.util.*
import kotlinx.android.synthetic.main.frag_compass.*
import java.lang.ref.WeakReference
import kotlin.math.pow
import kotlin.math.sqrt

class Compass : Fragment(), SensorEventListener {

    private var startAngle: Double = 0.0

    private var handler = Handler()

    private lateinit var needle: ParallaxView
    private lateinit var dial: ParallaxView
    private lateinit var degrees: TextView
    private lateinit var dialContainer: FrameLayout

    lateinit var skins: IntArray
    private val styleResId: Int = R.style.popupMenu
    private var sensorDelay = SensorManager.SENSOR_DELAY_GAME

    private var rotateWhich: Int = 1 // True for Needle, False for Dial
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String
    private var isTorchOn: Boolean = false

    private val accelerometerReadings = FloatArray(3)
    private val magnetometerReadings = FloatArray(3)

    private val orientation = FloatArray(3)
    private val rotation = FloatArray(9)
    private val inclination = FloatArray(9)

    private val readingsAlpha = 0.03f
    private val twoPI = 2.0 * Math.PI
    private val degreesPerRadian = 180 / Math.PI
    private var rotationAngle = 0f

    private lateinit var sensorManager: SensorManager
    private lateinit var sensorAccelerometer: Sensor
    private lateinit var sensorMagneticField: Sensor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        skins = CompassPreference().getSkins(requireContext())
        rotateWhich = CompassPreference().getRotatePreference(requireContext())
        sensorDelay = CompassPreference().getDelay(requireContext())

        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.frag_compass, container, false)

        dial = v.findViewById(R.id.dial)
        dial.init()
        dial.alpha = CompassPreference().getDialOpacity(requireContext())
        dial.setTranslationMultiplier(1f)
        needle = v.findViewById(R.id.compass_needle)
        needle.init()
        needle.setTranslationMultiplier(4f)

        degrees = v.findViewById(R.id.degrees)

        cameraManager = requireActivity().getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager.cameraIdList[0]

        dialContainer = v.findViewById(R.id.dial_container)

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        return v
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialContainer.setOnTouchListener(MyOnTouchListener())

        compass_menu.setOnClickListener {
            val weakReference = WeakReference(CompassMenu(WeakReference(this@Compass)))
            weakReference.get()?.show(parentFragmentManager, "compass_menu")
        }
    }

    override fun onResume() {
        super.onResume()
        register(CompassPreference().getDelay(requireContext()))
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
        unregister()
    }

    private fun register(sensorDelay: Int) {
        if (context == null) return
        sensorManager.registerListener(this, sensorAccelerometer, sensorDelay)
        sensorManager.registerListener(this, sensorMagneticField, sensorDelay)
        if (CompassPreference().getParallax(requireContext())) {
            parallax(true)
        }
    }

    private fun unregister() {
        if (context == null) return
        sensorManager.unregisterListener(this, sensorAccelerometer)
        sensorManager.unregisterListener(this, sensorMagneticField)
        if (CompassPreference().getParallax(requireContext())) {
            parallax(false)
        }
    }

    private fun parallax(boolean: Boolean) {
        if (boolean) {
            needle.registerSensorListener(ParallaxView.SensorDelay.GAME)
            needle.setMovementMultiplier(ParallaxView.DEFAULT_MOVEMENT_MULTIPLIER.toFloat() * 1.2f)
            dial.registerSensorListener(ParallaxView.SensorDelay.FASTEST)
            dial.setMovementMultiplier(ParallaxView.DEFAULT_MOVEMENT_MULTIPLIER.toFloat())
        } else {
            needle.unregisterSensorListener()
            dial.unregisterSensorListener()
        }
    }

    private fun animate(imageView: ImageView, value: Float) {
        val animator = ObjectAnimator.ofFloat(imageView, "rotation", imageView.rotation, value)
        animator.duration = 1000
        animator.interpolator = DecelerateInterpolator()
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                if (context == null) return
                register(CompassPreference().getDelay(requireContext()))
            }

            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationRepeat(animation: Animator?) {}

        })
        animator.start()
    }

    private fun setSkins() {
        setNeedle(CompassPreference().getNeedle(requireContext()))
        setDial(CompassPreference().getDial(requireContext()))
    }

    fun setNeedle(value: Int) {
        loadImageResources(compassNeedleSkins[value], needle, requireContext())
    }

    fun setDial(value: Int) {
        loadImageResources(compassDialSkins[value], dial, requireContext())
    }

    fun setDialAlpha(value: Int) {
        dial.alpha = value / 100f
    }

    fun toggleParallax() {
        if (CompassPreference().getParallax(requireContext())) {
            CompassPreference().setParallax(false, requireContext())
            needle.resetTranslationValues()
            dial.resetTranslationValues()
        } else {
            CompassPreference().setParallax(true, requireContext())
            parallax(true)
        }
    }

    fun calibrate() {
        needle.calibrate()
        dial.calibrate()
    }

    fun rotateWhich(value: Int) {
        when (value) {
            1 -> {
                rotateWhich = 1
                unregister()
                animate(dial, 0f)
                animate(needle, rotationAngle)
                CompassPreference().setRotatePreference(requireContext(), rotateWhich)
            }
            2 -> {
                rotateWhich = 2
                unregister()
                animate(needle, 0f)
                animate(dial, rotationAngle)
                CompassPreference().setRotatePreference(requireContext(), rotateWhich)
            }
            3 -> {
                rotateWhich = 3
                unregister()
                animate(needle, rotationAngle)
                animate(dial, rotationAngle)
                CompassPreference().setRotatePreference(requireContext(), rotateWhich)
            }
        }
    }

    fun setSpeed(value: Int) {
        unregister()
        when (value) {
            SensorManager.SENSOR_DELAY_GAME -> register(value)
            SensorManager.SENSOR_DELAY_FASTEST -> register(value)
        }
    }

    /**
     * Simple implementation of an [View.OnTouchListener] for registering the dialer's touch events.
     */
    private inner class MyOnTouchListener : View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    handler.removeCallbacksAndMessages(null)
                    startAngle = getAngle(event.x.toDouble(), event.y.toDouble(), dial.width.toFloat(), dial.height.toFloat())
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val currentAngle: Double = getAngle(event.x.toDouble(), event.y.toDouble(), dial.width.toFloat(), dial.height.toFloat())
                    rotateDial((startAngle - currentAngle).toFloat())

                    //startAngle = currentAngle
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    if (rotateWhich != 2 || rotateWhich != 3) {
                        handler.postDelayed({ animate(dial, 0f) }, 1000)
                    }
                    return true
                }
            }
            return true
        }
    }

    private fun rotateDial(degrees: Float) {
        dial.rotation = degrees
    }

    override fun onSensorChanged(event: SensorEvent?) {

        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> smoothAndSetReadings(accelerometerReadings, event.values)
            Sensor.TYPE_MAGNETIC_FIELD -> smoothAndSetReadings(magnetometerReadings, event.values)
        }

        val successfullyCalculatedRotationMatrix = SensorManager.getRotationMatrix(this.rotation, inclination, accelerometerReadings, magnetometerReadings)

        if (successfullyCalculatedRotationMatrix) {
            SensorManager.getOrientation(this.rotation, orientation)

            rotationAngle = adjustAzimuthForDisplayRotation(-(((((orientation[0] + twoPI) % twoPI * degreesPerRadian).toFloat())) + 360) % 360, requireActivity().windowManager)

            /**
             * Still testing this, currently it partially works
             *
             * the problem with the above algorithm is it only measures the direction correctly when device is facing up
             * This method approximately calculates if the device is facing up or down and that is by
             * comparing the z value to the x and y values. If the z value dominates or > 0 than the device is facing up
             *
             * If device is facing down, then value calculated gives a negative result
             *
             * Alternative method would be to simple check [accelerometerReadings] z value and if it is positive device is facing up
             * if negative then device is facing down
             *
             * Value 1.0e-6 is there to prevent accidentally dividing by zero when device is exactly perpendicular to the gravity
             */
            rotationAngle += if (accelerometerReadings[2] / sqrt(accelerometerReadings[0].pow(2) + accelerometerReadings[1].pow(2) + accelerometerReadings[2].pow(2) + 1.0e-6) > 0) {
                0f
            } else {
                180f
            }

            println(accelerometerReadings[2])

            when (rotateWhich) {
                1 -> needle.rotation = rotationAngle
                2 -> dial.rotation = rotationAngle
                3 -> {
                    needle.rotation = rotationAngle
                    dial.rotation = rotationAngle
                }
            }

            val azimuth = (rotationAngle * -1).toInt() //- ((dial.rotation + 360) % 360).toInt()

            var direction = "NW"
            if (azimuth >= 350 || azimuth <= 10) {
                direction = "N"
            }
            if (azimuth in 281..349) {
                direction = "NW"
            }
            if (azimuth in 261..280) {
                direction = "W"
            }
            if (azimuth in 191..260) {
                direction = "SW"
            }
            if (azimuth in 171..190) {
                direction = "S"
            }
            if (azimuth in 101..170) {
                direction = "SE"
            }
            if (azimuth in 81..100) {
                direction = "E"
            }
            if (azimuth in 11..80) {
                direction = "NE"
            }

            degrees.text = "$azimuth° $direction"
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        println(accuracy)
        if (sensor == sensorAccelerometer || sensor == sensorMagneticField) {
            when (accuracy) {
                SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                    dial.imageTintList = null
                    loadImageResources(R.drawable.compass_calibrate, dial, requireContext())
                    needle.visibility = View.GONE
                    compass_accuracy.text = fromHtml("Accuracy: <b>Unreliable</b>")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                    dial.imageTintList = null
                    loadImageResources(R.drawable.compass_calibrate, dial, requireContext())
                    needle.visibility = View.GONE
                    compass_accuracy.text = fromHtml("Accuracy: <b>Low</b>")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                    dial.setColorFilter(requireContext().resolveAttrColor(R.attr.iconColor))
                    needle.visibility = View.VISIBLE
                    setSkins()
                    compass_accuracy.text = fromHtml("Accuracy: <b>Medium</b>")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                    dial.setColorFilter(requireContext().resolveAttrColor(R.attr.iconColor))
                    needle.visibility = View.VISIBLE
                    setSkins()
                    compass_accuracy.text = fromHtml("Accuracy: <b>High</b>")
                }
            }
        }
    }

    private fun smoothAndSetReadings(readings: FloatArray, newReadings: FloatArray) {
        readings[0] = readingsAlpha * newReadings[0] + (1 - readingsAlpha) * readings[0]
        readings[1] = readingsAlpha * newReadings[1] + (1 - readingsAlpha) * readings[1]
        readings[2] = readingsAlpha * newReadings[2] + (1 - readingsAlpha) * readings[2]
    }
}