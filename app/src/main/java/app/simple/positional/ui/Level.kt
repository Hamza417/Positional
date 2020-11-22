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
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import app.simple.positional.R
import app.simple.positional.dialogs.compass.NoSensorAlert
import app.simple.positional.preference.LevelPreferences
import app.simple.positional.util.fromHtml
import app.simple.positional.util.loadImageResourcesWithoutAnimation
import app.simple.positional.util.round
import kotlinx.android.synthetic.main.frag_level.*

class Level : Fragment(), SensorEventListener {

    private lateinit var gravity: Sensor
    private lateinit var sensorManager: SensorManager
    private val displayMetrics = DisplayMetrics()

    private var screenHeight = 0
    private var screenWidth = 0

    private val gravityReadings = FloatArray(3)

    private var hasGravitySensor = false
    private var isScreenTouched = false

    private var readingsAlpha = 0.01f
    private var gravityWidthMotionCompensator = 0f
    private var gravityHeightMotionCompensator = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
            hasGravitySensor = true
        } else {
            hasGravitySensor = false
            if (LevelPreferences().isNoSensorAlertON(requireContext())) {
                val noSensorAlert = NoSensorAlert().newInstance("level")
                noSensorAlert.show(parentFragmentManager, "no_sensor_alert")
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

        gravityWidthMotionCompensator = screenWidth / 19.6f
        gravityHeightMotionCompensator = screenHeight / 19.6f
        return inflater.inflate(R.layout.frag_level, container, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadImageResourcesWithoutAnimation(R.drawable.level_indicator, level_indicator, requireContext())
        loadImageResourcesWithoutAnimation(R.drawable.level_dot, level_dot, requireContext())

        indicator_bounding_box.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    //isScreenTouched = true

                    level_indicator.animate()
                            .scaleX(1.3f)
                            .scaleY(1.3f)
                            .setDuration(1000)
                            .setInterpolator(AccelerateDecelerateInterpolator())
                            .start()

                    level_dot.animate()
                            .scaleX(1.3f)
                            .scaleY(1.3f)
                            .setDuration(1000)
                            .setInterpolator(AccelerateDecelerateInterpolator())
                            .start()
                }
                MotionEvent.ACTION_UP -> {
                    //isScreenTouched = false
                    level_indicator.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(1000)
                            .setInterpolator(DecelerateInterpolator())
                            .start()

                    level_dot.animate()
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
        level_dot.clearAnimation()
        level_indicator.clearAnimation()
        if (hasGravitySensor) {
            sensorManager.unregisterListener(this, gravity)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_GRAVITY) {

            if (isScreenTouched) return

            smoothAndSetReadings(gravityReadings, event.values)

            level_dot.translationX = level_indicator.translationX * -0.3f //gravityReadings[0] * -1 * gravityWidthMotionCompensator / 4
            level_dot.translationY = level_indicator.translationY * -0.3f //gravityReadings[1] * gravityHeightMotionCompensator / 4

            // level_dot.scaleX = 2 - ((gravityReadings[0] + gravityReadings[1]) / 2)
            // level_dot.scaleY = 2 - ((gravityReadings[0] + gravityReadings[1]) / 2)

            if (gravityReadings[0] in -0.2..0.2 && gravityReadings[1] in -0.2..0.2) {
                level_dot.setImageResource(R.drawable.level_dot_in_range)
            } else {
                level_dot.setImageResource(R.drawable.level_dot)
            }

            if (gravityReadings[0] * gravityWidthMotionCompensator - level_indicator.width / 2
                    > indicator_bounding_box.width / 2 * -1 &&
                    gravityReadings[0] * gravityWidthMotionCompensator + level_indicator.width / 2
                    < indicator_bounding_box.width / 2) {
                level_indicator.translationX = gravityReadings[0] * gravityWidthMotionCompensator
            }

            if (gravityReadings[1] * -1 * gravityHeightMotionCompensator - level_indicator.height / 2
                    > (indicator_bounding_box.height / 2 * -1)
                    && gravityReadings[1] * -1 * gravityHeightMotionCompensator + level_indicator.height / 2
                    < (indicator_bounding_box.height / 2)) {
                level_indicator.translationY = gravityReadings[1] * -1 * gravityHeightMotionCompensator
            }

            level_x.text = fromHtml("<b>X:</b> ${round(gravityReadings[0].toDouble(), 2)} m/s²")
            level_y.text = fromHtml("<b>Y:</b> ${round(gravityReadings[1].toDouble(), 2)} m/s²")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    private fun smoothAndSetReadings(readings: FloatArray, newReadings: FloatArray) {
        readings[0] = readingsAlpha * newReadings[0] + (1 - readingsAlpha) * readings[0] // x
        readings[1] = readingsAlpha * newReadings[1] + (1 - readingsAlpha) * readings[1] // y
        readings[2] = readingsAlpha * newReadings[2] + (1 - readingsAlpha) * readings[2] // z
    }
}