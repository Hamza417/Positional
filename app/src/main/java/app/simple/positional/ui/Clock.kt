package app.simple.positional.ui

import android.content.*
import android.content.res.Configuration
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.callbacks.BottomSheetSlide
import app.simple.positional.constants.clockNeedleSkins
import app.simple.positional.dialogs.clock.ClockMenu
import app.simple.positional.preference.ClockPreferences
import app.simple.positional.util.*
import com.elyeproj.loaderviewlibrary.LoaderTextView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.clock_info_cards.*
import kotlinx.android.synthetic.main.frag_clock.*
import org.shredzone.commons.suncalc.*
import java.lang.ref.WeakReference
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

class Clock : Fragment() {
    private lateinit var sunAzimuth: LoaderTextView
    private lateinit var sunDistance: LoaderTextView
    private lateinit var sunAltitude: LoaderTextView
    private lateinit var date: LoaderTextView

    private lateinit var toolbar: MaterialToolbar

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<CoordinatorLayout>

    private lateinit var calendar: Calendar

    private lateinit var hour: ImageView
    private lateinit var minutes: ImageView
    private lateinit var seconds: ImageView
    private lateinit var dial: ImageView
    private lateinit var expandUp: ImageView

    private lateinit var menu: ImageButton

    private lateinit var scrollView: NestedScrollView

    private lateinit var handler: Handler

    private var filter: IntentFilter = IntentFilter()
    private lateinit var locationBroadcastReceiver: BroadcastReceiver

    private lateinit var bottomSheetSlide: BottomSheetSlide

    var delay: Long = 1000

    private var moonImageCountViolation = 1
    private var dayNightIndicatorImageCountViolation = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.frag_clock, container, false)

        handler = Handler()

        filter.addAction("location")

        hour = view.findViewById(R.id.hour)
        minutes = view.findViewById(R.id.minutes)
        seconds = view.findViewById(R.id.seconds)
        dial = view.findViewById(R.id.clock_face)
        sunAzimuth = view.findViewById(R.id.sun_azimuth)
        sunDistance = view.findViewById(R.id.sun_distance)
        sunAltitude = view.findViewById(R.id.sun_altitude)
        date = view.findViewById(R.id.local_date)

        menu = view.findViewById(R.id.clock_menu)

        bottomSheetSlide = requireActivity() as BottomSheetSlide

        scrollView = view.findViewById(R.id.clock_panel_scrollview)

        setMotionDelay(ClockPreferences().getMovementType(requireContext()))

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            toolbar = view.findViewById(R.id.clock_appbar)

            scrollView.alpha = 0f

            expandUp = view.findViewById(R.id.expand_up_clock_sheet)
            bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.clock_info_bottom_sheet))
        }

        setSkins()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendar = Calendar.getInstance()
        updateDigitalTime(calendar)
        val df = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
        date.text = fromHtml("<b>Date:</b> ${df.format(calendar.time)}")

        locationBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null) {
                    when (intent.action) {

                        // TODO - Move all heavy time formatting into an asynchronous thread

                        "location" -> {
                            val location: Location = intent.getParcelableExtra("location") ?: return

                            // Set and Rise
                            run {
                                val sunTimes = SunTimes.compute().at(location.latitude, location.longitude).execute()

                                val pattern: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

                                sunrise_time.text = fromHtml("<b>Sunrise:</b> ${pattern.format(sunTimes.rise)}")
                                sunset_time.text = fromHtml("<b>Sunset:</b> ${pattern.format(sunTimes.set)}")
                                sun_noon.text = fromHtml("<b>Noon:</b> ${pattern.format(sunTimes.noon)}")
                                sun_nadir.text = fromHtml("<b>Nadir:</b> ${pattern.format(sunTimes.nadir)}")

                                val moonTimes = MoonTimes.compute().on(Instant.now()).latitude(location.latitude).longitude(location.longitude).execute()

                                moonrise_time.text = fromHtml("<b>Moonrise:</b> ${pattern.format(moonTimes.rise)}")
                                moon_set_time.text = fromHtml("<b>Moonset:</b> ${pattern.format(moonTimes.set)}")
                            }

                            run {
                                val sunTimes = SunTimes.compute().on(Instant.now()).latitude(location.latitude).longitude(location.longitude)
                                astronomical_dawm_twilight.text = fromHtml("<b>Astronomical Dawn:</b> ${formatZonedTimeDate(sunTimes.twilight(SunTimes.Twilight.ASTRONOMICAL).execute().rise.toString())}")
                                astronomical_dusk_twilight.text = fromHtml("<b>Astronomical Dusk:</b> ${formatZonedTimeDate(sunTimes.twilight(SunTimes.Twilight.ASTRONOMICAL).execute().set.toString())}")
                                nautical_dawn_twilight.text = fromHtml("<b>Nautical Dawn:</b> ${formatZonedTimeDate(sunTimes.twilight(SunTimes.Twilight.NAUTICAL).execute().rise.toString())}")
                                nautical_dusk_twilight.text = fromHtml("<b>Nautical Dusk:</b> ${formatZonedTimeDate(sunTimes.twilight(SunTimes.Twilight.NAUTICAL).execute().set.toString())}")
                                civil_dawn_twilight.text = fromHtml("<b>Civil Dawn:</b> ${formatZonedTimeDate(sunTimes.twilight(SunTimes.Twilight.CIVIL).execute().rise.toString())}")
                                civil_dusk_twilight.text = fromHtml("<b>Civil Dusk:</b> ${formatZonedTimeDate(sunTimes.twilight(SunTimes.Twilight.CIVIL).execute().set.toString())}")
                            }

                            run {
                                // Position
                                val sunPosition: SunPosition = SunPosition.compute().timezone(TimeZone.getDefault()).on(Instant.now()).at(location.latitude, location.longitude).execute()

                                sunAzimuth.text = fromHtml("<b>Azimuth:</b> ${round(sunPosition.azimuth, 2)}° ${getDirectionCodeFromAzimuth(sunPosition.azimuth)}")
                                sunAltitude.text = fromHtml("<b>Altitude:</b> ${round(sunPosition.trueAltitude, 2)}°")
                                sunDistance.text = fromHtml("<b>Distance:</b> ${String.format("%.3E", sunPosition.distance)} km")

                                val moonPosition: MoonPosition = MoonPosition.compute().at(location.latitude, location.longitude).execute()

                                moon_azimuth.text = fromHtml("<b>Azimuth:</b> ${round(moonPosition.azimuth, 2)}° ${getDirectionCodeFromAzimuth(moonPosition.azimuth)}")
                                moon_altitude.text = fromHtml("<b>Altitude:</b> ${round(moonPosition.altitude, 2)}°")
                                moon_distance.text = fromHtml("<b>Distance:</b> ${String.format("%.3E", moonPosition.distance)} km")
                                moon_parallactic_angle.text = fromHtml("<b>Parallactic Angle:</b> ${round(moonPosition.parallacticAngle, 2)}°")

                                val moonIllumination = MoonIllumination.compute().on(Instant.now()).execute()

                                moon_fraction.text = fromHtml("<b>Fraction: </b> ${round(moonIllumination.fraction, 2)}")
                                moon_angle.text = fromHtml("<b>Angle:</b> ${round(moonIllumination.angle, 2)}°")
                                moon_angle_state.text = fromHtml("<b>Angle State:</b> ${if (moonIllumination.angle < 0) "Waxing" else "Waning"}")
                                moon_phase.text = fromHtml("<b>Phase:</b> ${getMoonPhase(moonIllumination.phase)}")
                                moon_phase_angle.text = fromHtml("<b>Phase Angle:</b> ${round(moonIllumination.phase, 2)}°")

                                if (moonImageCountViolation != 0) {
                                    /**
                                     * [moonImageCountViolation] will prevent the moon image loading every time location updates
                                     *
                                     * Since the range of change is so small, its approximation/accuracy won't be affected in this tiny time frame
                                     * The value of the calculation is being trimmed to 2 decimal places anyway
                                     */
                                    loadImageResourcesWithoutAnimation(getMoonPhaseGraphics(round(moonIllumination.phase, 2)), moon_phase_graphics, requireContext())
                                    moonImageCountViolation = 0
                                }

                                next_full_moon.text = fromHtml("<b>Next Full Moon:</b> ${formatMoonDate(MoonPhase.compute().on(Instant.now()).phase(MoonPhase.Phase.FULL_MOON).execute().time.toString())}")
                            }
                        }
                    }
                }
            }
        }

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            clock_main_layout.setProxyView(view)

            bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {

                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    scrollView.alpha = slideOffset
                    expandUp.alpha = (1 - slideOffset)
                    //clockLayout.translationY = 150 * -slideOffset
                    //clockLayout.alpha = (1 - slideOffset)
                    view.findViewById<View>(R.id.clock_dim).alpha = slideOffset
                    bottomSheetSlide.onBottomSheetSliding(slideOffset)
                    toolbar.translationY = (toolbar.height * -slideOffset)
                }
            })
        }

        menu.setOnClickListener {
            val clockMenu = WeakReference(ClockMenu(WeakReference(this)))
            clockMenu.get()?.show(parentFragmentManager, "null")
        }

        clock_copy.setOnClickListener {
            val clipboard: ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            val stringBuilder = StringBuilder()

            stringBuilder.append("Local Time\n")
            stringBuilder.append("${local_timezone.text}\n")
            stringBuilder.append("${digital_time_24_hour.text}\n")
            stringBuilder.append("${digital_time_12_hour.text}\n")
            stringBuilder.append("${local_date.text}\n\n")

            stringBuilder.append("UTC Time\n")
            stringBuilder.append("${time_zone_utc.text}\n")
            stringBuilder.append("${time_utc.text}\n")
            stringBuilder.append("${date_utc.text}\n\n")

            if (sun_azimuth.text != "") {
                stringBuilder.append("Sun Position\n")
                stringBuilder.append("${sun_azimuth.text}\n")
                stringBuilder.append("${sun_distance.text}\n")
                stringBuilder.append("${sun_altitude.text}\n")
                stringBuilder.append("${sunrise_time.text}\n")
                stringBuilder.append("${sunset_time.text}\n")
                stringBuilder.append("${sun_nadir.text}\n")
                stringBuilder.append("${sun_noon.text}\n\n")

                stringBuilder.append("Twilight\n")
                stringBuilder.append("${astronomical_dawm_twilight.text}\n")
                stringBuilder.append("${astronomical_dusk_twilight.text}\n")
                stringBuilder.append("${nautical_dawn_twilight.text}\n")
                stringBuilder.append("${nautical_dusk_twilight.text}\n")
                stringBuilder.append("${civil_dusk_twilight.text}\n")
                stringBuilder.append("${civil_dawn_twilight.text}\n\n")

                stringBuilder.append("Moon Position\n")
                stringBuilder.append("${moon_azimuth.text}\n")
                stringBuilder.append("${moon_distance.text}\n")
                stringBuilder.append("${moon_altitude.text}\n")
                stringBuilder.append("${moon_parallactic_angle.text}\n")
                stringBuilder.append("${moonrise_time.text}\n")
                stringBuilder.append("${moon_set_time.text}\n")
                stringBuilder.append("${moon_fraction.text}\n")
                stringBuilder.append("${moon_angle.text}\n")
                stringBuilder.append("${moon_angle_state.text}\n")
                stringBuilder.append("${moon_phase.text}\n")
                stringBuilder.append("${moon_phase_angle.text}\n")
                stringBuilder.append("${next_full_moon.text}\n\n")
            }

            if (BuildConfig.FLAVOR == "lite") {
                stringBuilder.append("Information is copied using Positional\n")
                stringBuilder.append("Get the app from:\nhttps://play.google.com/store/apps/details?id=app.simple.positional")
            }

            val clip: ClipData = ClipData.newPlainText("Time Data", stringBuilder)
            clipboard.setPrimaryClip(clip)

            if (clipboard.hasPrimaryClip()) {
                clock_info_text.setTextAnimation(getString(R.string.info_copied), 300)

                handler.postDelayed({
                    clock_info_text.setTextAnimation("Time Info", 300)
                }, 3000)
            }
        }
    }

    private val clock: Runnable = object : Runnable {
        override fun run() {
            calendar = Calendar.getInstance()

            hour.rotation = getHoursInDegrees(calendar)
            minutes.rotation = getMinutesInDegrees(calendar)

            seconds.rotation = if (delay < 1000) {
                getSecondsInDegreesWithDecimalPrecision(calendar)
            } else {
                getSecondsInDegrees(calendar)
            }

            sweep_seconds.rotation = seconds.rotation - 90

            if (dayNightIndicatorImageCountViolation != 0) {
                if (calendar.get(Calendar.HOUR_OF_DAY) >= 6 || calendar.get(Calendar.HOUR_OF_DAY) <= 18) {
                    day_night_indicator.setImageResource(R.drawable.ic_day)
                } else {
                    day_night_indicator.setImageResource(R.drawable.ic_night)
                }

                // Setting this to zero will prevent the image from applying again every second
                dayNightIndicatorImageCountViolation = 0
            }

            handler.postDelayed(this, delay)
        }
    }

    private val calender: Runnable = object : Runnable {
        override fun run() {
            calendar = Calendar.getInstance()

            updateDigitalTime(calendar)

            handler.postDelayed(this, 1000)
        }
    }

    fun updateDigitalTime(calendar: Calendar) {
        dateUpdater(calendar)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationBroadcastReceiver)
        handler.removeCallbacks(clock)
        handler.removeCallbacks(calender)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(locationBroadcastReceiver, filter)
        handler.post(clock)
        handler.post(calender)
    }

    private fun setSkins() {
        setNeedle(ClockPreferences().getClockNeedleTheme(requireContext()))
        //setDial(ClockPreferences().getClockFaceTheme(requireContext()))
    }

    fun setNeedle(value: Int) {
        loadImageResources(clockNeedleSkins[value][0], hour, requireContext(), 0)
        loadImageResources(clockNeedleSkins[value][1], minutes, requireContext(), 100)
        loadImageResources(clockNeedleSkins[value][2], seconds, requireContext(), 200)
    }

    fun setFaceAlpha(value: Float) {
        dial.animate().alpha(value).setDuration(1500).setInterpolator(AccelerateDecelerateInterpolator()).start()
    }

    fun setMotionDelay(value: Boolean) {
        delay = if (value) {
            (1000 / getDisplayRefreshRate(requireContext(), requireActivity())!!).toLong()
        } else {
            1000
        }
    }

    @Suppress("deprecation")
    private fun dateUpdater(calendar: Calendar) {
        /**
         * TODO - Research more on this part
         * Since AsyncTask is deprecated from Android 11/API 30
         *
         * I am kind of curious and found out a lot of problems regarding
         * how [AsyncTask] can cause various problems
         *
         * Here the use case is pretty straight forward and I am only using it
         * for formatting my strings, it won't cause any such problems like
         * memory leaks or some serious exceptions so I am going to use it currently
         *
         */

        class GetData : AsyncTask<Void, Void, Array<String>>() {
            override fun doInBackground(vararg params: Void?): Array<String>? {
                return try {
                    val timeZone = TimeZone.getTimeZone(TimeZone.getDefault().id).getDisplayName(false, TimeZone.SHORT)
                    val digitalTime24 = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(calendar.time).toString()
                    val digitalTime12 = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(calendar.time).toString()
                    val digitalTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time).toString()
                    val digitalDaytime = SimpleDateFormat("a", Locale.getDefault()).format(calendar.time).toString().toUpperCase(Locale.getDefault())
                    val utcTimeZone = "GMT ${SimpleDateFormat("XXX", Locale.getDefault()).format(calendar.time)}"
                    val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(OffsetDateTime.now(ZoneOffset.UTC).toString())
                    val utcTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date!!)
                    val utcDate = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(date)

                    arrayOf(timeZone, digitalTime24, digitalTime12, digitalTime, digitalDaytime, utcTimeZone, utcTime, utcDate)
                } catch (e: ParseException) {
                    return null //arrayOf("error!!", "error!!", "error!!", "error!!", "error!!", "error!!", "error!!")
                }
            }

            override fun onPostExecute(result: Array<String>?) {
                super.onPostExecute(result)
                if (result == null) return
                local_timezone.text = fromHtml("<b>Time Zone: </b> ${result[0]}")
                digital_time_24_hour.text = fromHtml("<b>Time 24Hr:</b> ${result[1]}")
                digital_time_12_hour.text = fromHtml("<b>Time 12Hr:</b> ${result[2]}")
                digital_time_main.text = buildSpannableString(result[3].toUpperCase(Locale.getDefault()), 2)
                time_zone_utc.text = fromHtml("<b>Time Zone:</b> ${result[4]}")
                time_utc.text = fromHtml("<b>Time:</b> ${result[5]}")
                date_utc.text = fromHtml("<b>Date:</b> ${result[6]}")
            }
        }

        val getData = GetData()
        if (getData.status == AsyncTask.Status.RUNNING) {
            if (getData.cancel(true)) {
                getData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
        } else {
            getData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    }
}