package app.simple.positional.ui

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.*
import android.graphics.Color
import android.hardware.*
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.activities.fragment.ScopedFragment
import app.simple.positional.callbacks.BottomSheetSlide
import app.simple.positional.constants.CompassBloom.compassBloomRes
import app.simple.positional.constants.CompassBloom.compassBloomTextColor
import app.simple.positional.decorations.corners.DynamicCornerMaterialToolbar
import app.simple.positional.decorations.views.CustomCoordinatorLayout
import app.simple.positional.dialogs.app.ErrorDialog
import app.simple.positional.dialogs.compass.CompassCalibration
import app.simple.positional.dialogs.compass.CompassMenu
import app.simple.positional.math.Angle.getAngle
import app.simple.positional.math.Angle.normalizeEulerAngle
import app.simple.positional.math.CompassAzimuth
import app.simple.positional.math.LowPassFilter.smoothAndSetReadings
import app.simple.positional.math.MathExtensions.round
import app.simple.positional.math.Vector3
import app.simple.positional.preference.CompassPreference
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import app.simple.positional.util.AsyncImageLoader.loadImage
import app.simple.positional.util.ColorAnimator.animateColorChange
import app.simple.positional.util.Direction.getDirectionCodeFromAzimuth
import app.simple.positional.util.Direction.getDirectionNameFromAzimuth
import app.simple.positional.util.HtmlHelper.fromHtml
import app.simple.positional.util.NullSafety.isNull
import app.simple.positional.util.setTextAnimation
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.util.*
import kotlin.math.abs

class Compass : ScopedFragment(), SensorEventListener, SharedPreferences.OnSharedPreferenceChangeListener {

    fun newInstance(): Compass {
        val args = Bundle()
        val fragment = Compass()
        fragment.arguments = args
        return fragment
    }

    private var handler = Handler(Looper.getMainLooper())
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<CoordinatorLayout>
    private lateinit var bottomSheetSlide: BottomSheetSlide
    private var objectAnimator: ObjectAnimator? = null
    private var backPress: OnBackPressedDispatcher? = null
    private var calibrationDialog: CompassCalibration? = null

    private val accelerometerReadings = FloatArray(3)
    private val magnetometerReadings = FloatArray(3)

    private var haveAccelerometerSensor = false
    private var haveMagnetometerSensor = false
    private var showDirectionCode = true
    private var isUserRotatingDial = false

    private var accelerometer = Vector3.zero
    private var magnetometer = Vector3.zero

    private var filter: IntentFilter = IntentFilter()
    private lateinit var locationBroadcastReceiver: BroadcastReceiver

    /**
     *  [readingsAlpha]
     *
     *  this variable acts as a noise filter for sensor values, higher result will give results faster
     *  but will also generate heavier noise on compass movements but movements will be instantaneous
     *  while the lower values will make filter the noise but also acts as a decelerate interpolator
     *  and make the movement smoother and pleasant
     *
     *  0.03F is the somewhat default value, 0.05f is smoother, 0.15 is faster
     *
     *  see [smoothAndSetReadings] for its implementation
     */
    private var readingsAlpha = 0.06f
    private var rotationAngle = 0f
    private var flowerBloom = 0
    private var lastDialAngle = 0F
    private var startAngle = 0F

    private lateinit var sensorManager: SensorManager
    private lateinit var sensorAccelerometer: Sensor
    private lateinit var sensorMagneticField: Sensor

    private lateinit var accuracyAccelerometer: TextView
    private lateinit var accuracyMagnetometer: TextView
    private lateinit var inclinationTextView: TextView
    private lateinit var declination: TextView
    private lateinit var fieldStrength: TextView
    private lateinit var compassInfoText: TextView
    private lateinit var degrees: TextView
    private lateinit var direction: TextView

    private lateinit var expandUp: ImageView
    private lateinit var dial: ImageView
    private lateinit var flowerOne: ImageView
    private lateinit var flowerTwo: ImageView
    private lateinit var flowerThree: ImageView
    private lateinit var flowerFour: ImageView

    private lateinit var copy: ImageButton
    private lateinit var menu: ImageButton
    private lateinit var dialContainer: FrameLayout
    private lateinit var compassListScrollView: NestedScrollView
    private lateinit var dim: View
    private lateinit var toolbar: DynamicCornerMaterialToolbar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.frag_compass, container, false)

        accuracyAccelerometer = view.findViewById(R.id.compass_accuracy_accelerometer)
        accuracyMagnetometer = view.findViewById(R.id.compass_accuracy_magnetometer)
        inclinationTextView = view.findViewById(R.id.compass_inclination)
        declination = view.findViewById(R.id.compass_declination)
        fieldStrength = view.findViewById(R.id.compass_field_strength)
        compassInfoText = view.findViewById(R.id.compass_info_text)
        degrees = view.findViewById(R.id.degrees)
        direction = view.findViewById(R.id.direction)

        expandUp = view.findViewById(R.id.expand_up_compass_sheet)
        dial = view.findViewById(R.id.dial)
        flowerOne = view.findViewById(R.id.flower_one)
        flowerTwo = view.findViewById(R.id.flower_two)
        flowerThree = view.findViewById(R.id.flower_three)
        flowerFour = view.findViewById(R.id.flower_four)

        copy = view.findViewById(R.id.compass_copy)
        menu = view.findViewById(R.id.compass_menu)
        dialContainer = view.findViewById(R.id.dial_container)
        (view.findViewById(R.id.compass_main_layout) as CustomCoordinatorLayout).setProxyView(view)
        compassListScrollView = view.findViewById(R.id.compass_list_scroll_view)
        dim = view.findViewById(R.id.compass_dim)
        toolbar = view.findViewById(R.id.compass_appbar)

        filter.addAction("location")
        showDirectionCode = CompassPreference.getDirectionCode()
        bottomSheetSlide = requireActivity() as BottomSheetSlide
        backPress = requireActivity().onBackPressedDispatcher
        bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.compass_info_bottom_sheet))
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        try {
            if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD).isNull() && sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).isNull()) {
                haveAccelerometerSensor = false
                haveMagnetometerSensor = false

                if (CompassPreference.isNoSensorAlertON()) {
                    ErrorDialog.newInstance("Compass Sensor")
                            .show(childFragmentManager, "error_dialog")
                }
            } else {
                sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
                sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                haveMagnetometerSensor = true
                haveAccelerometerSensor = true
            }
        } catch (e: NullPointerException) {
            haveAccelerometerSensor = false
            haveMagnetometerSensor = false
        }

        flowerBloom = CompassPreference.getFlowerBloomTheme()
        setSpeed(CompassPreference.getCompassSpeed())
        setFlower(CompassPreference.isFlowerBloomOn())

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

        copy.setOnClickListener {
            handler.removeCallbacks(textAnimationRunnable)
            val clipboard: ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            val stringBuilder = StringBuilder()

            stringBuilder.append("${getString(R.string.compass_info)}\n\n")
            stringBuilder.append("${getString(R.string.compass_accuracy)}\n")
            stringBuilder.append("${accuracyAccelerometer.text}\n")
            stringBuilder.append("${accuracyMagnetometer.text}\n")
            stringBuilder.append("\n${getString(R.string.compass_field)}\n")
            stringBuilder.append("${inclinationTextView.text}\n")
            stringBuilder.append("${declination.text}\n")
            stringBuilder.append("${fieldStrength.text}\n\n")

            if (BuildConfig.FLAVOR == "lite") {
                stringBuilder.append("\n\n")
                stringBuilder.append("Information is copied using Positional Lite\n")
                stringBuilder.append("Get the app from:\nhttps://play.google.com/store/apps/details?id=app.simple.positional.lite")
            }

            val clip: ClipData = ClipData.newPlainText("Time Data", stringBuilder)
            clipboard.setPrimaryClip(clip)

            if (clipboard.hasPrimaryClip()) {
                compassInfoText.setTextAnimation(getString(R.string.info_copied), 300)
                handler.postDelayed(textAnimationRunnable, 3000)
            }
        }

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    backPressed(true)
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    backPressed(false)
                    if (backPress!!.hasEnabledCallbacks()) {
                        /**
                         * This is a workaround and not a full fledged method to
                         * remove any existing callbacks
                         *
                         * The [bottomSheetBehavior] adds a new callback every time it is expanded
                         * and it is a feasible approach to remove any existing callbacks
                         * as soon as it is collapsed, the callback number will always remain
                         * one
                         *
                         * What makes this approach a slightly less reliable is because so
                         * many presumption has been taken here
                         */
                        backPress?.onBackPressed()
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                compassListScrollView.alpha = slideOffset
                expandUp.alpha = 1 - slideOffset
                dim.alpha = slideOffset
                bottomSheetSlide.onBottomSheetSliding(slideOffset)
                toolbar.translationY = toolbar.height * -slideOffset
            }
        })

        locationBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null) {
                    when (intent.action) {
                        "location" -> {
                            val location: Location = intent.getParcelableExtra("location") ?: return
                            val geomagneticField = GeomagneticField(
                                    location.latitude.toFloat(),
                                    location.longitude.toFloat(),
                                    location.altitude.toFloat(),
                                    location.time
                            )

                            declination.text = fromHtml("<b>${getString(R.string.compass_declination)}</b> ${
                                round(
                                        geomagneticField.declination.toDouble(),
                                        2
                                )
                            }°")

                            inclinationTextView.text = fromHtml("<b>${getString(R.string.compass_inclination)}</b> ${
                                round(
                                        geomagneticField.inclination.toDouble(),
                                        2
                                )
                            }°")

                            fieldStrength.text = fromHtml("<b>${getString(R.string.compass_field_strength)}</b> ${
                                round(
                                        geomagneticField.fieldStrength.toDouble(),
                                        2
                                )
                            } nT")
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
        register()
    }

    override fun onPause() {
        super.onPause()
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
        handler.removeCallbacks(compassDialAnimationRunnable)
        objectAnimator?.removeAllListeners()
        objectAnimator?.cancel()
        dial.clearAnimation()
        compassInfoText.clearAnimation()
        handler.removeCallbacksAndMessages(null)
        unregister()
    }

    private fun register() {
        if (context == null) return
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(locationBroadcastReceiver, filter)
        if (haveAccelerometerSensor && haveMagnetometerSensor) {
            sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_GAME)
            sensorManager.registerListener(this, sensorMagneticField, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    private fun unregister() {
        if (context == null) return
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationBroadcastReceiver)
        if (haveAccelerometerSensor && haveMagnetometerSensor) {
            sensorManager.unregisterListener(this, sensorAccelerometer)
            sensorManager.unregisterListener(this, sensorMagneticField)
        }
    }

    private val textAnimationRunnable: Runnable = Runnable { compassInfoText.setTextAnimation(getString(R.string.compass_info), 300) }

    private inner class MyOnTouchListener : View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isUserRotatingDial = true
                    objectAnimator?.removeAllListeners()
                    objectAnimator?.cancel()
                    dial.clearAnimation()
                    handler.removeCallbacks(compassDialAnimationRunnable)
                    lastDialAngle = dial.rotation //if (dial.rotation < -180) abs(dial.rotation) else dial.rotation
                    startAngle = getAngle(event.x.toDouble(), event.y.toDouble(), dialContainer.width.toFloat(), dialContainer.height.toFloat())
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val currentAngle = getAngle(event.x.toDouble(), event.y.toDouble(), dialContainer.width.toFloat(), dialContainer.height.toFloat())
                    val finalAngle = currentAngle - startAngle + lastDialAngle
                    viewRotation(abs(finalAngle.normalizeEulerAngle(inverseResult = true)))
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    handler.postDelayed(compassDialAnimationRunnable, 1000)
                    return true
                }
            }
            return true
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

        if (!isUserRotatingDial) {
            rotationAngle = CompassAzimuth.calculate(gravity = accelerometer, magneticField = magnetometer)
            viewRotation(rotationAngle)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

        if (!haveMagnetometerSensor && !haveAccelerometerSensor) {
            accuracyMagnetometer.text = fromHtml("<b>${getString(R.string.magnetometer_accuracy)}</b> ${getString(R.string.not_available)}")
            accuracyAccelerometer.text = fromHtml("<b>${getString(R.string.accelerometer_accuracy)}</b> ${getString(R.string.not_available)}")
            return
        }

        if (sensor == sensorAccelerometer) {
            when (accuracy) {
                SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                    accuracyMagnetometer.text = fromHtml("<b>${getString(R.string.magnetometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_unreliable)}")
                    openCalibrationDialog()
                }
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                    accuracyMagnetometer.text = fromHtml("<b>${getString(R.string.magnetometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_low)}")
                    openCalibrationDialog()
                }
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                    accuracyMagnetometer.text = fromHtml("<b>${getString(R.string.magnetometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_medium)}")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                    accuracyMagnetometer.text = fromHtml("<b>${getString(R.string.magnetometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_high)}")
                }
            }
        }

        if (sensor == sensorAccelerometer) {
            when (accuracy) {
                SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                    accuracyAccelerometer.text = fromHtml("<b>${getString(R.string.accelerometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_unreliable)}")
                    openCalibrationDialog()
                }
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                    accuracyAccelerometer.text = fromHtml("<b>${getString(R.string.accelerometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_low)}")
                    openCalibrationDialog()
                }
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                    accuracyAccelerometer.text = fromHtml("<b>${getString(R.string.accelerometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_medium)}")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                    accuracyAccelerometer.text = fromHtml("<b>${getString(R.string.accelerometer_accuracy)}</b> ${getString(R.string.sensor_accuracy_high)}")
                }
            }
        }
    }

    private fun viewRotation(rotationAngle: Float) {
        dial.rotation = rotationAngle * -1

        if (CompassPreference.isFlowerBloomOn()) {
            flowerOne.rotation = rotationAngle * 2
            flowerTwo.rotation = rotationAngle * -3 + 45
            flowerThree.rotation = rotationAngle * 1 + 90
            flowerFour.rotation = rotationAngle * -4 + 135
        }

        degrees.text = StringBuilder().append(abs(rotationAngle.toInt())).append("°")

        direction.text = if (showDirectionCode) {
            getDirectionCodeFromAzimuth(requireContext(), azimuth = rotationAngle.toDouble()).toUpperCase(Locale.getDefault())
        } else {
            getDirectionNameFromAzimuth(requireContext(), azimuth = rotationAngle.toDouble()).toUpperCase(Locale.getDefault())
        }
    }

    private fun setSpeed(value: Float) {
        readingsAlpha = value
    }

    private fun setFlower(value: Boolean) {
        CompassPreference.setFlowerBloom(value)
        val x = compassBloomRes[CompassPreference.getFlowerBloomTheme()]
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
            degrees.animateColorChange(compassBloomTextColor[CompassPreference.getFlowerBloomTheme()])
        }
    }

    private fun setFlowerTheme(value: Int) {
        CompassPreference.setFlowerBloom(value)
        setFlower(value = CompassPreference.isFlowerBloomOn())
        flowerBloom = value
    }

    private fun backPressed(value: Boolean) {
        backPress?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(value) {
            override fun handleOnBackPressed() {
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }

                remove()
            }
        })
    }

    private val compassDialAnimationRunnable = Runnable {
        objectAnimator = ObjectAnimator.ofFloat(dial, "rotation", dial.rotation, rotationAngle * -1)
        objectAnimator!!.duration = 1000L
        objectAnimator!!.interpolator = DecelerateInterpolator()
        objectAnimator!!.setAutoCancel(true)
        objectAnimator!!.addUpdateListener { animation -> viewRotation(abs(animation.getAnimatedValue("rotation") as Float)) }
        objectAnimator!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {}
            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                isUserRotatingDial = false
            }
        })
        objectAnimator!!.start()
    }

    private fun openCalibrationDialog() {
        if (calibrationDialog == null) {
            calibrationDialog = CompassCalibration().newInstance()
            calibrationDialog!!.show(parentFragmentManager, "calibration_dialog")
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            CompassPreference.compassSpeed -> {
                setSpeed(CompassPreference.getCompassSpeed())
            }
            CompassPreference.direction_code -> {
                showDirectionCode = CompassPreference.getDirectionCode()
            }
            CompassPreference.flowerBloomTheme -> {
                setFlowerTheme(CompassPreference.getFlowerBloomTheme())
            }
            CompassPreference.flowerBloom -> {
                setFlower(CompassPreference.isFlowerBloomOn())
            }
        }
    }
}
