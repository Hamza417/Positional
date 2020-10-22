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
import android.os.HandlerThread
import android.os.Vibrator
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import app.simple.positional.R
import app.simple.positional.menu.compass.dial.Dial
import app.simple.positional.menu.compass.needle.Needle
import app.simple.positional.parallax.ParallaxView
import app.simple.positional.preference.CompassPreference
import app.simple.positional.util.adjustAzimuthForDisplayRotation
import app.simple.positional.util.getAngle
import app.simple.positional.util.imageViewAnimatedChange
import com.github.zawadz88.materialpopupmenu.popupMenu
import com.google.android.material.card.MaterialCardView
import kotlin.math.abs

class Compass : Fragment(), SensorEventListener {

    private var startAngle: Double = 0.0
    private var mCurrAngle: Double = 0.0
    private var handler = Handler()
    private lateinit var detector: GestureDetector
    private lateinit var vibrator: Vibrator
    private lateinit var needle: ParallaxView
    private lateinit var dial: ParallaxView
    private lateinit var degrees: TextView
    private lateinit var menu: MaterialCardView
    private lateinit var dialContainer: FrameLayout
    private lateinit var sensorManager: SensorManager
    private lateinit var sensorAccelerometer: Sensor
    private lateinit var sensorMagneticField: Sensor
    lateinit var skins: IntArray
    private val width: Int = 500
    private val styleResId: Int = R.style.popupMenu
    private var sensorDelay = SensorManager.SENSOR_DELAY_GAME
    private val twoPI = 2.0 * Math.PI
    private val degreesPerRadian = 180 / Math.PI
    private val readingsAlpha = 0.03f
    private var rotationAngle = 0f
    private var y: Float = 0f
    private var x: Float = 0f
    private var _xDelta = 0
    private var _yDelta = 0
    private val accelerometerReadings = FloatArray(3)
    private val magnetometerReadings = FloatArray(3)
    private val orientation = FloatArray(3)
    private val rotation = FloatArray(9)
    private val inclination = FloatArray(9)
    private var rotateWhich: Int = 1 // True for Needle, False for Dial
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String
    private var isTorchOn: Boolean = false

    private lateinit var mSensorThread: HandlerThread
    private lateinit var mSensorHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        skins = CompassPreference().getSkins(context)
        rotateWhich = CompassPreference().getRotatePreference(requireContext())
        sensorDelay = CompassPreference().getDelay(requireContext())
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        mSensorThread = HandlerThread("Sensor Thread", Thread.MAX_PRIORITY)
        mSensorThread.start()
        mSensorHandler = Handler(mSensorThread.looper) //Blocks until looper is prepared, which is fairly quick

        detector = GestureDetector(requireContext(), MyGestureDetector())
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
        menu = v.findViewById(R.id.compass_menu)

        cameraManager = requireActivity().getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager.cameraIdList[0]

        dialContainer = v.findViewById(R.id.dial_container)

        setSkins()

        return v
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        register()

        menu.setOnClickListener {
            unregister()
            val popupMenu = popupMenu {
                style = styleResId
                dropdownGravity = Gravity.END
                section {
                    title = "Appearance"
                    item {
                        icon = R.drawable.ic_navigation
                        hasNestedItems = true
                        label = "Needle"
                        callback = {
                            Needle().needleSkinsOptions(requireContext(), this@Compass, skins[0])
                        }
                    }
                    item {
                        icon = R.drawable.ic_compass
                        hasNestedItems = true
                        label = "Dial"
                        callback = {
                            Dial().dialSkinsOptions(requireContext(), this@Compass, skins[1])
                        }
                    }
                }
                section {
                    title = "Configuration"
                    item {
                        label = "Parallax"
                        icon = R.drawable.ic_parallax
                        hasNestedItems = true
                        callback = {
                            val parallaxPopupMenu = popupMenu {
                                style = styleResId
                                fixedContentWidthInPx = width
                                dropdownGravity = Gravity.END
                                section {
                                    val isParallaxEnabled = CompassPreference().getParallax(requireContext())
                                    title = "Parallax"
                                    item {
                                        label = if (isParallaxEnabled) "Disable" else "Enable"
                                        icon = if (isParallaxEnabled) R.drawable.ic_check_box_checked else R.drawable.ic_check_box_unchecked
                                        callback = {
                                            if (isParallaxEnabled) {
                                                CompassPreference().setParallax(false, requireContext())
                                                needle.resetTranslationValues()
                                                dial.resetTranslationValues()
                                            } else {
                                                CompassPreference().setParallax(true, requireContext())
                                                parallax(true)
                                            }
                                        }
                                    }
                                    item {
                                        label = "Calibrate"
                                        icon = R.drawable.ic_calibration
                                        callback = {
                                            needle.calibrate()
                                            dial.calibrate()
                                        }
                                    }
                                }
                                section {
                                    val sensitivity = CompassPreference().getParallaxSensitivity(requireContext())
                                    title = "Sensitivity"
                                    item {
                                        label = "Very Low"
                                        icon = if (sensitivity == 5) R.drawable.ic_radio_button_checked else R.drawable.ic_radio_button_unchecked
                                        callback = {
                                            setSensitivity(5)
                                        }
                                    }
                                    item {
                                        label = "Low"
                                        icon = if (sensitivity == 10) R.drawable.ic_radio_button_checked else R.drawable.ic_radio_button_unchecked
                                        callback = {
                                            setSensitivity(10)
                                        }
                                    }
                                    item {
                                        label = "Medium"
                                        icon = if (sensitivity == 15) R.drawable.ic_radio_button_checked else R.drawable.ic_radio_button_unchecked
                                        callback = {
                                            setSensitivity(15)
                                        }
                                    }
                                    item {
                                        label = "High"
                                        icon = if (sensitivity == 20) R.drawable.ic_radio_button_checked else R.drawable.ic_radio_button_unchecked
                                        callback = {
                                            setSensitivity(20)
                                        }
                                    }
                                }
                            }

                            parallaxPopupMenu.show(requireContext(), menu)
                        }
                    }
                    item {
                        label = "Sensor Speed"
                        hasNestedItems = true
                        icon = R.drawable.ic_speed_fast
                        callback = {
                            val sensorPopupMenu = popupMenu {
                                style = R.style.popupMenu
                                fixedContentWidthInPx = width
                                dropdownGravity = Gravity.END
                                section {
                                    title = "Sensor Speed"
                                    item {
                                        label = "Fast"
                                        icon = R.drawable.ic_speed_fast
                                        callback = {
                                            sensorDelay = SensorManager.SENSOR_DELAY_FASTEST
                                            CompassPreference().setDelay(sensorDelay, requireContext())
                                            unregister()
                                            register()
                                        }
                                    }
                                    item {
                                        label = "Smooth"
                                        icon = R.drawable.ic_speed_smooth
                                        callback = {
                                            sensorDelay = SensorManager.SENSOR_DELAY_GAME
                                            CompassPreference().setDelay(sensorDelay, requireContext())
                                            unregister()
                                            register()
                                        }
                                    }
                                }
                            }
                            sensorPopupMenu.show(requireContext(), menu)
                        }
                    }
                    item {
                        label = "Rotate"
                        hasNestedItems = true
                        icon = R.drawable.ic_rotate_which
                        callback = {
                            val rotateWhichMenu = popupMenu {
                                style = styleResId
                                dropdownGravity = Gravity.END
                                section {
                                    title = "Which to rotate ?"
                                    item {
                                        label = "Rotate Needle"
                                        icon = if (rotateWhich == 1) {
                                            R.drawable.ic_check_box_checked
                                        } else {
                                            R.drawable.ic_check_box_unchecked
                                        }
                                        callback = {
                                            rotateWhich = 1
                                            unregister()
                                            animate(dial, 0f)
                                            animate(needle, rotationAngle)
                                            CompassPreference().setRotatePreference(requireContext(), rotateWhich)
                                        }
                                    }
                                    item {
                                        label = "Rotate Dial"
                                        icon = if (rotateWhich == 2) {
                                            R.drawable.ic_check_box_checked
                                        } else {
                                            R.drawable.ic_check_box_unchecked
                                        }
                                        callback = {
                                            rotateWhich = 2
                                            unregister()
                                            animate(needle, 0f)
                                            animate(dial, rotationAngle)
                                            CompassPreference().setRotatePreference(requireContext(), rotateWhich)
                                        }
                                    }
                                    item {
                                        label = "Rotate Both"
                                        icon = if (rotateWhich == 3) {
                                            R.drawable.ic_check_box_checked
                                        } else {
                                            R.drawable.ic_check_box_unchecked
                                        }
                                        callback = {
                                            rotateWhich = 3
                                            unregister()
                                            animate(needle, rotationAngle)
                                            animate(dial, rotationAngle)
                                            CompassPreference().setRotatePreference(requireContext(), rotateWhich)
                                        }
                                    }
                                }
                            }

                            rotateWhichMenu.show(requireContext(), menu)
                        }
                    }
                }
                section {
                    title = "Tools"
                    item {
                        label = "Torch"
                        icon = if (isTorchOn) R.drawable.ic_flash_off else R.drawable.ic_flash_on
                        callback = {
                            if (!isTorchOn) {
                                cameraManager.setTorchMode(cameraId, true)
                                isTorchOn = true
                            } else {
                                cameraManager.setTorchMode(cameraId, false)
                                isTorchOn = false
                            }
                        }
                    }
                }
            }

            popupMenu.show(requireContext(), menu)
            popupMenu.setOnDismissListener { register() }
        }

        dialContainer.setOnTouchListener(MyOnTouchListener())
    }

    override fun onResume() {
        super.onResume()
        register()
    }

    override fun onPause() {
        super.onPause()
        unregister()
    }

    override fun onStop() {
        super.onStop()
        unregister()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregister()
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> smoothAndSetReadings(accelerometerReadings, event.values)
            Sensor.TYPE_MAGNETIC_FIELD -> smoothAndSetReadings(magnetometerReadings, event.values)
        }

        val successfullyCalculatedRotationMatrix = SensorManager.getRotationMatrix(this.rotation, inclination, accelerometerReadings, magnetometerReadings)

        if (successfullyCalculatedRotationMatrix) {
            SensorManager.getOrientation(this.rotation, orientation)

            rotationAngle = -(adjustAzimuthForDisplayRotation(((orientation[0] + twoPI) % twoPI * degreesPerRadian).toFloat(), requireActivity().windowManager) + 360) % 360

            when (rotateWhich) {
                1 -> needle.rotation = rotationAngle
                2 -> dial.rotation = rotationAngle
                3 -> {
                    needle.rotation = rotationAngle
                    dial.rotation = rotationAngle
                }
            }

            val azimuth = (rotationAngle * -1).toInt() //- ((dial.rotation + 360) % 360).toInt()

            /**
            azimuth = if (dialAngle < 0) {
            if ((needle.rotation.toInt() - dialAngle.toInt()) * -1 > 0) {
            (needle.rotation.toInt() * -1) - (dialAngle.toInt() * -1)
            } else (dialAngle.toInt() * -1) - (needle.rotation.toInt() * -1)
            } else if (dialAngle > 0) {
            if ((needle.rotation.toInt() + dialAngle.toInt()) * -1 > 0) {
            (needle.rotation.toInt() * -1) + (dialAngle.toInt() * -1)
            } else (dialAngle.toInt() * -1) + (needle.rotation.toInt() * -1)
            } else {
            (needle.rotation * -1).toInt()
            }
             **/

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

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    private fun smoothAndSetReadings(readings: FloatArray, newReadings: FloatArray) {
        readings[0] = readingsAlpha * newReadings[0] + (1 - readingsAlpha) * readings[0]
        readings[1] = readingsAlpha * newReadings[1] + (1 - readingsAlpha) * readings[1]
        readings[2] = readingsAlpha * newReadings[2] + (1 - readingsAlpha) * readings[2]
    }

    private fun register() {
        sensorManager.registerListener(this, sensorAccelerometer, sensorDelay)
        sensorManager.registerListener(this, sensorMagneticField, sensorDelay)
        if (CompassPreference().getParallax(requireContext())) {
            parallax(true)
        }
    }

    private fun unregister() {
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
                register()
            }

            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationRepeat(animation: Animator?) {}

        })
        animator.start()
    }

    private fun setSkins() {
        setNeedle()
        setDial()
    }

    fun setNeedle() {
        if (needle.tag != skins[0]) {
            imageViewAnimatedChange(needle, skins[0], requireContext())
            needle.tag = skins[0]
        }
    }

    fun setDial() {
        if (dial.tag != skins[1]) {
            imageViewAnimatedChange(dial, skins[1], requireContext())
            dial.tag = skins[1]
        }
    }

    fun setDialAlpha(value: Float) {
        dial.animate().alpha(value).setDuration(1500).setInterpolator(AccelerateDecelerateInterpolator()).start()
    }

    private fun setSensitivity(value: Int) {
        needle.sensitivity = value
        needle.calibrate()
        dial.sensitivity = value
        dial.calibrate()
    }

    /**
     * Simple implementation of an [OnTouchListener] for registering the dialer's touch events.
     */
    private inner class MyOnTouchListener : View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startAngle = getAngle(event.x.toDouble(), event.y.toDouble(), dial.width.toFloat(), dial.height.toFloat())
                }
                MotionEvent.ACTION_MOVE -> {
                    val currentAngle: Double = getAngle(event.x.toDouble(), event.y.toDouble(), dial.width.toFloat(), dial.height.toFloat())
                    rotateDial((startAngle - currentAngle).toFloat())

                    //startAngle = currentAngle
                }
                MotionEvent.ACTION_UP -> {
                    if (rotateWhich != 2 || rotateWhich != 3) {
                        handler.postDelayed({ animate(dial, 0f) }, 1000)
                    }
                }
            }
            //detector.onTouchEvent(event)
            return true
        }
    }

    /**
     * Simple implementation of a [SimpleOnGestureListener] for detecting a fling event.
     */
    private inner class MyGestureDetector : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            dial.post(FlingRunnable(velocityX + velocityY))
            return true
        }
    }

    /**
     * A [Runnable] for animating the the dialer's fling.
     */
    private inner class FlingRunnable(private var velocity: Float) : Runnable {
        override fun run() {
            if (abs(velocity) > 5) {
                rotateDial(velocity / 75)
                velocity /= 1.0666f

                // post this instance again
                dial.post(this)
            }
        }
    }

    private fun rotateDial(degrees: Float) {
        dial.rotation = degrees
    }
}