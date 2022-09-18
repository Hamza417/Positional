package app.simple.positional.ui.panels

import android.animation.ValueAnimator
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
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.decorations.views.VerticalTextView
import app.simple.positional.dialogs.app.ErrorDialog
import app.simple.positional.extensions.fragment.ScopedFragment
import app.simple.positional.math.LowPassFilter.smoothAndSetReadings
import app.simple.positional.math.MathExtensions.round
import app.simple.positional.math.MathExtensions.toDegrees
import app.simple.positional.math.Quaternion
import app.simple.positional.math.QuaternionMath
import app.simple.positional.preferences.LevelPreferences
import app.simple.positional.util.HtmlHelper.fromHtml
import app.simple.positional.util.ImageLoader.loadImage
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

class Level : ScopedFragment(), SensorEventListener {

    private lateinit var levelIndicator: ImageView
    private lateinit var levelDot: ImageView
    private lateinit var boundingBox: FrameLayout
    private lateinit var levelX: TextView
    private lateinit var levelY: VerticalTextView
    private lateinit var styleButton: DynamicRippleImageButton

    private lateinit var gravity: Sensor
    private lateinit var sensorManager: SensorManager
    private val displayMetrics = DisplayMetrics()

    private val gravityReadings = FloatArray(3)

    private val _quaternion = Quaternion.zero.toFloatArray()
    val orientation: Quaternion get() = Quaternion.from(_quaternion.clone())

    private var hasGravitySensor = false

    private var screenHeight = 0
    private var screenWidth = 0
    private var readingsAlpha = 0.01f
    private var gravityWidthMotionCompensator = 0f
    private var gravityHeightMotionCompensator = 0f

    private val dampingRatio = 0.32F
    private val stiffness = SpringForce.STIFFNESS_VERY_LOW
    private val scaleLarge = 1.5F
    private val scaleNormal = 1.0F
    private var isLarge = false

    private var anim1X: SpringAnimation? = null
    private var anim1Y: SpringAnimation? = null
    private var anim2X: SpringAnimation? = null
    private var anim2Y: SpringAnimation? = null
    private var animTint: ValueAnimator? = null

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

        @Suppress("deprecation")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            requireContext().display?.getRealMetrics(displayMetrics)
        } else {
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

            val springForceLarge = SpringForce()
                    .setDampingRatio(dampingRatio)
                    .setStiffness(stiffness)
                    .setFinalPosition(scaleLarge)

            val springForceNormal = SpringForce()
                    .setDampingRatio(dampingRatio)
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

                    isLarge = true
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

                    isLarge = false
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

            smoothAndSetReadings(gravityReadings, event.values, readingsAlpha)

            val roll = atan2(gravityReadings[0], sqrt(gravityReadings[1] * gravityReadings[1] + gravityReadings[2] * gravityReadings[2])).toDegrees()
            val pitch = -atan2(gravityReadings[1], sqrt(gravityReadings[0] * gravityReadings[0] + gravityReadings[2] * gravityReadings[2])).toDegrees()

            QuaternionMath.fromEuler(floatArrayOf(roll, pitch, 0f), _quaternion)

            val euler = orientation.toEuler()

            val x = when {
                euler.roll in -90f..90f -> euler.roll
                euler.roll > 90f -> 180 - euler.roll
                else -> -(180 + euler.roll)
            }

            val y = euler.pitch

            val levelSizeCompensator = 1.0F // TODO - Buggy if (isLarge) scaleLarge else scaleNormal

            levelDot.translationX = levelIndicator.translationX * -0.3f //gravityReadings[0] * -1 * gravityWidthMotionCompensator / 4
            levelDot.translationY = levelIndicator.translationY * -0.3f //gravityReadings[1] * gravityHeightMotionCompensator / 4

            // level_dot.scaleX = 2 - ((gravityReadings[0] + gravityReadings[1]) / 2)
            // level_dot.scaleY = 2 - ((gravityReadings[0] + gravityReadings[1]) / 2)

            if (gravityReadings[0] in -0.2..0.2 && gravityReadings[1] in -0.2..0.2) {
                levelDot.imageTintList = ColorStateList.valueOf(parseColor("#37A4CF"))
            } else {
                levelDot.imageTintList = ColorStateList.valueOf(parseColor("#BF4848"))
            }

            if (isLandscapeVar) {
                if (gravityReadings[0] * gravityWidthMotionCompensator - levelIndicator.height * levelSizeCompensator / 2 > boundingBox.height / 2 * -1
                        && gravityReadings[0] * gravityWidthMotionCompensator + levelIndicator.height * levelSizeCompensator / 2 < boundingBox.height / 2) {
                    levelIndicator.translationY = gravityReadings[0] * 1 * gravityHeightMotionCompensator
                }

                if (gravityReadings[1] * -1 * gravityHeightMotionCompensator - levelIndicator.width * levelSizeCompensator / 2 > boundingBox.width / 2 * -1
                        && gravityReadings[1] * -1 * gravityHeightMotionCompensator + levelIndicator.width * levelSizeCompensator / 2 < boundingBox.width / 2) {
                    levelIndicator.translationX = gravityReadings[1] * 1 * gravityWidthMotionCompensator
                }
            } else {
                if (gravityReadings[0] * gravityWidthMotionCompensator - levelIndicator.width * levelSizeCompensator / 2 > boundingBox.width / 2 * -1
                        && gravityReadings[0] * gravityWidthMotionCompensator + levelIndicator.width * levelSizeCompensator / 2 < boundingBox.width / 2) {
                    levelIndicator.translationX = gravityReadings[0] * gravityWidthMotionCompensator
                }

                if (gravityReadings[1] * -1 * gravityHeightMotionCompensator - levelIndicator.height * levelSizeCompensator / 2 > boundingBox.height / 2 * -1
                        && gravityReadings[1] * -1 * gravityHeightMotionCompensator + levelIndicator.height * levelSizeCompensator / 2 < boundingBox.height / 2) {
                    levelIndicator.translationY = gravityReadings[1] * -1 * gravityHeightMotionCompensator
                }
            }

            /**
             * Alternative rounding method
             */
            //String.format("%.2g%n", abs(x))

            if (isLandscapeVar) {
                levelX.text = fromHtml("<b>X:</b> ${abs(round(y.toDouble(), 1))}°")
                levelY.text = fromHtml("<b>Y:</b> ${abs(round(x.toDouble(), 1))}°")
            } else {
                levelX.text = fromHtml("<b>X:</b> ${abs(round(x.toDouble(), 1))}°")
                levelY.text = fromHtml("<b>Y:</b> ${abs(round(y.toDouble(), 1))}°")
            }
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

    // TODO - fix animation framework
    private fun ImageView.changeTint(@ColorInt color: Int) {
        if (imageTintList?.defaultColor == color) return
        animTint = ValueAnimator.ofArgb(imageTintList?.defaultColor ?: parseColor("#37A4CF"), color)
        animTint?.duration = 500L
        animTint?.interpolator = DecelerateInterpolator(1.5F)
        animTint?.addUpdateListener {
            imageTintList = ColorStateList.valueOf(it.animatedValue as Int)
        }
        animTint?.start()
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
