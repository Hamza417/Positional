package app.simple.positional.ui.panels

import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.activities.fragment.ScopedFragment
import app.simple.positional.activities.subactivity.TimezonePickerActivity
import app.simple.positional.callbacks.BottomSheetSlide
import app.simple.positional.constants.ClockSkinsConstants.clockNeedleSkins
import app.simple.positional.constants.LocationPins
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.decorations.views.CustomCoordinatorLayout
import app.simple.positional.decorations.views.PhysicalRotationImageView
import app.simple.positional.dialogs.app.LocationParameters
import app.simple.positional.dialogs.clock.ClockMenu
import app.simple.positional.math.MathExtensions.round
import app.simple.positional.math.TimeConverter.getHoursInDegrees
import app.simple.positional.math.TimeConverter.getHoursInDegreesFor24
import app.simple.positional.math.TimeConverter.getMinutesInDegrees
import app.simple.positional.math.TimeConverter.getSecondsInDegrees
import app.simple.positional.math.UnitConverter.toMiles
import app.simple.positional.preferences.ClockPreferences
import app.simple.positional.preferences.GPSPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.*
import app.simple.positional.util.Direction.getDirectionCodeFromAzimuth
import app.simple.positional.util.HtmlHelper.fromHtml
import app.simple.positional.util.ImageLoader.loadImage
import app.simple.positional.util.MoonAngle.getMoonPhase
import app.simple.positional.util.MoonAngle.getMoonPhaseGraphics
import app.simple.positional.util.MoonTimeFormatter.formatMoonDate
import app.simple.positional.util.TextViewUtils.setTextAnimation
import app.simple.positional.util.TimeFormatter.getTime
import app.simple.positional.util.TimeFormatter.getTimeWithSeconds
import app.simple.positional.util.ViewUtils.gone
import app.simple.positional.viewmodels.viewmodel.LocationViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.*
import org.shredzone.commons.suncalc.*
import java.lang.Runnable
import java.text.ParseException
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.IsoFields
import java.util.*

class Clock : ScopedFragment() {

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<CoordinatorLayout>

    private lateinit var hour: PhysicalRotationImageView
    private lateinit var minutes: PhysicalRotationImageView
    private lateinit var seconds: PhysicalRotationImageView
    private lateinit var face: ImageView
    private lateinit var expandUp: ImageView
    private lateinit var sweepSeconds: PhysicalRotationImageView
    private lateinit var dayNightIndicator: ImageView
    private lateinit var moonPhaseGraphics: ImageView

    private lateinit var menu: DynamicRippleImageButton
    private lateinit var copyButton: DynamicRippleImageButton
    private lateinit var timezoneButton: DynamicRippleImageButton
    private lateinit var customLocationButton: DynamicRippleImageButton
    private lateinit var divider: View
    private lateinit var customLocationButtonDivider: View
    private lateinit var clockMainLayout: CustomCoordinatorLayout
    private lateinit var scrollView: NestedScrollView

    private lateinit var digitalTimeMain: TextView
    private lateinit var clockInfoText: TextView
    private lateinit var localTimeData: TextView
    private lateinit var utcTimeData: TextView
    private lateinit var specifiedLocationNotice: TextView
    private lateinit var sunPositionData: TextView
    private lateinit var sunTimeData: TextView
    private lateinit var twilightData: TextView
    private lateinit var moonPositionData: TextView
    private lateinit var moonTimeData: TextView
    private lateinit var moonIlluminationData: TextView
    private lateinit var moonDatesData: TextView

    private lateinit var handler: Handler
    private var backPress: OnBackPressedDispatcher? = null
    private lateinit var locationViewModel: LocationViewModel

    var delay: Long = 1000
    private var dayNightIndicatorImageCountViolation = 1
    private var isMetric = true
    private var is24HourFace = false
    private var isCustomCoordinate = false
    private var customLatitude = 0.0
    private var customLongitude = 0.0
    private var timezone: String = "Asia/Tokyo"
    private var movementType = "smooth"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_clock, container, false)

        timezone = ClockPreferences.getTimeZone()
        movementType = ClockPreferences.getMovementType()

        locationViewModel = ViewModelProvider(requireActivity()).get(LocationViewModel::class.java)
        handler = Handler(Looper.getMainLooper())

        hour = view.findViewById(R.id.hour)
        minutes = view.findViewById(R.id.minutes)
        seconds = view.findViewById(R.id.seconds)
        face = view.findViewById(R.id.clock_face)
        sweepSeconds = view.findViewById(R.id.sweep_seconds)
        dayNightIndicator = view.findViewById(R.id.day_night_indicator)
        moonPhaseGraphics = view.findViewById(R.id.moon_phase_graphics)

        menu = view.findViewById(R.id.clock_menu)
        copyButton = view.findViewById(R.id.clock_copy)
        timezoneButton = view.findViewById(R.id.clock_timezone)
        customLocationButton = view.findViewById(R.id.clock_custom_location)
        divider = view.findViewById(R.id.clock_divider)
        customLocationButtonDivider = view.findViewById(R.id.custom_location_divider)
        clockMainLayout = view.findViewById(R.id.clock_main_layout)

        digitalTimeMain = view.findViewById(R.id.digital_time_main)
        clockInfoText = view.findViewById(R.id.clock_info_text)

        localTimeData = view.findViewById(R.id.local_timezone_data)
        utcTimeData = view.findViewById(R.id.utc_time_data)

        specifiedLocationNotice = view.findViewById(R.id.specified_location_notice_clock)
        sunPositionData = view.findViewById(R.id.sun_position_data)
        sunTimeData = view.findViewById(R.id.sun_time_data)
        twilightData = view.findViewById(R.id.twilight_data)
        moonPositionData = view.findViewById(R.id.moon_position_data)
        moonTimeData = view.findViewById(R.id.moon_time_data)
        moonIlluminationData = view.findViewById(R.id.moon_illumination_data)
        moonDatesData = view.findViewById(R.id.moon_dates_data)

        scrollView = view.findViewById(R.id.clock_panel_scrollview)
        scrollView.alpha = 0f
        expandUp = view.findViewById(R.id.expand_up_clock_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.clock_info_bottom_sheet))
        backPress = requireActivity().onBackPressedDispatcher

        setMotionDelay(ClockPreferences.getMovementType())
        isMetric = MainPreferences.getUnit()
        is24HourFace = if (ClockPreferences.isClockFace24Hour()) {
            face.setImageResource(R.drawable.clock_face_24)
            true
        } else {
            face.setImageResource(R.drawable.clock_face)
            false
        }
        if (MainPreferences.isCustomCoordinate()) {
            isCustomCoordinate = true
            customLatitude = MainPreferences.getCoordinates()[0].toDouble()
            customLongitude = MainPreferences.getCoordinates()[1].toDouble()
            customLocationButton.setImageResource(R.drawable.ic_place_custom)
        } else {
            customLocationButton.setImageResource(LocationPins.locationsPins[GPSPreferences.getPinSkin()])
        }

        setSkins()

        hour.setPhysical(0.5F, 8F, 5000F)
        minutes.setPhysical(0.5F, 8F, 5000F)

        calculateAndUpdateData(MainPreferences.getLastCoordinates()[0].toDouble(),
                MainPreferences.getLastCoordinates()[1].toDouble())

        if (BuildConfig.FLAVOR == "lite") {
            timezoneButton.gone()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isCustomCoordinate) {
            specifiedLocationNotice.visibility = View.VISIBLE
            divider.visibility = View.VISIBLE
        }

        clockMainLayout.setProxyView(view)

        locationViewModel.location.observe(viewLifecycleOwner, {
            if (isCustomCoordinate) return@observe
            calculateAndUpdateData(it.latitude, it.longitude)
        })

        bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    backPressed(true)
                    copyButton.isClickable = true
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    copyButton.isClickable = false
                    backPressed(false)
                    if (backPress!!.hasEnabledCallbacks()) {
                        backPress?.onBackPressed()
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                scrollView.alpha = slideOffset
                expandUp.alpha = 1 - slideOffset
                view.findViewById<View>(R.id.clock_dim).alpha = slideOffset
            }
        })

        menu.setOnClickListener {
            ClockMenu.newInstance().show(parentFragmentManager, "clock_menu")
        }

        copyButton.setOnClickListener {
            handler.removeCallbacks(textAnimationRunnable)
            val clipboard: ClipboardManager =
                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            val stringBuilder = StringBuilder().apply {
                append("${R.string.local_time}\n")
                append("${localTimeData.text}\n")
                append("${R.string.utc_time}\n")
                append("${utcTimeData.text}\n")

                if (isCustomCoordinate) {
                    append(specifiedLocationNotice.text)
                    append("\n")
                }

                if (sunPositionData.text.isNotEmpty()) {
                    append("${R.string.sun_position}\n")
                    append("${sunPositionData.text}\n")
                    append("${getString(R.string.sun_time)}\n")
                    append("${sunTimeData.text}\n\n")
                    append("${R.string.twilight}\n")
                    append("${twilightData.text}\n\n")
                    append("${R.string.moon_position}\n")
                    append("${moonPositionData.text}\n\n")
                    append("${moonTimeData.text}\n\n")
                    append("${R.string.moon_illumination}\n")
                    append("${moonIlluminationData.text}\n\n")
                    append("${R.string.moon_dates}\n")
                    append("${moonDatesData.text}\n\n")
                }

                if (BuildConfig.FLAVOR == "lite") {
                    append("\n\n")
                    append("Information is copied using Positional\n")
                    append("Get the app from:\nhttps://play.google.com/store/apps/details?id=app.simple.positional")
                }
            }

            val clip: ClipData = ClipData.newPlainText("Time Data", stringBuilder)
            clipboard.setPrimaryClip(clip)

            if (clipboard.hasPrimaryClip()) {
                clockInfoText.setTextAnimation(getString(R.string.info_copied), 300)
                handler.postDelayed(textAnimationRunnable, 3000)
            }
        }

        timezoneButton.setOnClickListener {
            requireContext().startActivity(Intent(requireActivity(), TimezonePickerActivity::class.java))
        }

        customLocationButton.setOnClickListener {
            LocationParameters.newInstance()
                    .show(parentFragmentManager, "location_parameters")
        }
    }

    private val textAnimationRunnable = Runnable {
        clockInfoText.setTextAnimation(getString(R.string.clock_info), 300)
    }

    private val clock = object : Runnable {
        override fun run() {
            minutes.rotationUpdate(getMinutesInDegrees(getCurrentTimeData()), true)

            if (is24HourFace) {
                hour.rotationUpdate(getHoursInDegreesFor24(getCurrentTimeData()), true)
            } else {
                hour.rotationUpdate(getHoursInDegrees(getCurrentTimeData()), true)
            }

            when (movementType) {
                "oscillate" -> {
                    seconds.setPhysical(1F, -1F, 25000F)
                    seconds.rotationUpdate(getSecondsInDegrees(getCurrentTimeData(), false), true)
                    sweepSeconds.setPhysical(1F, -1F, 25000F)
                    sweepSeconds.rotationUpdate(getSecondsInDegrees(getCurrentTimeData(), false) - 90F, true)
                }
                "tick_smooth" -> {
                    seconds.setPhysical(-1F, 5F, 5000F)
                    seconds.rotationUpdate(getSecondsInDegrees(getCurrentTimeData(), false), true)
                    sweepSeconds.setPhysical(-1F, 5F, 5000F)
                    sweepSeconds.rotationUpdate(getSecondsInDegrees(getCurrentTimeData(), false) - 90F, true)
                }
                "smooth",
                "tick" -> {
                    seconds.rotation = if (delay < 1000) {
                        getSecondsInDegrees(getCurrentTimeData(), true)
                    } else {
                        getSecondsInDegrees(getCurrentTimeData(), false)
                    }

                    sweepSeconds.rotation = seconds.rotation - 90
                }
            }

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

    private val calender = object : Runnable {
        override fun run() {
            updateTimeData()
            handler.postDelayed(this, 1000)
        }
    }
    private val customDataUpdater = object : Runnable {
        override fun run() {
            calculateAndUpdateData(customLatitude, customLongitude)
            handler.postDelayed(this, 2500)
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(clock)
        handler.post(calender)
        if (isCustomCoordinate) {
            handler.post(customDataUpdater)
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(clock)
        handler.removeCallbacks(calender)
        handler.removeCallbacks(textAnimationRunnable)
        handler.removeCallbacks(customDataUpdater)
        clockInfoText.clearAnimation()
        if (backPress!!.hasEnabledCallbacks()) {
            backPressed(false)
        }
    }

    private fun setSkins() {
        setNeedle(ClockPreferences.getClockNeedleTheme())
    }

    private fun setNeedle(value: Int) {
        loadImage(clockNeedleSkins[value][0], hour, requireContext(), 0)
        loadImage(clockNeedleSkins[value][1], minutes, requireContext(), 100)
        loadImage(clockNeedleSkins[value][2], seconds, requireContext(), 200)
    }

    private fun setMotionDelay(value: String) {
        handler.removeCallbacks(clock)

        delay = if (value == "smooth") {
            (1000 / getDisplayRefreshRate(requireContext(), requireActivity())!!).toLong()
        } else {
            1000
        }

        handler.post(clock)
    }

    private fun calculateAndUpdateData(latitude: Double, longitude: Double) {
        viewLifecycleOwner.lifecycleScope.launch {

            var sunTimeData: Spanned
            var moonTimeData: Spanned
            var twilightData: Spanned
            var sunPositionData: Spanned
            var moonPositionData: Spanned
            var moonIlluminationData: Spanned
            var moonDatesData: Spanned

            var moonPhase: Double?

            withContext(Dispatchers.Default) {
                val pattern: DateTimeFormatter = if (ClockPreferences.getDefaultClockTimeFormat()) {
                    if (ClockPreferences.isUsingSecondsPrecision()) {
                        DateTimeFormatter.ofPattern("hh:mm:ss a").withLocale(LocaleHelper.getAppLocale())
                    } else {
                        DateTimeFormatter.ofPattern("hh:mm a").withLocale(LocaleHelper.getAppLocale())
                    }
                } else {
                    if (ClockPreferences.isUsingSecondsPrecision()) {
                        DateTimeFormatter.ofPattern("HH:mm:ss")
                    } else {
                        DateTimeFormatter.ofPattern("HH:mm")
                    }
                }

                sunTimeData =
                        with(SunTimes.compute().timezone(timezone).on(Instant.now()).latitude(latitude).longitude(longitude).execute()) {
                            fromHtml("<b>${getString(R.string.sun_sunrise)}</b> ${pattern.format(rise)}<br>" +
                                    "<b>${getString(R.string.sun_sunset)}</b> ${pattern.format(set)}<br>" +
                                    "<b>${getString(R.string.sun_noon)}</b> ${pattern.format(noon)}<br>" +
                                    "<b>${getString(R.string.sun_nadir)}</b> ${pattern.format(nadir)}")
                        }

                moonTimeData =
                        with(MoonTimes.compute().on(Instant.now()).timezone(timezone).latitude(latitude).longitude(longitude).execute()) {
                            fromHtml("<b>${getString(R.string.moon_moonrise)}</b> ${pattern.format(rise)}<br>" +
                                    "<b>${getString(R.string.moon_moonset)}</b> ${pattern.format(set)}")
                        }

                twilightData =
                        with(SunTimes.compute().timezone(timezone).on(Instant.now()).latitude(latitude).longitude(longitude)) {
                            fromHtml("<b>${getString(R.string.twilight_astronomical_dawn)}</b> ${pattern.format(twilight(SunTimes.Twilight.ASTRONOMICAL).execute().rise)}<br>" +
                                    "<b>${getString(R.string.twilight_nautical_dawn)}</b> ${pattern.format(twilight(SunTimes.Twilight.NAUTICAL).execute().rise)}<br>" +
                                    "<b>${getString(R.string.twilight_civil_dawn)}</b> ${pattern.format(twilight(SunTimes.Twilight.CIVIL).execute().rise)}<br>" +
                                    "<b>${getString(R.string.twilight_civil_dusk)}</b> ${pattern.format(twilight(SunTimes.Twilight.CIVIL).execute().set)}<br>" +
                                    "<b>${getString(R.string.twilight_nautical_dusk)}</b> ${pattern.format(twilight(SunTimes.Twilight.NAUTICAL).execute().set)}<br>" +
                                    "<b>${getString(R.string.twilight_astronomical_dusk)}</b> ${pattern.format(twilight(SunTimes.Twilight.ASTRONOMICAL).execute().set)}")
                        }

                sunPositionData =
                        with(SunPosition.compute().timezone(timezone).on(Instant.now()).at(latitude, longitude).execute()) {
                            fromHtml("<b>${getString(R.string.sun_azimuth)}</b> ${round(azimuth, 2)}° ${getDirectionCodeFromAzimuth(requireContext(), azimuth)}<br>" +
                                    "<b>${getString(R.string.sun_altitude)}</b> ${round(trueAltitude, 2)}°<br>" +
                                    if (isMetric) {
                                        "<b>${getString(R.string.sun_distance)}</b> ${String.format("%.3E", distance)} ${getString(R.string.kilometer)}"
                                    } else {
                                        "<b>${getString(R.string.sun_distance)}</b> ${String.format("%.3E", distance.toMiles())} ${getString(R.string.miles)}"
                                    })
                        }

                moonPositionData =
                        with(MoonPosition.compute().timezone(timezone).on(Instant.now()).latitude(latitude).longitude(longitude).execute()) {
                            fromHtml("<b>${getString(R.string.moon_azimuth)}</b> ${round(azimuth, 2)}° ${getDirectionCodeFromAzimuth(requireContext(), azimuth)}<br>" +
                                    "<b>${getString(R.string.moon_altitude)}</b> ${round(altitude, 2)}°<br>" +
                                    (if (isMetric) {
                                        "<b>${getString(R.string.moon_distance)}</b> ${String.format("%.3E", distance)} ${getString(R.string.kilometer)}<br>"
                                    } else {
                                        "<b>${getString(R.string.moon_distance)}</b> ${String.format("%.3E", distance.toMiles())} ${getString(R.string.miles)}<br>"
                                    }) +
                                    "<b>${getString(R.string.moon_parallactic_angle)}</b> ${round(parallacticAngle, 2)}°")
                        }

                moonIlluminationData =
                        with(MoonIllumination.compute().timezone(timezone).on(Instant.now()).execute()) {
                            moonPhase = phase

                            fromHtml("<b>${getString(R.string.moon_fraction)}</b> ${round(fraction, 2)}<br>" +
                                    "<b>${getString(R.string.moon_angle)}</b> ${round(angle, 2)}°<br>" +
                                    "<b>${getString(R.string.moon_angle_state)}</b> ${if (angle < 0) getString(R.string.waxing) else getString(R.string.waning)}<br>" +
                                    "<b>${getString(R.string.moon_phase)}</b> ${getMoonPhase(requireContext(), phase)}<br>" +
                                    "<b>${getString(R.string.moon_phase_angle)}</b> ${round(phase, 2)}°")
                        }

                moonDatesData =
                        with(MoonPhase.compute().timezone(timezone).on(Instant.now())) {
                            fromHtml("<b>${getString(R.string.moon_full_moon)}</b> ${formatMoonDate(phase(MoonPhase.Phase.FULL_MOON).execute().time)}<br>" +
                                    "<b>${getString(R.string.moon_new_moon)}</b> ${formatMoonDate(phase(MoonPhase.Phase.NEW_MOON).execute().time)}<br>" +
                                    "<b>${getString(R.string.moon_first_quarter)}</b> ${formatMoonDate(phase(MoonPhase.Phase.FIRST_QUARTER).execute().time)}<br>" +
                                    "<b>${getString(R.string.moon_last_quarter)}</b> ${formatMoonDate(phase(MoonPhase.Phase.LAST_QUARTER).execute().time)}<br>")
                        }
            }

            try {
                this@Clock.sunTimeData.text = sunTimeData
                this@Clock.moonTimeData.text = moonTimeData
                this@Clock.twilightData.text = twilightData
                this@Clock.sunPositionData.text = sunPositionData
                this@Clock.moonPositionData.text = moonPositionData
                this@Clock.moonIlluminationData.text = moonIlluminationData
                this@Clock.moonPhaseGraphics.setImageResource(getMoonPhaseGraphics(round(moonPhase!!, 2)))
                //this@Clock.moonPhaseGraphics.rotation = (moonPosition.parallacticAngle - moonIllumination.angle).toFloat()
                this@Clock.moonDatesData.text = moonDatesData
            } catch (ignored: NullPointerException) {
            } catch (ignored: UninitializedPropertyAccessException) {
            }
        }
    }

    private fun updateTimeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            var localTimeData: Spanned? = null
            var digitalTime: Spanned? = null
            var utcTimeData: Spanned? = null

            withContext(Dispatchers.Default) {
                try {
                    val zoneId = ZoneId.of(timezone)
                    val zonedDateTime: ZonedDateTime = Instant.now().atZone(zoneId)
                    val patternLocale = LocaleHelper.getAppLocale()

                    localTimeData =
                            with(zonedDateTime) {
                                fromHtml("<b>${getString(R.string.local_timezone)}</b> ${zone}<br>" +
                                        "<b>${getString(R.string.local_time_24hr)}</b> ${format(DateTimeFormatter.ofPattern("HH:mm:ss").withLocale(patternLocale))}<br>" +
                                        "<b>${getString(R.string.local_time_12hr)}</b> ${format(DateTimeFormatter.ofPattern("hh:mm:ss a").withLocale(patternLocale))}<br>" +
                                        "<b>${getString(R.string.local_date)}</b> ${format(DateTimeFormatter.ofPattern("dd MMMM, yyyy").withLocale(patternLocale))}<br>" +
                                        "<b>${getString(R.string.local_day)}</b> ${format(DateTimeFormatter.ofPattern("EEEE").withLocale(patternLocale))}<br>" +
                                        "<b>${getString(R.string.local_day_of_year)}</b> ${LocalDate.now().dayOfYear.toOrdinal()}<br>" +
                                        "<b>${getString(R.string.local_week_of_year)}</b> ${get(IsoFields.WEEK_OF_WEEK_BASED_YEAR).toOrdinal()}")
                            }

                    digitalTime = getTime(zonedDateTime)

                    utcTimeData =
                            with(ZonedDateTime.ofInstant(Instant.now(), ZoneId.of(ZoneOffset.UTC.toString()))) {
                                fromHtml("<b>${getString(R.string.utc_local_time_offset)}</b> ${
                                    zonedDateTime.format(DateTimeFormatter.ofPattern("XXX")
                                            .withLocale(patternLocale)).replace("Z", "+00:00")
                                }<br>" +
                                        "<b>${getString(R.string.utc_current_time)}</b> ${getTimeWithSeconds(this)}<br>" +
                                        "<b>${getString(R.string.local_date)}</b> ${format(DateTimeFormatter.ofPattern("dd MMMM, yyyy").withLocale(patternLocale))}")
                            }
                } catch (ignored: ParseException) {
                }
            }

            try {
                this@Clock.digitalTimeMain.text = digitalTime
                this@Clock.localTimeData.text = localTimeData
                this@Clock.utcTimeData.text = utcTimeData
            } catch (ignored: NullPointerException) {
            } catch (ignored: UninitializedPropertyAccessException) {
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            ClockPreferences.clockNeedleMovementType -> {
                movementType = ClockPreferences.getMovementType()
                setMotionDelay(ClockPreferences.getMovementType())
            }
            ClockPreferences.clockNeedle -> {
                setNeedle(ClockPreferences.getClockNeedleTheme())
            }
            ClockPreferences.timezone -> {
                timezone = ClockPreferences.getTimeZone()
            }
            ClockPreferences.is24HourFace -> {
                is24HourFace = if (ClockPreferences.isClockFace24Hour()) {
                    face.setImageResource(R.drawable.clock_face_24)
                    hour.setPhysical(0.5F, 8F, 5000F)
                    hour.rotationUpdate(getHoursInDegreesFor24(getCurrentTimeData()), true)
                    true
                } else {
                    face.setImageResource(R.drawable.clock_face)
                    hour.setPhysical(0.5F, 8F, 5000F)
                    hour.rotationUpdate(getHoursInDegrees(getCurrentTimeData()), true)
                    false
                }
            }
        }
    }

    companion object {
        fun newInstance(): Clock {
            val args = Bundle()
            val fragment = Clock()
            fragment.arguments = args
            return fragment
        }
    }
}
