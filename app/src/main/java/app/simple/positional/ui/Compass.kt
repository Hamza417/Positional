package app.simple.positional.ui

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.SensorManager
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.positional.R
import app.simple.positional.menu.compass.dial.Dial
import app.simple.positional.menu.compass.needle.Needle
import app.simple.positional.parallax.ParallaxView
import app.simple.positional.preference.CompassPreference
import app.simple.positional.util.adjustAzimuthForDisplayRotation
import app.simple.positional.util.getAngle
import app.simple.positional.util.imageViewAnimatedChange
import com.github.zawadz88.materialpopupmenu.popupMenu
import kotlin.math.abs

class Compass : Fragment() {

    private var startAngle: Double = 0.0
    private var mCurrAngle: Double = 0.0
    private var handler = Handler()
    private lateinit var detector: GestureDetector
    private lateinit var needle: ParallaxView
    private lateinit var dial: ParallaxView
    private lateinit var degrees: TextView
    private lateinit var dialContainer: FrameLayout
    lateinit var actionView: View

    lateinit var skins: IntArray
    private val width: Int = 500
    private val styleResId: Int = R.style.popupMenu
    private var sensorDelay = SensorManager.SENSOR_DELAY_GAME
    private var rotationAngle = 0f

    private var filter: IntentFilter = IntentFilter("compass_update")
    private lateinit var compassBroadcastReceiver: BroadcastReceiver

    private var rotateWhich: Int = 1 // True for Needle, False for Dial
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraId: String
    private var isTorchOn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        skins = CompassPreference().getSkins(requireContext())
        rotateWhich = CompassPreference().getRotatePreference(requireContext())
        sensorDelay = CompassPreference().getDelay(requireContext())

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

        cameraManager = requireActivity().getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager.cameraIdList[0]

        dialContainer = v.findViewById(R.id.dial_container)

        setSkins()

        return v
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialContainer.setOnTouchListener(MyOnTouchListener())

        compassBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null) {

                    rotationAngle = adjustAzimuthForDisplayRotation(intent.getFloatExtra("rotation", 0f), requireActivity().windowManager)

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
        if (context == null) return
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(compassBroadcastReceiver, filter)
        if (CompassPreference().getParallax(requireContext())) {
            parallax(true)
        }
    }

    private fun unregister() {
        if (context == null) return
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(compassBroadcastReceiver)
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

    fun openCompassMenu(actionView: View) {
        this.actionView = actionView
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

                        parallaxPopupMenu.show(requireContext(), actionView)
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
                        sensorPopupMenu.show(requireContext(), actionView)
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
                                        R.drawable.ic_radio_button_checked
                                    } else {
                                        R.drawable.ic_radio_button_unchecked
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
                                        R.drawable.ic_radio_button_checked
                                    } else {
                                        R.drawable.ic_radio_button_unchecked
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
                                        R.drawable.ic_radio_button_checked
                                    } else {
                                        R.drawable.ic_radio_button_unchecked
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

                        rotateWhichMenu.show(requireContext(), actionView)
                    }
                }
            }
            section {
                title = "Tools"
                item {
                    label = "Torch"
                    icon = if (isTorchOn) R.drawable.ic_flash_off else R.drawable.ic_flash_on
                    callback = {
                        isTorchOn = if (!isTorchOn) {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                cameraManager.setTorchMode(cameraId, true)
                            }
                            true
                        } else {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                cameraManager.setTorchMode(cameraId, false)
                            }
                            false
                        }
                    }
                }
            }
        }

        popupMenu.show(requireContext(), actionView)
    }
}