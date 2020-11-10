package app.simple.positional.ui

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import app.simple.positional.dialogs.compass.CompassMenu
import app.simple.positional.parallax.ParallaxView
import app.simple.positional.preference.CompassPreference
import app.simple.positional.util.*
import kotlinx.android.synthetic.main.frag_compass.*
import kotlinx.android.synthetic.main.generic_compass_rose.*
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

class Compass : Fragment(), SensorEventListener {

    private var startAngle: Double = 0.0

    private var handler = Handler(Looper.getMainLooper())

    private lateinit var dial: ParallaxView
    private lateinit var degrees: TextView
    private lateinit var dialContainer: FrameLayout

    private val accelerometerReadings = FloatArray(3)
    private val magnetometerReadings = FloatArray(3)

    private val orientation = FloatArray(3)
    private val rotation = FloatArray(9)
    private val inclination = FloatArray(9)

    /**
     *  [readingsAlpha]
     *
     *  this variable acts as a noise filter for sensor values, higher result will give results faster
     *  but will also generate heavier noise on compass movements but movements will be instantaneous
     *  while the lower values will make filter the noise but also acts as a decelerate interpolator
     *  and make the movement smoother and pleasant
     *
     *  0.03F is the default value, 0.05f is smoother, 0.15 is smoother and faster
     *
     *  see [smoothAndSetReadings] for its implementation
     */
    private var readingsAlpha = 0.06f

    private val twoPI = 2.0 * Math.PI
    private val degreesPerRadian = 180 / Math.PI
    private var rotationAngle = 0f

    private lateinit var sensorManager: SensorManager
    private lateinit var sensorAccelerometer: Sensor
    private lateinit var sensorMagneticField: Sensor

    private var isFLowerBlooming = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.frag_compass, container, false)

        dial = v.findViewById(R.id.dial)
        dial.init()

        degrees = v.findViewById(R.id.degrees)

        dialContainer = v.findViewById(R.id.dial_container)

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        isFLowerBlooming = CompassPreference().isFlowerBloom(requireContext())
        setSpeed(CompassPreference().getCompassSpeed(requireContext()))

        return v
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFlower(isFLowerBlooming)

        dialContainer.setOnTouchListener(MyOnTouchListener())

        compass_menu.setOnClickListener {
            val weakReference = WeakReference(CompassMenu(WeakReference(this@Compass)))
            weakReference.get()?.show(parentFragmentManager, "compass_menu")
        }
    }

    override fun onResume() {
        super.onResume()
        register()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
        unregister()
    }

    private fun register() {
        if (context == null) return
        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_GAME)
    }

    private fun unregister() {
        if (context == null) return
        sensorManager.unregisterListener(this, sensorAccelerometer)
        sensorManager.unregisterListener(this, sensorMagneticField)
    }

    private fun animate(imageView: ImageView, value: Float) {
        val animator = ObjectAnimator.ofFloat(imageView, "rotation", imageView.rotation, value)
        animator.duration = 1000
        animator.interpolator = DecelerateInterpolator()
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                if (context == null) return
            }

            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationRepeat(animation: Animator?) {}

        })
        animator.start()
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
                    handler.postDelayed({ animate(dial, 0f) }, 1000)
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

            try {
                rotationAngle = adjustAzimuthForDisplayRotation(
                        -(((((orientation[0] + twoPI) % twoPI * degreesPerRadian).toFloat())) + 360) % 360,
                        requireActivity().windowManager
                )
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }

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

            dial.rotation = rotationAngle

            val azimuth = (rotationAngle * -1) //- ((dial.rotation + 360) % 360).toInt()

            if (isFLowerBlooming) {
                flower_one.rotation = rotationAngle * 2
                flower_two.rotation = azimuth * -3 + 45
                flower_three.rotation = rotationAngle * 1 + 90
                flower_four.rotation = azimuth * -4 + 135
            }

            degrees.text = "${azimuth.toInt()}°"
            direction.text = getDirectionNameFromAzimuth(azimuth = azimuth.toDouble()).toUpperCase(Locale.getDefault())
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        println(accuracy)
        if (sensor == sensorAccelerometer || sensor == sensorMagneticField) {
            when (accuracy) {
                SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                    compass_accuracy.text = fromHtml("Accuracy: <b>Unreliable, immediate calibration required</b>")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                    compass_accuracy.text = fromHtml("Accuracy: <b>Low, calibration required</b>")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                    compass_accuracy.text = fromHtml("Accuracy: <b>Medium</b>")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                    compass_accuracy.text = fromHtml("Accuracy: <b>High</b>")
                }
            }
        }
    }

    private fun smoothAndSetReadings(readings: FloatArray, newReadings: FloatArray) {
        readings[0] = readingsAlpha * newReadings[0] + (1 - readingsAlpha) * readings[0] // x
        readings[1] = readingsAlpha * newReadings[1] + (1 - readingsAlpha) * readings[1] // y
        readings[2] = readingsAlpha * newReadings[2] + (1 - readingsAlpha) * readings[2] // z
    }

    fun setSpeed(value: Float) {
        readingsAlpha = value
    }

    fun setFlower(value: Boolean) {
        isFLowerBlooming = value
        if (value) {
            loadImageResources(R.drawable.compass_flower_01, flower_one, requireContext(), 0)
            loadImageResources(R.drawable.compass_flower_01, flower_two, requireContext(), 50)
            loadImageResources(R.drawable.compass_flower_01, flower_three, requireContext(), 100)
            loadImageResources(R.drawable.compass_flower_01, flower_four, requireContext(), 150)
            animateColorChange(degrees, Color.parseColor("#f88806"), Color.parseColor("#ffffff"))
        } else {
            loadImageResources(0, flower_one, requireContext(), 150)
            loadImageResources(0, flower_two, requireContext(), 100)
            loadImageResources(0, flower_three, requireContext(), 50)
            loadImageResources(0, flower_four, requireContext(), 0)
            animateColorChange(degrees, Color.parseColor("#ffffff"), Color.parseColor("#f88806"))
        }
    }
}