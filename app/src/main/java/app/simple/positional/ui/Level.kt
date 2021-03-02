package app.simple.positional.ui

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.fragment.app.Fragment
import app.simple.positional.R
import app.simple.positional.dialogs.app.ErrorDialog
import app.simple.positional.math.LowPassFilter.smoothAndSetReadings
import app.simple.positional.math.MathExtensions.round
import app.simple.positional.preference.LevelPreferences.isNoSensorAlertON
import app.simple.positional.util.AsyncImageLoader.loadImageResourcesWithoutAnimation
import app.simple.positional.util.HtmlHelper.fromHtml

class Level : Fragment(), SensorEventListener {

    fun newInstance(): Level {
        val args = Bundle()
        val fragment = Level()
        fragment.arguments = args
        return fragment
    }

    private lateinit var levelIndicator: ImageView
    private lateinit var levelDot: ImageView
    private lateinit var boundingBox: FrameLayout
    private lateinit var levelX: TextView
    private lateinit var levelY: TextView

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frag_level, container, false)

        levelIndicator = view.findViewById(R.id.level_indicator)
        levelDot = view.findViewById(R.id.level_dot)
        boundingBox = view.findViewById(R.id.indicator_bounding_box)
        levelX = view.findViewById(R.id.level_x)
        levelY = view.findViewById(R.id.level_y)

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
            hasGravitySensor = true
        } else {
            hasGravitySensor = false
            if (isNoSensorAlertON()) {
                ErrorDialog.newInstance("Level Sensor")
                        .show(childFragmentManager, "error_dialog")
            }
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            requireContext().display?.getRealMetrics(displayMetrics)
        } else {
            @Suppress("deprecation")
            requireActivity().windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        }

        screenHeight = displayMetrics.heightPixels
        screenWidth = displayMetrics.widthPixels

        /**
         * Gravitational constant multiplied by 2
         * (9.8 m/(s * s)) * 2 = 19.6
         */
        gravityWidthMotionCompensator = screenWidth / 19.6F
        gravityHeightMotionCompensator = screenHeight / 19.6F
        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadImageResourcesWithoutAnimation(R.drawable.level_indicator, levelIndicator, requireContext())
        loadImageResourcesWithoutAnimation(R.drawable.level_dot, levelDot, requireContext())

        boundingBox.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    levelIndicator.animate()
                            .scaleX(1.3f)
                            .scaleY(1.3f)
                            .setDuration(1000)
                            .setInterpolator(DecelerateInterpolator())
                            .start()

                    levelDot.animate()
                            .scaleX(1.3f)
                            .scaleY(1.3f)
                            .setDuration(1000)
                            .setInterpolator(DecelerateInterpolator())
                            .start()
                }
                MotionEvent.ACTION_UP -> {
                    levelIndicator.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(1000)
                            .setInterpolator(DecelerateInterpolator())
                            .start()

                    levelDot.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(1000)
                            .setInterpolator(DecelerateInterpolator())
                            .start()
                }
            }
            true
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
                levelDot.setImageResource(R.drawable.level_dot_in_range)
            } else {
                levelDot.setImageResource(R.drawable.level_dot)
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
}
