package app.simple.positional.ui.panels

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color.parseColor
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import app.simple.positional.R
import app.simple.positional.activities.fragment.ScopedFragment
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.dialogs.app.ErrorDialog
import app.simple.positional.math.LowPassFilter.smoothAndSetReadings
import app.simple.positional.math.MathExtensions.round
import app.simple.positional.preferences.LevelPreferences
import app.simple.positional.util.AsyncImageLoader.loadImage
import app.simple.positional.util.HtmlHelper.fromHtml

class Level : ScopedFragment(), SensorEventListener {

    private lateinit var levelIndicator: ImageView
    private lateinit var levelDot: ImageView
    private lateinit var boundingBox: FrameLayout
    private lateinit var levelX: TextView
    private lateinit var levelY: TextView
    private lateinit var styleButton: DynamicRippleImageButton

    private lateinit var gravity: Sensor
    private lateinit var sensorManager: SensorManager
    private val displayMetrics = DisplayMetrics()

    private val gravityReadings = FloatArray(3)

    private var hasGravitySensor = false
    private var isScreenTouched = false

    private var screenHeight = 0
    private var screenWidth = 0
    private var readingsAlpha = 0.01f
    private var gravityWidthMotionCompensator = 0f
    private var gravityHeightMotionCompensator = 0f

    private var anim1X: SpringAnimation? = null
    private var anim1Y: SpringAnimation? = null
    private var anim2X: SpringAnimation? = null
    private var anim2Y: SpringAnimation? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_level, container, false)

        levelIndicator = view.findViewById(R.id.level_indicator)
        levelDot = view.findViewById(R.id.level_dot)
        boundingBox = view.findViewById(R.id.indicator_bounding_box)
        levelX = view.findViewById(R.id.level_x)
        levelY = view.findViewById(R.id.level_y)
        styleButton = view.findViewById(R.id.level_circle)

        anim1X = SpringAnimation(levelIndicator, DynamicAnimation.SCALE_X)
        anim1Y = SpringAnimation(levelIndicator, DynamicAnimation.SCALE_Y)
        anim2X = SpringAnimation(levelDot, DynamicAnimation.SCALE_X)
        anim2Y = SpringAnimation(levelDot, DynamicAnimation.SCALE_Y)

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        kotlin.runCatching {
            gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
            hasGravitySensor = true
        }.getOrElse {
            hasGravitySensor = false

            ErrorDialog.newInstance(getString(R.string.sensor_error))
                .show(childFragmentManager, "error_dialog")
        }

        ErrorDialog.newInstance(getString(R.string.sensor_error))
            .show(childFragmentManager, "error_dialog")

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            requireContext().display?.getRealMetrics(displayMetrics)
        } else {
            @Suppress("deprecation")
            requireActivity().windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        }

        screenHeight = displayMetrics.heightPixels
        screenWidth = displayMetrics.widthPixels

        /**
         * Gravity constant multiplied by 2
         * (9.8 m(s * s)) * 2 = 19.6
         */
        gravityWidthMotionCompensator = screenWidth / 19.6F
        gravityHeightMotionCompensator = screenHeight / 19.6F
        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setStyle()

        boundingBox.setOnTouchListener { _, event ->

            val dampingRation = 0.32F
            val stiffness = SpringForce.STIFFNESS_VERY_LOW
            val scaleLarge = 1.5F
            val scaleNormal = 1.0F

            val springForceLarge = SpringForce()
                    .setDampingRatio(dampingRation)
                    .setStiffness(stiffness)
                    .setFinalPosition(scaleLarge)

            val springForceNormal = SpringForce()
                    .setDampingRatio(dampingRation)
                    .setStiffness(stiffness)
                    .setFinalPosition(scaleNormal)

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    anim1X?.cancel()
                    anim1Y?.cancel()

                    anim1X?.spring = springForceLarge
                    anim1Y?.spring = springForceLarge
                    anim1X?.start()
                    anim1Y?.start()

                    anim2X?.cancel()
                    anim2Y?.cancel()

                    anim2X?.spring = springForceLarge
                    anim2Y?.spring = springForceLarge
                    anim2X?.start()
                    anim2Y?.start()
                }
                MotionEvent.ACTION_UP -> {
                    anim1X?.cancel()
                    anim1Y?.cancel()

                    anim1X?.spring = springForceNormal
                    anim1Y?.spring = springForceNormal
                    anim1X?.start()
                    anim1Y?.start()

                    anim2X?.cancel()
                    anim2Y?.cancel()

                    anim2X?.spring = springForceNormal
                    anim2Y?.spring = springForceNormal
                    anim2X?.start()
                    anim2Y?.start()
                }
            }
            true
        }

        styleButton.setOnClickListener {
            LevelPreferences.setSquareStyle(!LevelPreferences.isSquareStyle())
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasGravitySensor) {
            sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    override fun onPause() {
        super.onPause()
        levelDot.clearAnimation()
        levelIndicator.clearAnimation()
        if (hasGravitySensor) {
            sensorManager.unregisterListener(this, gravity)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_GRAVITY) {

            if (isScreenTouched) return

            smoothAndSetReadings(gravityReadings, event.values, readingsAlpha)

            levelDot.translationX = levelIndicator.translationX * -0.3f //gravityReadings[0] * -1 * gravityWidthMotionCompensator / 4
            levelDot.translationY = levelIndicator.translationY * -0.3f //gravityReadings[1] * gravityHeightMotionCompensator / 4

            // level_dot.scaleX = 2 - ((gravityReadings[0] + gravityReadings[1]) / 2)
            // level_dot.scaleY = 2 - ((gravityReadings[0] + gravityReadings[1]) / 2)

            if (gravityReadings[0] in -0.2..0.2 && gravityReadings[1] in -0.2..0.2) {
                levelDot.imageTintList = ColorStateList.valueOf(parseColor("#37A4CF"))
            } else {
                levelDot.imageTintList = ColorStateList.valueOf(parseColor("#BF4848"))
            }

            if (gravityReadings[0] * gravityWidthMotionCompensator - levelIndicator.width / 2
                    > boundingBox.width / 2 * -1
                    && gravityReadings[0] * gravityWidthMotionCompensator + levelIndicator.width / 2
                    < boundingBox.width / 2) {
                levelIndicator.translationX = gravityReadings[0] * gravityWidthMotionCompensator
            }

            if (gravityReadings[1] * -1 * gravityHeightMotionCompensator - levelIndicator.height / 2
                    > boundingBox.height / 2 * -1
                    && gravityReadings[1] * -1 * gravityHeightMotionCompensator + levelIndicator.height / 2
                    < boundingBox.height / 2) {
                levelIndicator.translationY = gravityReadings[1] * -1 * gravityHeightMotionCompensator
            }

            levelX.text = fromHtml("<b>X:</b> ${round(gravityReadings[0].toDouble(), 2)} m/s²")
            levelY.text = fromHtml("<b>Y:</b> ${round(gravityReadings[1].toDouble(), 2)} m/s²")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        /* no-op */
    }

    private fun setStyle() {
        when {
            LevelPreferences.isSquareStyle() -> {
                loadImage(R.drawable.level_indicator_square, levelIndicator, requireContext(), 0)
                loadImage(R.drawable.level_dot_square, levelDot, requireContext(), 100)
                loadImage(R.drawable.ic_circle, styleButton, requireContext(), 0)
            }
            else -> {
                loadImage(R.drawable.level_indicator, levelIndicator, requireContext(), 0)
                loadImage(R.drawable.level_dot, levelDot, requireContext(), 100)
                loadImage(R.drawable.ic_square, styleButton, requireContext(), 0)
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            LevelPreferences.isSquareStyle -> {
                setStyle()
            }
        }
    }

    companion object {
        fun newInstance(): Level {
            val args = Bundle()
            val fragment = Level()
            fragment.arguments = args
            return fragment
        }
    }
}
