package app.simple.positional.ui

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
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.callbacks.BottomSheetSlide
import app.simple.positional.constants.compassBloomRes
import app.simple.positional.constants.compassBloomTextColor
import app.simple.positional.dialogs.compass.CompassCalibration
import app.simple.positional.dialogs.compass.CompassMenu
import app.simple.positional.dialogs.compass.NoSensorAlert
import app.simple.positional.preference.CompassPreference.getCompassSpeed
import app.simple.positional.preference.CompassPreference.getDirectionCode
import app.simple.positional.preference.CompassPreference.getFlowerBloomTheme
import app.simple.positional.preference.CompassPreference.isFlowerBloom
import app.simple.positional.preference.CompassPreference.isNoSensorAlertON
import app.simple.positional.preference.CompassPreference.setFlowerBloom
import app.simple.positional.util.*
import app.simple.positional.views.CustomCoordinatorLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.lang.ref.WeakReference
import java.util.*

class Compass : Fragment(), SensorEventListener {

    private var handler = Handler(Looper.getMainLooper())
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<CoordinatorLayout>
    private lateinit var bottomSheetSlide: BottomSheetSlide
    private var backPress: OnBackPressedDispatcher? = null

    private val accelerometerReadings = FloatArray(3)
    private val magnetometerReadings = FloatArray(3)
    private val orientation = FloatArray(3)
    private val rotation = FloatArray(9)
    private val inclination = FloatArray(9)

    private var haveAccelerometerSensor = false
    private var haveMagnetometerSensor = false
    private var isFLowerBlooming = false
    var showDirectionCode = true
    private var isUserRotatingDial = false

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
    private val twoPI = 2.0 * Math.PI
    private val degreesPerRadian = 180 / Math.PI
    private var rotationAngle = 0f
    private var flowerBloom = 0

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
    private lateinit var compassMainLayout: CustomCoordinatorLayout
    private lateinit var compassListScrollView: NestedScrollView
    private lateinit var dim: View
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

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
        compassMainLayout = view.findViewById(R.id.compass_main_layout)
        compassListScrollView = view.findViewById(R.id.compass_list_scroll_view)
        dim = view.findViewById(R.id.compass_dim)
        toolbar = view.findViewById(R.id.compass_appbar)

        filter.addAction("location")
        showDirectionCode = getDirectionCode()
        bottomSheetSlide = requireActivity() as BottomSheetSlide
        backPress = requireActivity().onBackPressedDispatcher
        bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.compass_info_bottom_sheet))
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager

        try {
            if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null && sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
                sensorMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
                sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                haveMagnetometerSensor = true
                haveAccelerometerSensor = true
            } else {
                haveAccelerometerSensor = false
                haveMagnetometerSensor = false

                if (isNoSensorAlertON()) {
                    val noSensorAlert = NoSensorAlert().newInstance("compass")
                    noSensorAlert.show(parentFragmentManager, "no_sensor_alert")
                }
            }
        } catch (e: NullPointerException) {
            haveAccelerometerSensor = false
            haveMagnetometerSensor = false
        }

        isFLowerBlooming = isFlowerBloom()
        flowerBloom = getFlowerBloomTheme()
        setSpeed(getCompassSpeed())

        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        compassMainLayout.setProxyView(view)

        setFlower(isFLowerBlooming)

        loadImageResources(R.drawable.compass_dial, dial, requireContext(), 0)

        dialContainer.setOnTouchListener(MyOnTouchListener())

        menu.setOnClickListener {
            val weakReference = WeakReference(CompassMenu(WeakReference(this@Compass)))
            weakReference.get()?.show(parentFragmentManager, "compass_menu")
        }

        copy.setOnClickListener {
            handler.removeCallbacks(textAnimationRunnable)
            val clipboard: ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            val stringBuilder = StringBuilder()

            stringBuilder.append("Compass Info\n\nAccuracy\n")
            stringBuilder.append("${accuracyAccelerometer.text}\n")
            stringBuilder.append("${accuracyMagnetometer.text}\n")
            stringBuilder.append("\nMagnetic Field\n")
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
                expandUp.alpha = (1 - slideOffset)
                dim.alpha = slideOffset
                bottomSheetSlide.onBottomSheetSliding(slideOffset)
                toolbar.translationY = (toolbar.height * -slideOffset)
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

                            declination.text = fromHtml("<b>Declination:</b> ${
                                round(
                                        geomagneticField.declination.toDouble(),
                                        2
                                )
                            }°")

                            inclinationTextView.text = fromHtml("<b>Inclination:</b> ${
                                round(
                                        geomagneticField.inclination.toDouble(),
                                        2
                                )
                            }°")

                            fieldStrength.text = fromHtml("<b>Field Strength:</b> ${
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
        register()
    }

    override fun onPause() {
        super.onPause()
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

    private val textAnimationRunnable: Runnable = Runnable { compassInfoText.setTextAnimation("Compass Info", 300) }

    private inner class MyOnTouchListener : View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isUserRotatingDial = true
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val currentAngle = getAngle(event.x.toDouble(), event.y.toDouble(), dial.width.toFloat(), dial.height.toFloat())
                    rotateDial((360.0f - currentAngle).toFloat())
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    isUserRotatingDial = false
                    return true
                }
            }
            return true
        }
    }

    private fun rotateDial(degrees: Float) {
        dial.rotation = degrees
        viewRotation((degrees - 360.0f) * -1)
    }

    override fun onSensorChanged(event: SensorEvent?) {

        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> smoothAndSetReadings(accelerometerReadings, event.values, readingsAlpha)
            Sensor.TYPE_MAGNETIC_FIELD -> smoothAndSetReadings(magnetometerReadings, event.values, readingsAlpha)
        }

        val successfullyCalculatedRotationMatrix = SensorManager.getRotationMatrix(this.rotation, inclination, accelerometerReadings, magnetometerReadings)

        if (successfullyCalculatedRotationMatrix) {
            SensorManager.getOrientation(this.rotation, orientation)

            try {
                if (isUserRotatingDial) {
                    /* no-op */
                } else {
                    rotationAngle = adjustAzimuthForDisplayRotation(
                            -(((((orientation[0] + twoPI) % twoPI * degreesPerRadian).toFloat())) + 360) % 360,
                            requireActivity().windowManager
                    )

                    viewRotation(rotationAngle * -1)

                    /**
                     * Still testing this, currently it partially works
                     *
                     * the problem with the above algorithm is it only measures the direction correctly when device is facing up
                     * This method approximately calculates if the device is facing up or down and that is by
                     * comparing the z value to the x and y values. If the z value dominates or > 0 then device is facing up
                     *
                     * If device is facing down, then value calculated gives a negative result
                     *
                     * Alternative method would be to simple check [accelerometerReadings] z value and if it is positive device is facing up
                     * if negative then device is facing down
                     *
                     * Value 1.0e-6 is there to prevent accidentally dividing by zero when device is exactly perpendicular to the gravity
                     */
                    // rotationAngle += if (accelerometerReadings[2] / sqrt(accelerometerReadings[0].pow(2) + accelerometerReadings[1].pow(2) + accelerometerReadings[2].pow(2) + 1.0e-6) > 0) {
                    //    0f
                    // } else {
                    //    180f
                    // }
                }
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor == sensorAccelerometer) {
            when (accuracy) {
                SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                    accuracyMagnetometer.text = fromHtml("<b>Magnetic Field</b>: Unreliable")
                    val weakReference = WeakReference(CompassCalibration().newInstance())
                    weakReference.get()?.show(parentFragmentManager, "null")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                    accuracyMagnetometer.text = fromHtml("<b>Magnetic Field</b>: Low")
                    val weakReference = WeakReference(CompassCalibration().newInstance())
                    weakReference.get()?.show(parentFragmentManager, "null")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                    accuracyMagnetometer.text = fromHtml("<b>Magnetic Field</b>: Medium")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                    accuracyMagnetometer.text = fromHtml("<b>Magnetic Field</b>: High")
                }
            }
        }

        if (sensor == sensorAccelerometer) {
            when (accuracy) {
                SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                    accuracyAccelerometer.text = fromHtml("<b>Accelerometer</b>: Unreliable")
                    val weakReference = WeakReference(CompassCalibration().newInstance())
                    weakReference.get()?.show(parentFragmentManager, "null")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                    accuracyAccelerometer.text = fromHtml("<b>Accelerometer</b>: Low")
                    val weakReference = WeakReference(CompassCalibration().newInstance())
                    weakReference.get()?.show(parentFragmentManager, "null")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                    accuracyAccelerometer.text = fromHtml("<b>Accelerometer</b>: Medium")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                    accuracyAccelerometer.text = fromHtml("<b>Accelerometer</b>: High")
                }
            }
        }
    }

    private fun viewRotation(rotationAngle: Float) {
        dial.rotation = rotationAngle * -1

        if (isFLowerBlooming) {
            when (flowerBloom) {
                0 -> {
                    flowerOne.rotation = rotationAngle * 2
                    flowerTwo.rotation = rotationAngle * -3 + 45
                    flowerThree.rotation = rotationAngle * 1 + 90
                    flowerFour.rotation = rotationAngle * -4 + 135
                }
                1 -> {
                    flowerOne.rotation = rotationAngle * 2
                    flowerTwo.rotation = rotationAngle * -3 + 22.5f
                    flowerThree.rotation = rotationAngle * 1 + 45f
                    flowerFour.rotation = rotationAngle * -4 + 67.5f
                }
                2 -> {
                    flowerOne.rotation = rotationAngle * 2
                    flowerTwo.rotation = rotationAngle * -3 + 45
                    flowerThree.rotation = rotationAngle * 1 + 90
                    flowerFour.rotation = rotationAngle * -4 + 135
                }
                3 -> {
                    flowerOne.rotation = rotationAngle * 2
                    flowerTwo.rotation = rotationAngle * -3 + 45
                    flowerThree.rotation = rotationAngle * 1 + 90
                    flowerFour.rotation = rotationAngle * -4 + 135
                }
            }
        }

        degrees.text = StringBuilder().append(rotationAngle.toInt()).append("°")

        direction.text = if (showDirectionCode) {
            getDirectionCodeFromAzimuth(azimuth = rotationAngle.toDouble()).toUpperCase(Locale.getDefault())
        } else {
            getDirectionNameFromAzimuth(azimuth = rotationAngle.toDouble()).toUpperCase(Locale.getDefault())
        }
    }

    fun setSpeed(value: Float) {
        readingsAlpha = value
    }

    fun setFlower(value: Boolean) {
        isFLowerBlooming = value
        val x = compassBloomRes[getFlowerBloomTheme()]
        if (value) {
            loadImageResources(x, flowerOne, requireContext(), 0)
            loadImageResources(x, flowerTwo, requireContext(), 50)
            loadImageResources(x, flowerThree, requireContext(), 100)
            loadImageResources(x, flowerFour, requireContext(), 150)
            animateColorChange(degrees, compassBloomTextColor[getFlowerBloomTheme()], Color.parseColor("#ffffff"))
        } else {
            loadImageResources(0, flowerOne, requireContext(), 150)
            loadImageResources(0, flowerTwo, requireContext(), 100)
            loadImageResources(0, flowerThree, requireContext(), 50)
            loadImageResources(0, flowerFour, requireContext(), 0)
            animateColorChange(degrees, degrees.currentTextColor, compassBloomTextColor[getFlowerBloomTheme()])
        }
    }

    fun setFlowerTheme(value: Int) {
        setFlowerBloom(value)
        setFlower(value = isFlowerBloom())
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
}