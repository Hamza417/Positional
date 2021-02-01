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
import app.simple.positional.constants.ClockSkinsConstants.clockNeedleSkins
import app.simple.positional.dialogs.clock.ClockMenu
import app.simple.positional.math.MathExtensions.round
import app.simple.positional.math.TimeConverter.getHoursInDegrees
import app.simple.positional.math.TimeConverter.getMinutesInDegrees
import app.simple.positional.math.TimeConverter.getSecondsInDegrees
import app.simple.positional.math.TimeConverter.getSecondsInDegreesWithDecimalPrecision
import app.simple.positional.math.UnitConverter.toMiles
import app.simple.positional.preference.ClockPreferences
import app.simple.positional.preference.MainPreferences
import app.simple.positional.util.*
import app.simple.positional.util.AsyncImageLoader.loadImageResources
import app.simple.positional.util.AsyncImageLoader.loadImageResourcesWithoutAnimation
import app.simple.positional.util.DigitalTimeFormatter.getTime
import app.simple.positional.util.DigitalTimeFormatter.getTimeWithSeconds
import app.simple.positional.util.Direction.getDirectionCodeFromAzimuth
import app.simple.positional.util.HtmlHelper.fromHtml
import app.simple.positional.util.MoonAngle.getMoonPhase
import app.simple.positional.util.MoonAngle.getMoonPhaseGraphics
import app.simple.positional.util.MoonTimeFormatter.formatMoonDate
import app.simple.positional.views.CustomCoordinatorLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.*
import org.shredzone.commons.suncalc.*
import java.lang.Runnable
import java.lang.ref.WeakReference
import java.text.ParseException
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.IsoFields
import java.util.*

class Clock : Fragment() {

    fun newInstance(): Clock {
        val args = Bundle()
        val fragment = Clock()
        fragment.arguments = args
        return fragment
    }

    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<CoordinatorLayout>

    private lateinit var hour: ImageView
    private lateinit var minutes: ImageView
    private lateinit var seconds: ImageView
    private lateinit var face: ImageView
    private lateinit var expandUp: ImageView
    private lateinit var sweepSeconds: ImageView
    private lateinit var dayNightIndicator: ImageView
    private lateinit var moonPhaseGraphics: ImageView

    private lateinit var menu: ImageButton
    private lateinit var copyButton: ImageButton
    private lateinit var divider: View
    private lateinit var clockMainLayout: CustomCoordinatorLayout
    private lateinit var scrollView: NestedScrollView

    private lateinit var clockInfoText: TextView
    private lateinit var localTimeZone: TextView
    private lateinit var digitalTime24: TextView
    private lateinit var digitalTime12: TextView
    private lateinit var digitalTimeMain: TextView
    private lateinit var localDay: TextView
    private lateinit var localDate: TextView
    private lateinit var localDayOfTheYear: TextView
    private lateinit var localWeekOfTheYear: TextView
    private lateinit var utcTimeZone: TextView
    private lateinit var utcTime: TextView
    private lateinit var utcDate: TextView
    private lateinit var specifiedLocationNotice: TextView
    private lateinit var sunAzimuth: TextView
    private lateinit var sunDistance: TextView
    private lateinit var sunAltitude: TextView
    private lateinit var sunriseTime: TextView
    private lateinit var sunsetTime: TextView
    private lateinit var sunNadir: TextView
    private lateinit var sunNoon: TextView
    private lateinit var astronomicalDawnTwilight: TextView
    private lateinit var astronomicalDuskTwilight: TextView
    private lateinit var nauticalDawnTwilight: TextView
    private lateinit var nauticalDuskTwilight: TextView
    private lateinit var civilDawnTwilight: TextView
    private lateinit var civilDuskTwilight: TextView
    private lateinit var moonAzimuth: TextView
    private lateinit var moonDistance: TextView
    private lateinit var moonAltitude: TextView
    private lateinit var moonParallacticAngle: TextView
    private lateinit var moonriseTime: TextView
    private lateinit var moonsetTime: TextView
    private lateinit var moonFraction: TextView
    private lateinit var moonAngle: TextView
    private lateinit var moonAngleState: TextView
    private lateinit var moonPhase: TextView
    private lateinit var moonPhaseAngle: TextView
    private lateinit var nextNewMoon: TextView
    private lateinit var nextFullMoon: TextView
    private lateinit var nextFirstQuarter: TextView
    private lateinit var nextLastQuarter: TextView

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
    private var timezone: String? = "Asia/Tokyo"

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
        face = view.findViewById(R.id.clock_face)
        sweepSeconds = view.findViewById(R.id.sweep_seconds)
        dayNightIndicator = view.findViewById(R.id.day_night_indicator)
        moonPhaseGraphics = view.findViewById(R.id.moon_phase_graphics)

        menu = view.findViewById(R.id.clock_menu)
        copyButton = view.findViewById(R.id.clock_copy)
        divider = view.findViewById(R.id.clock_divider)
        clockMainLayout = view.findViewById(R.id.clock_main_layout)

        clockInfoText = view.findViewById(R.id.clock_info_text)
        localTimeZone = view.findViewById(R.id.local_timezone)
        digitalTime24 = view.findViewById(R.id.digital_time_24_hour)
        digitalTime12 = view.findViewById(R.id.digital_time_12_hour)
        digitalTimeMain = view.findViewById(R.id.digital_time_main)
        localDay = view.findViewById(R.id.local_day)
        localDate = view.findViewById(R.id.local_date)
        localDayOfTheYear = view.findViewById(R.id.local_day_of_the_year)
        localWeekOfTheYear = view.findViewById(R.id.local_week_of_the_year)
        utcTimeZone = view.findViewById(R.id.time_zone_utc)
        utcTime = view.findViewById(R.id.time_utc)
        utcDate = view.findViewById(R.id.date_utc)
        specifiedLocationNotice = view.findViewById(R.id.specified_location_notice_clock)
        sunAzimuth = view.findViewById(R.id.sun_azimuth)
        sunDistance = view.findViewById(R.id.sun_distance)
        sunAltitude = view.findViewById(R.id.sun_altitude)
        sunriseTime = view.findViewById(R.id.sunrise_time)
        sunsetTime = view.findViewById(R.id.sunset_time)
        sunNadir = view.findViewById(R.id.sun_nadir)
        sunNoon = view.findViewById(R.id.sun_noon)
        astronomicalDawnTwilight = view.findViewById(R.id.astronomical_dawn_twilight)
        astronomicalDuskTwilight = view.findViewById(R.id.astronomical_dusk_twilight)
        nauticalDawnTwilight = view.findViewById(R.id.nautical_dawn_twilight)
        nauticalDuskTwilight = view.findViewById(R.id.nautical_dusk_twilight)
        civilDawnTwilight = view.findViewById(R.id.civil_dawn_twilight)
        civilDuskTwilight = view.findViewById(R.id.civil_dusk_twilight)
        moonAzimuth = view.findViewById(R.id.moon_azimuth)
        moonDistance = view.findViewById(R.id.moon_distance)
        moonAltitude = view.findViewById(R.id.moon_altitude)
        moonParallacticAngle = view.findViewById(R.id.moon_parallactic_angle)
        moonriseTime = view.findViewById(R.id.moonrise_time)
        moonsetTime = view.findViewById(R.id.moon_set_time)
        moonFraction = view.findViewById(R.id.moon_fraction)
        moonAngle = view.findViewById(R.id.moon_angle)
        moonAngleState = view.findViewById(R.id.moon_angle_state)
        moonPhase = view.findViewById(R.id.moon_phase)
        moonPhaseAngle = view.findViewById(R.id.moon_phase_angle)
        nextNewMoon = view.findViewById(R.id.next_new_moon)
        nextFullMoon = view.findViewById(R.id.next_full_moon)
        nextFirstQuarter = view.findViewById(R.id.next_first_quarter)
        nextLastQuarter = view.findViewById(R.id.next_last_quarter)

        bottomSheetSlide = requireActivity() as BottomSheetSlide
        scrollView = view.findViewById(R.id.clock_panel_scrollview)
        scrollView.alpha = 0f
        toolbar = view.findViewById(R.id.clock_appbar)
        expandUp = view.findViewById(R.id.expand_up_clock_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.clock_info_bottom_sheet))
        backPress = requireActivity().onBackPressedDispatcher

        setMotionDelay(ClockPreferences.getMovementType())
        isMetric = MainPreferences.getUnit()
        if (MainPreferences.isCustomCoordinate()) {
            isCustomCoordinate = true
            customLatitude = MainPreferences.getCoordinates()[0].toDouble()
            customLongitude = MainPreferences.getCoordinates()[1].toDouble()
        }

        timezone = if (isCustomCoordinate) {
            if (MainPreferences.getTimeZone() != "") {
                MainPreferences.getTimeZone()
            } else {
                Calendar.getInstance().timeZone.id
            }
        } else {
            Calendar.getInstance().timeZone.id
        }

        setSkins()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isCustomCoordinate) {
            specifiedLocationNotice.visibility = View.VISIBLE
            divider.visibility = View.VISIBLE
        }

        clockMainLayout.setProxyView(view)

        loadImageResourcesWithoutAnimation(R.drawable.clock_face, face, requireContext())
        loadImageResourcesWithoutAnimation(R.drawable.clock_trail, sweepSeconds, requireContext())

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
                        backPress?.onBackPressed()
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                scrollView.alpha = slideOffset
                expandUp.alpha = (1 - slideOffset)
                view.findViewById<View>(R.id.clock_dim).alpha = slideOffset
                bottomSheetSlide.onBottomSheetSliding(slideOffset)
                toolbar.translationY = (toolbar.height * -slideOffset)
            }
        })

        menu.setOnClickListener {
            val clockMenu = WeakReference(ClockMenu(WeakReference(this)))
            clockMenu.get()?.show(parentFragmentManager, "null")
        }

        copyButton.setOnClickListener {
            handler.removeCallbacks(textAnimationRunnable)
            val clipboard: ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            val stringBuilder = StringBuilder()

            stringBuilder.append("${R.string.local_time}\n")
            stringBuilder.append("${localTimeZone.text}\n")
            stringBuilder.append("${digitalTime24.text}\n")
            stringBuilder.append("${digitalTime12.text}\n")
            stringBuilder.append("${localDay.text}\n")
            stringBuilder.append("${localDate.text}\n")
            stringBuilder.append("${localDayOfTheYear.text}\n")
            stringBuilder.append("${localWeekOfTheYear.text}\n\n")

            stringBuilder.append("${R.string.utc_time}\n")
            stringBuilder.append("${utcTimeZone.text}\n")
            stringBuilder.append("${utcTime.text}\n")
            stringBuilder.append("${utcDate.text}\n\n")

            if (isCustomCoordinate) {
                stringBuilder.append(specifiedLocationNotice.text)
                stringBuilder.append("\n")
            }

            if (sunAzimuth.text != "") {
                stringBuilder.append("${R.string.sun_position}\n")
                stringBuilder.append("${sunAzimuth.text}\n")
                stringBuilder.append("${sunDistance.text}\n")
                stringBuilder.append("${sunAltitude.text}\n")
                stringBuilder.append("${sunriseTime.text}\n")
                stringBuilder.append("${sunsetTime.text}\n")
                stringBuilder.append("${sunNadir.text}\n")
                stringBuilder.append("${sunNoon.text}\n\n")

                stringBuilder.append("${R.string.twilight}\n")
                stringBuilder.append("${astronomicalDawnTwilight.text}\n")
                stringBuilder.append("${nauticalDawnTwilight.text}\n")
                stringBuilder.append("${civilDawnTwilight.text}\n")
                stringBuilder.append("${civilDuskTwilight.text}\n\n")
                stringBuilder.append("${nauticalDuskTwilight.text}\n")
                stringBuilder.append("${astronomicalDuskTwilight.text}\n\n")

                stringBuilder.append("${R.string.moon_position}\n")
                stringBuilder.append("${moonAzimuth.text}\n")
                stringBuilder.append("${moonDistance.text}\n")
                stringBuilder.append("${moonAltitude.text}\n")
                stringBuilder.append("${moonParallacticAngle.text}\n")
                stringBuilder.append("${moonriseTime.text}\n")
                stringBuilder.append("${moonsetTime.text}\n\n")

                stringBuilder.append("${R.string.moon_illumination}\n")
                stringBuilder.append("${moonFraction.text}\n")
                stringBuilder.append("${moonAngle.text}\n")
                stringBuilder.append("${moonAngleState.text}\n")
                stringBuilder.append("${moonPhase.text}\n")
                stringBuilder.append("${moonPhaseAngle.text}\n\n")

                stringBuilder.append("${R.string.moon_dates}\n")
                stringBuilder.append("${nextNewMoon.text}\n")
                stringBuilder.append("${nextFullMoon.text}\n")
                stringBuilder.append("${nextFirstQuarter.text}\n")
                stringBuilder.append("${nextLastQuarter.text}\n\n")
            }

            if (BuildConfig.FLAVOR == "lite") {
                stringBuilder.append("\n\n")
                stringBuilder.append("Information is copied using Positional Lite\n")
                stringBuilder.append("Get the app from:\nhttps://play.google.com/store/apps/details?id=app.simple.positional.lite")
            }

            val clip: ClipData = ClipData.newPlainText("Time Data", stringBuilder)
            clipboard.setPrimaryClip(clip)

            if (clipboard.hasPrimaryClip()) {
                clockInfoText.setTextAnimation(getString(R.string.info_copied), 300)
                handler.postDelayed(textAnimationRunnable, 3000)
            }
        }
    }

    private val textAnimationRunnable: Runnable = Runnable { clockInfoText.setTextAnimation(getString(R.string.clock_info), 300) }

    private val clock = object : Runnable {
        override fun run() {
            hour.rotation = getHoursInDegrees(getCurrentTimeData())
            minutes.rotation = getMinutesInDegrees(getCurrentTimeData())

            seconds.rotation = if (delay < 1000) {
                getSecondsInDegreesWithDecimalPrecision(getCurrentTimeData())
            } else {
                getSecondsInDegrees(getCurrentTimeData())
            }
            sweepSeconds.rotation = seconds.rotation - 90

            if (dayNightIndicatorImageCountViolation != 0) {
                val calendar = getCurrentTimeData().hour
                if (calendar < 7 || calendar > 18) {
                    dayNightIndicator.setImageResource(R.drawable.ic_night)
                } else if (calendar < 18 || calendar > 6) {
                    dayNightIndicator.setImageResource(R.drawable.ic_day)
                }
                // Setting this to zero will prevent the image from applying again every second
                dayNightIndicatorImageCountViolation = 0
            }

            handler.postDelayed(this, delay)
        }
    }

    private val calender: Runnable = object : Runnable {
        override fun run() {
            updateTimeData()
            handler.postDelayed(this, 1000)
        }
    }

    private val customDataUpdater: Runnable = object : Runnable {
        override fun run() {
            calculateAndUpdateData(customLatitude, customLongitude)
            handler.postDelayed(this, 2500)
        }
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationBroadcastReceiver)
        handler.removeCallbacks(clock)
        handler.removeCallbacks(calender)
        handler.removeCallbacks(textAnimationRunnable)
        handler.removeCallbacks(customDataUpdater)
        clockInfoText.clearAnimation()
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
        setNeedle(ClockPreferences.getClockNeedleTheme())
    }

    fun setNeedle(value: Int) {
        loadImageResources(clockNeedleSkins[value][0], hour, requireContext(), 0)
        loadImageResources(clockNeedleSkins[value][1], minutes, requireContext(), 100)
        loadImageResources(clockNeedleSkins[value][2], seconds, requireContext(), 200)
    }

    fun setMotionDelay(value: Boolean) {
        handler.removeCallbacks(clock)
        delay = if (value) {
            (1000 / getDisplayRefreshRate(requireContext(), requireActivity())!!).toLong()
        } else {
            1000
        }
        handler.post(clock)
    }

    private fun calculateAndUpdateData(latitude: Double, longitude: Double) {
        CoroutineScope(Dispatchers.Default).launch {

            if (context == null) return@launch

            // Set and Rise
            val sunTimes = SunTimes.compute().timezone(timezone).on(Instant.now()).latitude(latitude).longitude(longitude).execute()

            val pattern: DateTimeFormatter = if (ClockPreferences.getDefaultClockTime()) {
                DateTimeFormatter.ofPattern("hh:mm:ss a").withLocale(LocaleHelper.getAppLocale())
            } else {
                DateTimeFormatter.ofPattern("HH:mm:ss")
            }

            val sunrise = fromHtml("<b>${getString(R.string.sun_sunrise)}</b> ${pattern.format(sunTimes.rise)}")
            val sunset = fromHtml("<b>${getString(R.string.sun_sunset)}</b> ${pattern.format(sunTimes.set)}")
            val sunNoon = fromHtml("<b>${getString(R.string.sun_noon)}</b> ${pattern.format(sunTimes.noon)}")
            val sunNadir = fromHtml("<b>${getString(R.string.sun_nadir)}</b> ${pattern.format(sunTimes.nadir)}")

            val moonTimes = MoonTimes.compute().on(Instant.now()).timezone(timezone).latitude(latitude).longitude(longitude).execute()
            val moonrise = fromHtml("<b>${getString(R.string.moon_moonrise)}</b> ${pattern.format(moonTimes.rise)}")
            val moonset = fromHtml("<b>${getString(R.string.moon_moonset)}</b> ${pattern.format(moonTimes.set)}")

            val twilightTimes = SunTimes.compute().timezone(timezone).on(Instant.now()).latitude(latitude).longitude(longitude)
            val astronomicalDawn = fromHtml("<b>${getString(R.string.twilight_astronomical_dawn)}</b> ${pattern.format(twilightTimes.twilight(SunTimes.Twilight.ASTRONOMICAL).execute().rise)}")
            val nauticalDawn = fromHtml("<b>${getString(R.string.twilight_nautical_dawn)}</b> ${pattern.format(twilightTimes.twilight(SunTimes.Twilight.NAUTICAL).execute().rise)}")
            val civilDawn = fromHtml("<b>${getString(R.string.twilight_civil_dawn)}</b> ${pattern.format(twilightTimes.twilight(SunTimes.Twilight.CIVIL).execute().rise)}")
            val civilDusk = fromHtml("<b>${getString(R.string.twilight_civil_dusk)}</b> ${pattern.format(twilightTimes.twilight(SunTimes.Twilight.CIVIL).execute().set)}")
            val nauticalDusk = fromHtml("<b>${getString(R.string.twilight_nautical_dusk)}</b> ${pattern.format(twilightTimes.twilight(SunTimes.Twilight.NAUTICAL).execute().set)}")
            val astronomicalDusk = fromHtml("<b>${getString(R.string.twilight_astronomical_dusk)}</b> ${pattern.format(twilightTimes.twilight(SunTimes.Twilight.ASTRONOMICAL).execute().set)}")

            // Position
            val sunPosition: SunPosition = SunPosition.compute().timezone(timezone).on(Instant.now()).at(latitude, longitude).execute()
            val sunAzimuth = fromHtml("<b>${getString(R.string.sun_azimuth)}</b> ${round(sunPosition.azimuth, 2)}° ${getDirectionCodeFromAzimuth(requireContext(), sunPosition.azimuth)}")
            val sunAltitude = fromHtml("<b>${getString(R.string.sun_altitude)}</b> ${round(sunPosition.trueAltitude, 2)}°")
            val sunDistance = if (isMetric) {
                fromHtml("<b>${getString(R.string.sun_distance)}</b> ${String.format("%.3E", sunPosition.distance)} ${getString(R.string.kilometer)}")
            } else {
                fromHtml("<b>${getString(R.string.sun_distance)}</b> ${String.format("%.3E", sunPosition.distance.toMiles())} ${getString(R.string.miles)}")
            }

            val moonPosition: MoonPosition = MoonPosition.compute().timezone(timezone).on(Instant.now()).latitude(latitude).longitude(longitude).execute()
            val moonAzimuth = fromHtml("<b>${getString(R.string.moon_azimuth)}</b> ${round(moonPosition.azimuth, 2)}° ${getDirectionCodeFromAzimuth(requireContext(), moonPosition.azimuth)}")
            val moonAltitude = fromHtml("<b>${getString(R.string.moon_altitude)}</b> ${round(moonPosition.altitude, 2)}°")
            val moonDistance = if (isMetric) {
                fromHtml("<b>${getString(R.string.moon_distance)}</b> ${String.format("%.3E", moonPosition.distance)} ${getString(R.string.kilometer)}")
            } else {
                fromHtml("<b>${getString(R.string.moon_distance)}</b> ${String.format("%.3E", moonPosition.distance.toMiles())} ${getString(R.string.miles)}")
            }
            val moonParallacticAngle = fromHtml("<b>${getString(R.string.moon_parallactic_angle)}</b> ${round(moonPosition.parallacticAngle, 2)}°")

            val moonIllumination = MoonIllumination.compute().timezone(timezone).on(Instant.now()).execute()
            val moonFraction = fromHtml("<b>${getString(R.string.moon_fraction)}</b> ${round(moonIllumination.fraction, 2)}")
            val moonAngle = fromHtml("<b>${getString(R.string.moon_angle)}</b> ${round(moonIllumination.angle, 2)}°")
            val moonAngleState = fromHtml("<b>${getString(R.string.moon_angle_state)}</b> ${if (moonIllumination.angle < 0) getString(R.string.waxing) else getString(R.string.waning)}")
            val moonPhase = fromHtml("<b>${getString(R.string.moon_phase)}</b> ${getMoonPhase(requireContext(), moonIllumination.phase)}")
            val moonPhaseAngle = fromHtml("<b>${getString(R.string.moon_phase_angle)}</b> ${round(moonIllumination.phase, 2)}°")

            if (moonImageCountViolation != 0) {
                /**
                 * [moonImageCountViolation] will prevent the moon image loading every time location updates
                 *
                 * Since the range of change is so small, its approximation/accuracy won't be affected in this tiny time frame
                 * The value of the calculation is being trimmed to 2 decimal places anyway
                 */
                loadImageResourcesWithoutAnimation(getMoonPhaseGraphics(round(moonIllumination.phase, 2)), moonPhaseGraphics, requireContext())
                moonImageCountViolation = 0
            }

            val nextFullMoon = fromHtml("<b>${getString(R.string.moon_full_moon)}</b> ${formatMoonDate(MoonPhase.compute().timezone(timezone).on(Instant.now()).phase(MoonPhase.Phase.FULL_MOON).execute().time)}")
            val nextNewMoon = fromHtml("<b>${getString(R.string.moon_new_moon)}</b> ${formatMoonDate(MoonPhase.compute().timezone(timezone).on(Instant.now()).phase(MoonPhase.Phase.NEW_MOON).execute().time)}")
            val nextFirstQuarter = fromHtml("<b>${getString(R.string.moon_first_quarter)}</b> ${formatMoonDate(MoonPhase.compute().timezone(timezone).on(Instant.now()).phase(MoonPhase.Phase.FIRST_QUARTER).execute().time)}")
            val nextLastQuarter = fromHtml("<b>${getString(R.string.moon_last_quarter)}</b> ${formatMoonDate(MoonPhase.compute().timezone(timezone).on(Instant.now()).phase(MoonPhase.Phase.LAST_QUARTER).execute().time)}")

            withContext(Dispatchers.Main) {
                try {
                    this@Clock.sunriseTime.text = sunrise
                    this@Clock.sunsetTime.text = sunset
                    this@Clock.sunNoon.text = sunNoon
                    this@Clock.sunNadir.text = sunNadir

                    this@Clock.moonriseTime.text = moonrise
                    this@Clock.moonsetTime.text = moonset

                    this@Clock.astronomicalDawnTwilight.text = astronomicalDawn
                    this@Clock.astronomicalDuskTwilight.text = astronomicalDusk
                    this@Clock.nauticalDawnTwilight.text = nauticalDawn
                    this@Clock.nauticalDuskTwilight.text = nauticalDusk
                    this@Clock.civilDawnTwilight.text = civilDawn
                    this@Clock.civilDuskTwilight.text = civilDusk

                    this@Clock.sunAzimuth.text = sunAzimuth
                    this@Clock.sunAltitude.text = sunAltitude
                    this@Clock.sunDistance.text = sunDistance

                    this@Clock.moonAzimuth.text = moonAzimuth
                    this@Clock.moonAltitude.text = moonAltitude
                    this@Clock.moonDistance.text = moonDistance
                    this@Clock.moonParallacticAngle.text = moonParallacticAngle
                    this@Clock.moonFraction.text = moonFraction
                    this@Clock.moonAngle.text = moonAngle
                    this@Clock.moonAngleState.text = moonAngleState
                    this@Clock.moonPhase.text = moonPhase
                    this@Clock.moonPhaseAngle.text = moonPhaseAngle
                    this@Clock.nextFullMoon.text = nextFullMoon
                    this@Clock.nextNewMoon.text = nextNewMoon
                    this@Clock.nextFirstQuarter.text = nextFirstQuarter
                    this@Clock.nextLastQuarter.text = nextLastQuarter
                } catch (e: NullPointerException) {
                } catch (e: UninitializedPropertyAccessException) {
                }
            }
        }
    }

    private fun updateTimeData() {
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
                val zoneId = ZoneId.of(timezone)
                val zonedDateTime: ZonedDateTime = Instant.now().atZone(zoneId)
                val patternLocale = LocaleHelper.getAppLocale()

                localTimeZone = fromHtml("<b>${getString(R.string.local_timezone)}</b> ${zonedDateTime.zone}")
                digitalTime24 = fromHtml("<b>${getString(R.string.local_time_24hr)}</b> ${zonedDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss").withLocale(patternLocale))}")
                digitalTime12 = fromHtml("<b>${getString(R.string.local_time_12hr)}</b> ${zonedDateTime.format(DateTimeFormatter.ofPattern("hh:mm:ss a").withLocale(patternLocale))}")
                digitalTime = getTime(zonedDateTime)
                localDate = fromHtml("<b>${getString(R.string.local_date)}</b> ${zonedDateTime.format(DateTimeFormatter.ofPattern("dd MMMM, yyyy").withLocale(patternLocale))}")
                localDay = fromHtml("<b>${getString(R.string.local_day)}</b> ${zonedDateTime.format(DateTimeFormatter.ofPattern("EEEE").withLocale(patternLocale))}")
                dayOfTheYear = fromHtml("<b>${getString(R.string.local_day_of_year)}</b> ${LocalDate.now().dayOfYear.toOrdinal()}")
                weekOfTheYear = fromHtml("<b>${getString(R.string.local_week_of_year)}</b> ${zonedDateTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR).toOrdinal()}")

                utcTimeZone = fromHtml("<b>${getString(R.string.utc_local_time_offset)}</b> ${zonedDateTime.format(DateTimeFormatter.ofPattern("XXX").withLocale(patternLocale))}")
                utcTime = fromHtml("<b>${getString(R.string.utc_current_time)}</b> ${getTimeWithSeconds(ZonedDateTime.ofInstant(Instant.now(), ZoneId.of(ZoneOffset.UTC.toString())))}")
                utcDate = fromHtml("<b>${getString(R.string.utc_current_date)}</b> ${ZonedDateTime.ofInstant(Instant.now(), ZoneId.of(ZoneOffset.UTC.toString())).format(DateTimeFormatter.ofPattern("dd MMMM, yyyy").withLocale(patternLocale))}")
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            withContext(Dispatchers.Main) {
                try {
                    this@Clock.localTimeZone.text = localTimeZone
                    this@Clock.digitalTime24.text = digitalTime24
                    this@Clock.digitalTime12.text = digitalTime12
                    this@Clock.digitalTimeMain.text = digitalTime
                    this@Clock.localDay.text = localDay
                    this@Clock.localDate.text = localDate
                    this@Clock.localDayOfTheYear.text = dayOfTheYear
                    this@Clock.localWeekOfTheYear.text = weekOfTheYear
                    this@Clock.utcTimeZone.text = utcTimeZone
                    this@Clock.utcDate.text = utcDate
                    this@Clock.utcTime.text = utcTime
                } catch (e: NullPointerException) {
                } catch (e: UninitializedPropertyAccessException) {
                }
            }
        }
    }

    private fun getCurrentTimeData(): ZonedDateTime {
        val zoneId = ZoneId.of(timezone)
        return Instant.now().atZone(zoneId)
    }

    private fun backPressed(value: Boolean) {
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
         * many presumptions have been taken here
         */
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
