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
import app.simple.positional.preference.CompassPreference
import app.simple.positional.util.*
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.bottom_sheet_compass.*
import kotlinx.android.synthetic.main.frag_compass.*
import kotlinx.android.synthetic.main.generic_compass_rose.*
import kotlinx.android.synthetic.main.info_panel_compass.*
import java.lang.ref.WeakReference
import java.util.*

class Compass : Fragment(), SensorEventListener {

    private var startAngle: Double = 0.0

    private var handler = Handler(Looper.getMainLooper())

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<CoordinatorLayout>
    private lateinit var bottomSheetSlide: BottomSheetSlide
    private lateinit var scrollView: NestedScrollView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var expandUp: ImageView

    private var backPress: OnBackPressedDispatcher? = null

    private lateinit var degrees: TextView
    private lateinit var dialContainer: FrameLayout

    private val accelerometerReadings = FloatArray(3)
    private val magnetometerReadings = FloatArray(3)

    private val orientation = FloatArray(3)
    private val rotation = FloatArray(9)
    private val inclination = FloatArray(9)

    var haveAccelerometerSensor = false
    var haveMagnetometerSensor = false

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
    private var flowerBloom = 0
    var showDirectionCode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.frag_compass, container, false)

        filter.addAction("location")

        showDirectionCode = CompassPreference().getDirectionCode(requireContext())

        degrees = v.findViewById(R.id.degrees)

        dialContainer = v.findViewById(R.id.dial_container)

        bottomSheetSlide = requireActivity() as BottomSheetSlide

        backPress = requireActivity().onBackPressedDispatcher

        bottomSheetBehavior = BottomSheetBehavior.from(v.findViewById(R.id.compass_info_bottom_sheet))

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

                if (CompassPreference().isNoSensorAlertON(requireContext())) {
                    val noSensorAlert = NoSensorAlert().newInstance("compass")
                    noSensorAlert.show(parentFragmentManager, "no_sensor_alert")
                }
            }
        } catch (e: NullPointerException) {
            haveAccelerometerSensor = false
            haveMagnetometerSensor = false
        }

        isFLowerBlooming = CompassPreference().isFlowerBloom(requireContext())
        flowerBloom = CompassPreference().getFlowerBloomTheme(requireContext())
        setSpeed(CompassPreference().getCompassSpeed(requireContext()))

        return v
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        compass_main_layout.setProxyView(view)

        setFlower(isFLowerBlooming)

        loadImageResources(R.drawable.compass_dial, dial, requireContext(), 0)

        dialContainer.setOnTouchListener(MyOnTouchListener())

        compass_menu.setOnClickListener {
            val weakReference = WeakReference(CompassMenu(WeakReference(this@Compass)))
            weakReference.get()?.show(parentFragmentManager, "compass_menu")
        }

        compass_copy.setOnClickListener {
            val clipboard: ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            val stringBuilder = StringBuilder()

            stringBuilder.append("Compass Info\n\nAccuracy\n")
            stringBuilder.append("${compass_accuracy_accelerometer.text}\n")
            stringBuilder.append("${compass_accuracy_magnetometer.text}\n")
            stringBuilder.append("\nMagnetic Field\n")
            stringBuilder.append("${compass_inclination.text}\n")
            stringBuilder.append("${compass_declination.text}\n")
            stringBuilder.append("${compass_field_strength.text}\n\n")

            if (BuildConfig.FLAVOR == "lite") {
                stringBuilder.append("\n\n")
                stringBuilder.append("Information is copied using Positional Lite\n")
                stringBuilder.append("Get the app from:\nhttps://play.google.com/store/apps/details?id=app.simple.positional.lite")
            }

            val clip: ClipData = ClipData.newPlainText("Time Data", stringBuilder)
            clipboard.setPrimaryClip(clip)

            if (clipboard.hasPrimaryClip()) {
                compass_info_text.setTextAnimation(getString(R.string.info_copied), 300)

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
                compass_list_scroll_view.alpha = slideOffset
                expand_up_compass_sheet.alpha = (1 - slideOffset)
                compass_dim.alpha = slideOffset
                bottomSheetSlide.onBottomSheetSliding(slideOffset)
                compass_appbar.translationY = (compass_appbar.height * -slideOffset)
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

                            compass_declination.text = fromHtml("<b>Declination:</b> ${
                                round(
                                        geomagneticField.declination.toDouble(),
                                        2
                                )
                            }°")

                            compass_inclination.text = fromHtml("<b>Inclination:</b> ${
                                round(
                                        geomagneticField.inclination.toDouble(),
                                        2
                                )
                            }°")

                            compass_field_strength.text = fromHtml("<b>Field Strength:</b> ${
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
        compass_info_text.clearAnimation()
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

    private val textAnimationRunnable: Runnable = Runnable { compass_info_text.setTextAnimation("Compass Info", 300) }

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

            dial.rotation = rotationAngle

            val azimuth = (rotationAngle * -1) //- ((dial.rotation + 360) % 360).toInt()

            if (isFLowerBlooming) {
                when (flowerBloom) {
                    0 -> {
                        flower_one.rotation = rotationAngle * 2
                        flower_two.rotation = azimuth * -3 + 45
                        flower_three.rotation = rotationAngle * 1 + 90
                        flower_four.rotation = azimuth * -4 + 135
                    }
                    1 -> {
                        flower_one.rotation = rotationAngle * 2
                        flower_two.rotation = azimuth * -3 + 22.5f
                        flower_three.rotation = rotationAngle * 1 + 45f
                        flower_four.rotation = azimuth * -4 + 67.5f
                    }
                    2 -> {
                        flower_one.rotation = rotationAngle * 2
                        flower_two.rotation = azimuth * -3 + 45
                        flower_three.rotation = rotationAngle * 1 + 90
                        flower_four.rotation = azimuth * -4 + 135
                    }
                    3 -> {
                        flower_one.rotation = rotationAngle * 2
                        flower_two.rotation = azimuth * -3 + 45
                        flower_three.rotation = rotationAngle * 1 + 90
                        flower_four.rotation = azimuth * -4 + 135
                    }
                }
            }

            degrees.text = "${azimuth.toInt()}°"
            direction.text = if (showDirectionCode) {
                getDirectionCodeFromAzimuth(azimuth = azimuth.toDouble()).toUpperCase(Locale.getDefault())
            } else {
                getDirectionNameFromAzimuth(azimuth = azimuth.toDouble()).toUpperCase(Locale.getDefault())
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor == sensorAccelerometer) {
            when (accuracy) {
                SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                    compass_accuracy_magnetometer.text = fromHtml("<b>Magnetic Field</b>: Unreliable")
                    val weakReference = WeakReference(CompassCalibration().newInstance())
                    weakReference.get()?.show(parentFragmentManager, "null")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                    compass_accuracy_magnetometer.text = fromHtml("<b>Magnetic Field</b>: Low")
                    val weakReference = WeakReference(CompassCalibration().newInstance())
                    weakReference.get()?.show(parentFragmentManager, "null")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                    compass_accuracy_magnetometer.text = fromHtml("<b>Magnetic Field</b>: Medium")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                    compass_accuracy_magnetometer.text = fromHtml("<b>Magnetic Field</b>: High")
                }
            }
        }

        if (sensor == sensorAccelerometer) {
            when (accuracy) {
                SensorManager.SENSOR_STATUS_UNRELIABLE -> {
                    compass_accuracy_accelerometer.text = fromHtml("<b>Accelerometer</b>: Unreliable")
                    val weakReference = WeakReference(CompassCalibration().newInstance())
                    weakReference.get()?.show(parentFragmentManager, "null")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_LOW -> {
                    compass_accuracy_accelerometer.text = fromHtml("<b>Accelerometer</b>: Low")
                    val weakReference = WeakReference(CompassCalibration().newInstance())
                    weakReference.get()?.show(parentFragmentManager, "null")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> {
                    compass_accuracy_accelerometer.text = fromHtml("<b>Accelerometer</b>: Medium")
                }
                SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> {
                    compass_accuracy_accelerometer.text = fromHtml("<b>Accelerometer</b>: High")
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
        val x = compassBloomRes[CompassPreference().getFlowerBloomTheme(requireContext())]
        if (value) {
            loadImageResources(x, flower_one, requireContext(), 0)
            loadImageResources(x, flower_two, requireContext(), 50)
            loadImageResources(x, flower_three, requireContext(), 100)
            loadImageResources(x, flower_four, requireContext(), 150)
            animateColorChange(degrees, compassBloomTextColor[CompassPreference().getFlowerBloomTheme(requireContext())], Color.parseColor("#ffffff"))
        } else {
            loadImageResources(0, flower_one, requireContext(), 150)
            loadImageResources(0, flower_two, requireContext(), 100)
            loadImageResources(0, flower_three, requireContext(), 50)
            loadImageResources(0, flower_four, requireContext(), 0)
            animateColorChange(degrees, degrees.currentTextColor, compassBloomTextColor[CompassPreference().getFlowerBloomTheme(requireContext())])
        }
    }

    fun setFlowerTheme(value: Int) {
        CompassPreference().setFlowerBloom(value, requireContext())
        setFlower(value = CompassPreference().isFlowerBloom(requireContext()))
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