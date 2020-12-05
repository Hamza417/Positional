package app.simple.positional.ui

import android.content.*
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
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
import app.simple.positional.preference.MainPreferences
import app.simple.positional.util.*
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.frag_clock.*
import kotlinx.android.synthetic.main.info_panel_clock.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private var backPress: OnBackPressedDispatcher? = null
    private var filter: IntentFilter = IntentFilter()
    private lateinit var locationBroadcastReceiver: BroadcastReceiver
    private lateinit var bottomSheetSlide: BottomSheetSlide

    var delay: Long = 1000
    private var moonImageCountViolation = 1
    private var dayNightIndicatorImageCountViolation = 1
    private var isMetric = true
    private var isCustomCoordinate = false
    private var customLatitude = 0.0
    private var customLongitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.frag_clock, container, false)

        handler = Handler(Looper.getMainLooper())
        filter.addAction("location")
        hour = view.findViewById(R.id.hour)
        minutes = view.findViewById(R.id.minutes)
        seconds = view.findViewById(R.id.seconds)
        dial = view.findViewById(R.id.clock_face)
        menu = view.findViewById(R.id.clock_menu)
        bottomSheetSlide = requireActivity() as BottomSheetSlide
        scrollView = view.findViewById(R.id.clock_panel_scrollview)
        scrollView.alpha = 0f
        toolbar = view.findViewById(R.id.clock_appbar)
        expandUp = view.findViewById(R.id.expand_up_clock_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.clock_info_bottom_sheet))

        backPress = requireActivity().onBackPressedDispatcher

        setMotionDelay(ClockPreferences().getMovementType(requireContext()))

        isMetric = MainPreferences().getUnit(requireContext())
        isCustomCoordinate = MainPreferences().isCustomCoordinate(requireContext())

        if (isCustomCoordinate) {
            customLatitude = MainPreferences().getCoordinates(requireContext())[0].toDouble()
            customLongitude = MainPreferences().getCoordinates(requireContext())[1].toDouble()
        }

        setSkins()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isCustomCoordinate) {
            specified_location_notice_clock.visibility = View.VISIBLE
            clock_divider.visibility = View.VISIBLE
        }

        clock_main_layout.setProxyView(view)

        loadImageResourcesWithoutAnimation(R.drawable.clock_face, clock_face, requireContext())
        loadImageResourcesWithoutAnimation(R.drawable.clock_trail, sweep_seconds, requireContext())

        calendar = Calendar.getInstance()
        updateDigitalTime(calendar)

        locationBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null) {
                    when (intent.action) {
                        "location" -> {
                            if (isCustomCoordinate) return
                            val location: Location = intent.getParcelableExtra("location") ?: return
                            calculateAndUpdateData(location.latitude, location.longitude)
                        }
                    }
                }
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
                scrollView.alpha = slideOffset
                expandUp.alpha = (1 - slideOffset)
                //clockLayout.translationY = 150 * -slideOffset
                //clockLayout.alpha = (1 - slideOffset)
                view.findViewById<View>(R.id.clock_dim).alpha = slideOffset
                bottomSheetSlide.onBottomSheetSliding(slideOffset)
                toolbar.translationY = (toolbar.height * -slideOffset)
            }
        })

        menu.setOnClickListener {
            val clockMenu = WeakReference(ClockMenu(WeakReference(this)))
            clockMenu.get()?.show(parentFragmentManager, "null")
        }

        clock_copy.setOnClickListener {
            handler.removeCallbacks(textAnimationRunnable)
            val clipboard: ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            val stringBuilder = StringBuilder()

            stringBuilder.append("Local Time\n")
            stringBuilder.append("${local_timezone.text}\n")
            stringBuilder.append("${digital_time_24_hour.text}\n")
            stringBuilder.append("${digital_time_12_hour.text}\n")
            stringBuilder.append("${local_day.text}\n")
            stringBuilder.append("${local_date.text}\n")
            stringBuilder.append("${local_day_of_the_year.text}\n")
            stringBuilder.append("${local_week_of_the_year.text}\n\n")

            stringBuilder.append("UTC Time\n")
            stringBuilder.append("${time_zone_utc.text}\n")
            stringBuilder.append("${time_utc.text}\n")
            stringBuilder.append("${date_utc.text}\n\n")

            if (isCustomCoordinate) {
                stringBuilder.append(specified_location_notice_clock.text)
                stringBuilder.append("\n")
            }

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

                stringBuilder.append("Moon Dates\n")
                stringBuilder.append("${next_new_moon.text}\n")
                stringBuilder.append("${next_full_moon.text}\n")
                stringBuilder.append("${next_first_quarter.text}\n")
                stringBuilder.append("${next_last_quarter.text}\n")
            }

            if (BuildConfig.FLAVOR == "lite") {
                stringBuilder.append("\n\n")
                stringBuilder.append("Information is copied using Positional Lite\n")
                stringBuilder.append("Get the app from:\nhttps://play.google.com/store/apps/details?id=app.simple.positional.lite")
            }

            val clip: ClipData = ClipData.newPlainText("Time Data", stringBuilder)
            clipboard.setPrimaryClip(clip)

            if (clipboard.hasPrimaryClip()) {
                clock_info_text.setTextAnimation(getString(R.string.info_copied), 300)
                handler.postDelayed(textAnimationRunnable, 3000)
            }
        }
    }

    private val textAnimationRunnable: Runnable = Runnable { clock_info_text.setTextAnimation("Time Info", 300) }

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
                val calendar = calendar.get(Calendar.HOUR_OF_DAY)
                if (calendar < 7 || calendar > 18) {
                    day_night_indicator.setImageResource(R.drawable.ic_night)
                } else if (calendar < 18 || calendar > 6) {
                    day_night_indicator.setImageResource(R.drawable.ic_day)
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

    private val customDataUpdater: Runnable = object : Runnable {
        override fun run() {
            calculateAndUpdateData(customLatitude, customLongitude)
            handler.postDelayed(this, 2500)
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
        handler.removeCallbacks(textAnimationRunnable)
        handler.removeCallbacks(customDataUpdater)
        clock_info_text.clearAnimation()
        if (backPress!!.hasEnabledCallbacks()) {
            backPressed(false)
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(locationBroadcastReceiver, filter)
        handler.post(clock)
        handler.post(calender)
        if (isCustomCoordinate) {
            handler.post(customDataUpdater)
        }
    }

    private fun setSkins() {
        setNeedle(ClockPreferences().getClockNeedleTheme(requireContext()))
    }

    fun setNeedle(value: Int) {
        loadImageResources(clockNeedleSkins[value][0], hour, requireContext(), 0)
        loadImageResources(clockNeedleSkins[value][1], minutes, requireContext(), 100)
        loadImageResources(clockNeedleSkins[value][2], seconds, requireContext(), 200)
    }

    fun setMotionDelay(value: Boolean) {
        delay = if (value) {
            (1000 / getDisplayRefreshRate(requireContext(), requireActivity())!!).toLong()
        } else {
            1000
        }
    }

    private fun calculateAndUpdateData(latitude: Double, longitude: Double) {
        CoroutineScope(Dispatchers.Default).launch {
            // Set and Rise
            val timezone = if (isCustomCoordinate) {
                if (MainPreferences().getTimeZone(requireContext()) != "") {
                    MainPreferences().getTimeZone(requireContext())
                } else {
                    Calendar.getInstance().timeZone.id
                }
            } else {
                Calendar.getInstance().timeZone.id
            }

            val sunTimes = SunTimes.compute().on(Instant.now()).timezone(timezone).latitude(latitude).longitude(longitude).execute()

            val pattern: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

            val sunrise = fromHtml("<b>Sunrise:</b> ${pattern.format(sunTimes.rise)}")
            val sunset = fromHtml("<b>Sunset:</b> ${pattern.format(sunTimes.set)}")
            val sunNoon = fromHtml("<b>Noon:</b> ${pattern.format(sunTimes.noon)}")
            val sunNadir = fromHtml("<b>Nadir:</b> ${pattern.format(sunTimes.nadir)}")

            val moonTimes = MoonTimes.compute().on(Instant.now()).timezone(timezone).latitude(latitude).longitude(longitude).execute()
            val moonrise = fromHtml("<b>Moonrise:</b> ${pattern.format(moonTimes.rise)}")
            val moonset = fromHtml("<b>Moonset:</b> ${pattern.format(moonTimes.set)}")

            val twilightTimes = SunTimes.compute().on(Instant.now()).timezone(timezone).latitude(latitude).longitude(longitude)
            val astronomicalDawn = fromHtml("<b>Astronomical Dawn:</b> ${formatZonedTimeDate(twilightTimes.twilight(SunTimes.Twilight.ASTRONOMICAL).execute().rise.toString())}")
            val astronomicalDusk = fromHtml("<b>Astronomical Dusk:</b> ${formatZonedTimeDate(twilightTimes.twilight(SunTimes.Twilight.ASTRONOMICAL).execute().set.toString())}")
            val nauticalDawn = fromHtml("<b>Nautical Dawn:</b> ${formatZonedTimeDate(twilightTimes.twilight(SunTimes.Twilight.NAUTICAL).execute().rise.toString())}")
            val nauticalDusk = fromHtml("<b>Nautical Dusk:</b> ${formatZonedTimeDate(twilightTimes.twilight(SunTimes.Twilight.NAUTICAL).execute().set.toString())}")
            val civilDawn = fromHtml("<b>Civil Dawn:</b> ${formatZonedTimeDate(twilightTimes.twilight(SunTimes.Twilight.CIVIL).execute().rise.toString())}")
            val civilDusk = fromHtml("<b>Civil Dusk:</b> ${formatZonedTimeDate(twilightTimes.twilight(SunTimes.Twilight.CIVIL).execute().set.toString())}")

            // Position
            val sunPosition: SunPosition = SunPosition.compute().timezone(timezone).on(Instant.now()).at(latitude, longitude).execute()

            val sunAzimuth = fromHtml("<b>Azimuth:</b> ${round(sunPosition.azimuth, 2)}° ${getDirectionCodeFromAzimuth(sunPosition.azimuth)}")
            val sunAltitude = fromHtml("<b>Altitude:</b> ${round(sunPosition.trueAltitude, 2)}°")
            val sunDistance = if (isMetric) {
                fromHtml("<b>Distance:</b> ${String.format("%.3E", sunPosition.distance)} km")
            } else {
                fromHtml("<b>Distance:</b> ${String.format("%.3E", sunPosition.distance.toMiles())} miles")
            }

            val moonPosition: MoonPosition = MoonPosition.compute().timezone(timezone).at(latitude, longitude).execute()

            val moonAzimuth = fromHtml("<b>Azimuth:</b> ${round(moonPosition.azimuth, 2)}° ${getDirectionCodeFromAzimuth(moonPosition.azimuth)}")
            val moonAltitude = fromHtml("<b>Altitude:</b> ${round(moonPosition.altitude, 2)}°")
            val moonDistance = if (isMetric) {
                fromHtml("<b>Distance:</b> ${String.format("%.3E", moonPosition.distance)} km")
            } else {
                fromHtml("<b>Distance:</b> ${String.format("%.3E", moonPosition.distance.toMiles())} miles")
            }
            val moonParallacticAngle = fromHtml("<b>Parallactic Angle:</b> ${round(moonPosition.parallacticAngle, 2)}°")

            val moonIllumination = MoonIllumination.compute().on(Instant.now()).timezone(timezone).execute()
            val moonFraction = fromHtml("<b>Fraction: </b> ${round(moonIllumination.fraction, 2)}")
            val moonAngle = fromHtml("<b>Angle:</b> ${round(moonIllumination.angle, 2)}°")
            val moonAngleState = fromHtml("<b>Angle State:</b> ${if (moonIllumination.angle < 0) "Waxing" else "Waning"}")
            val moonPhase = fromHtml("<b>Phase:</b> ${getMoonPhase(moonIllumination.phase)}")
            val moonPhaseAngle = fromHtml("<b>Phase Angle:</b> ${round(moonIllumination.phase, 2)}°")

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

            val nextFullMoon = fromHtml("<b>Full Moon:</b> ${formatMoonDate(MoonPhase.compute().on(Instant.now()).phase(MoonPhase.Phase.FULL_MOON).execute().time.toString())}")
            val nextNewMoon = fromHtml("<b>New Moon:</b> ${formatMoonDate(MoonPhase.compute().on(Instant.now()).phase(MoonPhase.Phase.NEW_MOON).execute().time.toString())}")
            val nextFirstQuarter = fromHtml("<b>First Quarter:</b> ${formatMoonDate(MoonPhase.compute().on(Instant.now()).phase(MoonPhase.Phase.FIRST_QUARTER).execute().time.toString())}")
            val nextLastQuarter = fromHtml("<b>Last Quarter:</b> ${formatMoonDate(MoonPhase.compute().on(Instant.now()).phase(MoonPhase.Phase.LAST_QUARTER).execute().time.toString())}")

            withContext(Dispatchers.Main) {
                try {
                    sunrise_time.text = sunrise
                    sunset_time.text = sunset
                    sun_noon.text = sunNoon
                    sun_nadir.text = sunNadir

                    moonrise_time.text = moonrise
                    moon_set_time.text = moonset

                    astronomical_dawm_twilight.text = astronomicalDawn
                    astronomical_dusk_twilight.text = astronomicalDusk
                    nautical_dawn_twilight.text = nauticalDawn
                    nautical_dusk_twilight.text = nauticalDusk
                    civil_dawn_twilight.text = civilDawn
                    civil_dusk_twilight.text = civilDusk

                    sun_azimuth.text = sunAzimuth
                    sun_altitude.text = sunAltitude
                    sun_distance.text = sunDistance

                    moon_azimuth.text = moonAzimuth
                    moon_altitude.text = moonAltitude
                    moon_distance.text = moonDistance
                    moon_parallactic_angle.text = moonParallacticAngle

                    moon_fraction.text = moonFraction
                    moon_angle.text = moonAngle
                    moon_angle_state.text = moonAngleState
                    moon_phase.text = moonPhase
                    moon_phase_angle.text = moonPhaseAngle
                    next_full_moon.text = nextFullMoon
                    next_new_moon.text = nextNewMoon
                    next_first_quarter.text = nextFirstQuarter
                    next_last_quarter.text = nextLastQuarter
                } catch (e: NullPointerException) {
                } catch (e: UninitializedPropertyAccessException) {
                }
            }
        }
    }

    private fun dateUpdater(calendar: Calendar) {
        CoroutineScope(Dispatchers.Default).launch {
            var localTimeZone: Spanned? = null
            var digitalTime24: Spanned? = null
            var digitalTime12: Spanned? = null
            var digitalTime: SpannableString? = null
            var localDate: Spanned? = null
            var dayOfTheYear: Spanned? = null
            var weekOfTheYear: Spanned? = null
            var localDay: Spanned? = null
            var utcTimeZone: Spanned? = null
            var utcTime: Spanned? = null
            var utcDate: Spanned? = null

            try {
                localTimeZone = fromHtml("<b>Time Zone: </b> ${TimeZone.getTimeZone(TimeZone.getDefault().id).getDisplayName(false, TimeZone.SHORT)}")
                digitalTime24 = fromHtml("<b>Time 24Hr:</b> ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(calendar.time)}")
                digitalTime12 = fromHtml("<b>Time 12Hr:</b> ${SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(calendar.time)}")
                digitalTime = getTime(requireContext(), calendar)
                utcTimeZone = fromHtml("<b>Time Zone:</b> ${"GMT ${SimpleDateFormat("XXX", Locale.getDefault()).format(calendar.time)}"}")
                val df = SimpleDateFormat("dd MMMM, yyyy", Locale.getDefault())
                localDate = fromHtml("<b>Date:</b> ${df.format(calendar.time)}")
                localDay = fromHtml("<b>Day:</b> ${SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)}")
                dayOfTheYear = fromHtml("<b>Day of Year:</b> ${calendar.get(Calendar.DAY_OF_YEAR).getOrdinal()}")
                weekOfTheYear = fromHtml("<b>Week of Year:</b> ${calendar.get(Calendar.WEEK_OF_YEAR).getOrdinal()}")
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(OffsetDateTime.now(ZoneOffset.UTC).toString())
                utcTime = fromHtml("<b>Time:</b> ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date!!)}")
                utcDate = fromHtml("<b>Date:</b> ${SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(date)}")
            } catch (e: ParseException) {
            }

            withContext(Dispatchers.Main) {
                try {
                    local_timezone.text = localTimeZone
                    digital_time_24_hour.text = digitalTime24
                    digital_time_12_hour.text = digitalTime12
                    digital_time_main.text = digitalTime
                    local_day.text = localDay
                    local_date.text = localDate
                    local_day_of_the_year.text = dayOfTheYear
                    local_week_of_the_year.text = weekOfTheYear
                    time_zone_utc.text = utcTimeZone
                    date_utc.text = utcDate
                    time_utc.text = utcTime
                } catch (e: NullPointerException) {
                } catch (e: UninitializedPropertyAccessException) {
                }
            }
        }
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