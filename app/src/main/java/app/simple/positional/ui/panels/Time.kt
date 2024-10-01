package app.simple.positional.ui.panels

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.simple.positional.R
import app.simple.positional.activities.subactivity.TimezonePickerActivity
import app.simple.positional.callbacks.BottomSheetSlide
import app.simple.positional.constants.ClockSkinsConstants.clockNeedleSkins
import app.simple.positional.constants.LocationPins
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.decorations.views.PhysicalRotationImageView
import app.simple.positional.dialogs.app.LocationParameters
import app.simple.positional.dialogs.clock.ClockMenu
import app.simple.positional.extensions.fragment.ScopedFragment
import app.simple.positional.math.MathExtensions.round
import app.simple.positional.math.TimeConverter.getHoursInDegrees
import app.simple.positional.math.TimeConverter.getHoursInDegreesFor24
import app.simple.positional.math.TimeConverter.getMinutesInDegrees
import app.simple.positional.math.TimeConverter.getSecondsInDegrees
import app.simple.positional.math.UnitConverter.toMiles
import app.simple.positional.preferences.ClockPreferences
import app.simple.positional.preferences.GPSPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.AppUtils
import app.simple.positional.util.Direction.getDirectionCodeFromAzimuth
import app.simple.positional.util.DisplayRefreshRate.getDisplayRefreshRate
import app.simple.positional.util.HtmlHelper.fromHtml
import app.simple.positional.util.ImageLoader.loadImage
import app.simple.positional.util.LocaleHelper
import app.simple.positional.util.MoonAngle.getMoonPhase
import app.simple.positional.util.MoonAngle.getMoonPhaseGraphics
import app.simple.positional.util.MoonTimeFormatter.formatMoonDate
import app.simple.positional.util.Ordinal.toOrdinal
import app.simple.positional.util.TimeFormatter.getTimeWithSeconds
import app.simple.positional.util.ViewUtils.gone
import app.simple.positional.viewmodels.viewmodel.LocationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.shredzone.commons.suncalc.MoonIllumination
import org.shredzone.commons.suncalc.MoonPhase
import org.shredzone.commons.suncalc.MoonPosition
import org.shredzone.commons.suncalc.MoonTimes
import org.shredzone.commons.suncalc.SunPosition
import org.shredzone.commons.suncalc.SunTimes
import java.text.ParseException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.IsoFields

class Time : ScopedFragment() {

    private lateinit var hour: PhysicalRotationImageView
    private lateinit var minutes: PhysicalRotationImageView
    private lateinit var seconds: PhysicalRotationImageView
    private lateinit var face: ImageView
    private lateinit var sweepSeconds: PhysicalRotationImageView
    private lateinit var dayNightIndicator: ImageView
    private lateinit var moonPhaseGraphics: ImageView

    private lateinit var menu: DynamicRippleImageButton
    private lateinit var timezoneButton: DynamicRippleImageButton
    private lateinit var customLocationButton: DynamicRippleImageButton
    private lateinit var divider: View
    private lateinit var customLocationButtonDivider: View

    private lateinit var localTimeData: TextView
    private lateinit var utcTimeData: TextView
    private lateinit var specifiedLocationNotice: TextView
    private lateinit var sunPositionData: TextView
    private lateinit var sunPosition: app.simple.positional.decorations.views.SunPosition
    private lateinit var sunTimeData: TextView
    private lateinit var twilightData: TextView
    private lateinit var moonPositionData: TextView
    private lateinit var moonTimeData: TextView
    private lateinit var moonIlluminationData: TextView
    private lateinit var moonDatesData: TextView

    private lateinit var handler: Handler
    private lateinit var bottomSheetSlide: BottomSheetSlide
    private lateinit var locationViewModel: LocationViewModel

    var delay: Long = 1000
    private var isMetric = true
    private var is24HourFace = false
    private var isCustomCoordinate = false
    private var customLatitude = 0.0
    private var customLongitude = 0.0
    private var timezone: String = "Asia/Tokyo"
    private var movementType = "tick"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_clock, container, false)

        timezone = ClockPreferences.getTimeZone()
        movementType = ClockPreferences.getMovementType()

        locationViewModel = ViewModelProvider(requireActivity())[LocationViewModel::class.java]
        handler = Handler(Looper.getMainLooper())

        hour = view.findViewById(R.id.hour)
        minutes = view.findViewById(R.id.minutes)
        seconds = view.findViewById(R.id.seconds)
        face = view.findViewById(R.id.clock_face)
        sweepSeconds = view.findViewById(R.id.sweep_seconds)
        dayNightIndicator = view.findViewById(R.id.day_night_indicator)
        moonPhaseGraphics = view.findViewById(R.id.moon_phase_graphics)

        menu = view.findViewById(R.id.clock_menu)
        timezoneButton = view.findViewById(R.id.clock_timezone)
        customLocationButton = view.findViewById(R.id.clock_custom_location)
        divider = view.findViewById(R.id.clock_divider)
        customLocationButtonDivider = view.findViewById(R.id.custom_location_divider)

        localTimeData = view.findViewById(R.id.local_timezone_data)
        utcTimeData = view.findViewById(R.id.utc_time_data)

        specifiedLocationNotice = view.findViewById(R.id.specified_location_notice_clock)
        sunPositionData = view.findViewById(R.id.sun_position_data)
        sunPosition = view.findViewById(R.id.sun_position_diagram)
        sunTimeData = view.findViewById(R.id.sun_time_data)
        twilightData = view.findViewById(R.id.twilight_data)
        moonPositionData = view.findViewById(R.id.moon_position_data)
        moonTimeData = view.findViewById(R.id.moon_time_data)
        moonIlluminationData = view.findViewById(R.id.moon_illumination_data)
        moonDatesData = view.findViewById(R.id.moon_dates_data)

        bottomSheetSlide = requireActivity() as BottomSheetSlide

        setMotionDelay(ClockPreferences.getMovementType())

        isMetric = MainPreferences.isMetric()
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
            MainPreferences.getLastCoordinates()[1].toDouble(),
            MainPreferences.getLastAltitude().toDouble())

        if (AppUtils.isLiteFlavor()) {
            timezoneButton.gone()
            sunPosition.gone()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isCustomCoordinate) {
            specifiedLocationNotice.visibility = View.VISIBLE
            divider.visibility = View.VISIBLE
        }

        locationViewModel.getLocation().observe(viewLifecycleOwner) {
            if (isCustomCoordinate) return@observe
            calculateAndUpdateData(it.latitude, it.longitude, it.altitude)
        }

        menu.setOnClickListener {
            ClockMenu.newInstance()
                    .show(parentFragmentManager, ClockMenu.TAG)
        }

        timezoneButton.setOnClickListener {
            requireContext().startActivity(Intent(requireActivity(), TimezonePickerActivity::class.java))
        }

        customLocationButton.setOnClickListener {
            LocationParameters.newInstance()
                    .show(parentFragmentManager, LocationParameters.TAG)
        }
    }

    private val clock = object : Runnable {
        override fun run() {
            minutes.rotationUpdate(getMinutesInDegrees(getCurrentTimeData()), true)

            val hourDegrees = if (is24HourFace) {
                getHoursInDegreesFor24(getCurrentTimeData())
            } else {
                getHoursInDegrees(getCurrentTimeData())
            }
            hour.rotationUpdate(hourDegrees, true)

            val secondsDegrees = getSecondsInDegrees(getCurrentTimeData(), movementType != "tick")
            val sweepSecondsDegrees = secondsDegrees - 90F

            when (movementType) {
                "oscillate" -> {
                    seconds.setPhysical(1F, -1F, 25000F)
                    seconds.rotationUpdate(secondsDegrees, true)
                    sweepSeconds.setPhysical(1F, -1F, 25000F)
                    sweepSeconds.rotationUpdate(sweepSecondsDegrees, true)
                }
                "tick_smooth" -> {
                    seconds.setPhysical(-1F, 5F, 5000F)
                    seconds.rotationUpdate(secondsDegrees, true)
                    sweepSeconds.setPhysical(-1F, 5F, 5000F)
                    sweepSeconds.rotationUpdate(sweepSecondsDegrees, true)
                }
                "smooth" -> {
                    seconds.rotationUpdate(secondsDegrees, true)
                    sweepSeconds.setPhysical(0.1F, 10F, 5000F)
                    sweepSeconds.rotationUpdate(sweepSecondsDegrees, true)
                }
                "tick" -> {
                    seconds.rotationUpdate(secondsDegrees, false)
                    sweepSeconds.rotationUpdate(sweepSecondsDegrees, false)
                }
                "mechanical" -> {
                    seconds.rotationUpdate(secondsDegrees, false)
                    sweepSeconds.rotationUpdate(sweepSecondsDegrees, false)
                }
            }

            setDayNightIcon()
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
            calculateAndUpdateData(customLatitude, customLongitude, 0.0)
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
        handler.removeCallbacks(customDataUpdater)
    }

    private fun setSkins() {
        setNeedle(ClockPreferences.getClockNeedleTheme())
    }

    private fun setDayNightIcon() {
        if (getCurrentTimeData().hour in 6..18) {
            dayNightIndicator.setImageResource(R.drawable.ic_day)
        } else {
            dayNightIndicator.setImageResource(R.drawable.ic_night)
        }
    }

    private fun setNeedle(value: Int) {
        loadImage(clockNeedleSkins[value][0], hour, requireContext(), 0)
        loadImage(clockNeedleSkins[value][1], minutes, requireContext(), 100)
        loadImage(clockNeedleSkins[value][2], seconds, requireContext(), 200)
    }

    private fun setMotionDelay(value: String) {
        delay = when (value) {
            "smooth" -> {
                1000 / requireActivity().getDisplayRefreshRate().toLong()
            }

            "mechanical" -> {
                240
            }

            else -> {
                1000
            }
        }
    }

    private fun calculateAndUpdateData(latitude: Double, longitude: Double, altitude: Double) {
        viewLifecycleOwner.lifecycleScope.launch {

            var sunTimeData: Spanned
            var moonTimeData: Spanned
            var twilightData: Spanned
            var sunPositionData: Spanned
            var moonPositionData: Spanned
            var moonIlluminationData: Spanned
            var moonDatesData: Spanned

            var moonPhase: Double?
            var sunAzimuth: Double?
            var moonAzimuth: Double?
//            var sunMoonCalculator: SunMoonCalculator?
//            var moonBitmap: Bitmap?
//            var size = moonPhaseGraphics.width

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
                    with(SunTimes.compute().timezone(timezone).on(Instant.now()).latitude(latitude).longitude(longitude).elevation(altitude).execute()) {
                        fromHtml("<b>${getString(R.string.sun_sunrise)}</b> ${pattern.format(rise)}<br>" +
                                 "<b>${getString(R.string.sun_sunset)}</b> ${pattern.format(set)}<br>" +
                                 "<b>${getString(R.string.sun_noon)}</b> ${pattern.format(noon)}<br>" +
                                 "<b>${getString(R.string.sun_nadir)}</b> ${pattern.format(nadir)}")
                    }

                moonTimeData =
                    with(MoonTimes.compute().on(Instant.now()).timezone(timezone).latitude(latitude).longitude(longitude).elevation(altitude).execute()) {
                        fromHtml("<b>${getString(R.string.moon_moonrise)}</b> ${pattern.format(rise)}<br>" +
                                 "<b>${getString(R.string.moon_moonset)}</b> ${pattern.format(set)}")
                    }

                twilightData =
                    with(SunTimes.compute().timezone(timezone).on(Instant.now()).latitude(latitude).longitude(longitude).elevation(altitude)) {
                        fromHtml("<b>${getString(R.string.twilight_astronomical_dawn)}</b> ${pattern.format(twilight(SunTimes.Twilight.ASTRONOMICAL).execute().rise)}<br>" +
                                 "<b>${getString(R.string.twilight_nautical_dawn)}</b> ${pattern.format(twilight(SunTimes.Twilight.NAUTICAL).execute().rise)}<br>" +
                                 "<b>${getString(R.string.twilight_civil_dawn)}</b> ${pattern.format(twilight(SunTimes.Twilight.CIVIL).execute().rise)}<br>" +
                                 "<b>${getString(R.string.twilight_civil_dusk)}</b> ${pattern.format(twilight(SunTimes.Twilight.CIVIL).execute().set)}<br>" +
                                 "<b>${getString(R.string.twilight_nautical_dusk)}</b> ${pattern.format(twilight(SunTimes.Twilight.NAUTICAL).execute().set)}<br>" +
                                 "<b>${getString(R.string.twilight_astronomical_dusk)}</b> ${pattern.format(twilight(SunTimes.Twilight.ASTRONOMICAL).execute().set)}")
                    }

                sunPositionData =
                    with(SunPosition.compute().timezone(timezone).on(Instant.now()).at(latitude, longitude).elevation(altitude).execute()) {
                        sunAzimuth = azimuth

                        fromHtml("<b>${getString(R.string.sun_azimuth)}</b> ${round(azimuth, 2)}° ${getDirectionCodeFromAzimuth(requireContext(), azimuth)}<br>" +
                                 "<b>${getString(R.string.sun_altitude)}</b> ${round(trueAltitude, 2)}°<br>" +
                                 if (isMetric) {
                                     "<b>${getString(R.string.sun_distance)}</b> ${String.format("%.3E", distance)} ${getString(R.string.kilometer)}"
                                 } else {
                                     "<b>${getString(R.string.sun_distance)}</b> ${String.format("%.3E", distance.toMiles())} ${getString(R.string.miles)}"
                                 })
                    }

                moonPositionData =
                    with(MoonPosition.compute().timezone(timezone).on(Instant.now()).latitude(latitude).longitude(longitude).elevation(altitude).execute()) {
                        moonAzimuth = this.azimuth

                        fromHtml(buildString {
                            append("<b>${getString(R.string.moon_azimuth)}</b> ${round(azimuth, 2)}° ${getDirectionCodeFromAzimuth(requireContext(), azimuth)}<br>")
                            append("<b>${getString(R.string.moon_altitude)}</b> ${round(this@with.altitude, 2)}°<br>")
                            append("<b>${getString(R.string.true_altitude)}</b> ${round(trueAltitude, 2)}°<br>")
                            append(if (isMetric) {
                                "<b>${getString(R.string.moon_distance)}</b> ${String.format("%.3E", distance)} ${getString(R.string.kilometer)}<br>"
                            } else {
                                "<b>${getString(R.string.moon_distance)}</b> ${String.format("%.3E", distance.toMiles())} ${getString(R.string.miles)}<br>"
                            })
                            append("<b>${getString(R.string.moon_parallactic_angle)}</b> ${round(parallacticAngle, 2)}°")
                        })
                    }

                moonIlluminationData =
                    with(MoonIllumination.compute().timezone(timezone).on(Instant.now()).execute()) {
                        moonPhase = phase

                        fromHtml(buildString {
                            // Moon Fraction
                            append("<b>${getString(R.string.moon_fraction)}</b> ${round(fraction, 2)}")
                            append("<br>")
                            // Moon Angle
                            append("<b>${getString(R.string.moon_angle)}</b> ${round(angle, 2)}°")
                            append("<br>")
                            // Moon Angle State
                            append("<b>${getString(R.string.moon_angle_state)}</b> ${if (angle < 0) getString(R.string.waxing) else getString(R.string.waning)}")
                            append("<br>")
                            // Moon Phase
                            append("<b>${getString(R.string.moon_phase)}</b> ${getMoonPhase(requireContext(), phase)}")
                            append("<br>")
                            // Moon Phase Angle
                            append("<b>${getString(R.string.moon_phase_angle)}</b> ${round(phase, 2)}°")
                            append("<br>")
                            // Moon Elongation
                            if (MainPreferences.isMetric()) {
                                append("<b>${getString(R.string.elongation)}</b> ${String.format("%.3E", elongation)} ${getString(R.string.kilometer)}")
                            } else {
                                append("<b>${getString(R.string.elongation)}</b> ${String.format("%.3E", elongation.toMiles())} ${getString(R.string.miles)}")
                            }
                            append("<br>")
                            // Moon Radius
                            if (MainPreferences.isMetric()) {
                                append("<b>${getString(R.string.radius)}</b> ${String.format("%.3E", radius)} ${getString(R.string.kilometer)}")
                            } else {
                                append("<b>${getString(R.string.radius)}</b> ${String.format("%.3E", radius.toMiles())} ${getString(R.string.miles)}")
                            }
                            append("<br>")
                            // Crescent Width
                            if (MainPreferences.isMetric()) {
                                append("<b>${getString(R.string.crescent_width)}</b> ${String.format("%.3E", crescentWidth)} ${getString(R.string.kilometer)}")
                            } else {
                                append("<b>${getString(R.string.crescent_width)}</b> ${String.format("%.3E", crescentWidth.toMiles())} ${getString(R.string.miles)}")
                            }
                        })
                    }

                moonDatesData =
                    with(MoonPhase.compute().timezone(timezone).on(Instant.now())) {
                        fromHtml("<b>${getString(R.string.moon_full_moon)}</b> ${formatMoonDate(phase(MoonPhase.Phase.FULL_MOON).execute().time)}<br>" +
                                 "<b>${getString(R.string.moon_new_moon)}</b> ${formatMoonDate(phase(MoonPhase.Phase.NEW_MOON).execute().time)}<br>" +
                                 "<b>${getString(R.string.moon_first_quarter)}</b> ${formatMoonDate(phase(MoonPhase.Phase.FIRST_QUARTER).execute().time)}<br>" +
                                 "<b>${getString(R.string.moon_last_quarter)}</b> ${formatMoonDate(phase(MoonPhase.Phase.LAST_QUARTER).execute().time)}<br>")
                    }

                // Unreliable!!!
                // sunMoonCalculator= SunMoonCalculator(getCurrentTimeData(), latitude, longitude, altitude.toInt())
                // sunMoonCalculator!!.calcSunAndMoon()
                // moonBitmap = DrawMoonBitmap.getLunarPhaseBitmap(sunMoonCalculator!!.moonPhase, sunMoonCalculator!!.moon!!.illuminationPhase, sunMoonCalculator!!, latitude, LocationExtension.isHemisphereNorth(latitude))
            }

            try {
                this@Time.sunTimeData.text = sunTimeData
                this@Time.moonTimeData.text = moonTimeData
                this@Time.twilightData.text = twilightData
                this@Time.sunPositionData.text = sunPositionData
                this@Time.moonPositionData.text = moonPositionData
                this@Time.moonIlluminationData.text = moonIlluminationData
                this@Time.moonPhaseGraphics.setImageResource(getMoonPhaseGraphics(round(moonPhase!!, 2)))
                sunPosition.setSunAzimuth(sunAzimuth ?: 0.0)
                sunPosition.setMoonDrawable(moonAzimuth!!, moonPhase!!)
                sunPosition.invalidate()
                //this@Clock.moonPhaseGraphics.rotation = (moonPosition.parallacticAngle - moonIllumination.angle).toFloat()
                //this@Clock.moonPhaseGraphics.setImageBitmap(moonBitmap)
                this@Time.moonDatesData.text = moonDatesData
            } catch (ignored: NullPointerException) {
            } catch (ignored: UninitializedPropertyAccessException) {
            }
        }
    }

    private fun updateTimeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            var localTimeData: Spanned? = null
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
                this@Time.localTimeData.text = localTimeData
                this@Time.utcTimeData.text = utcTimeData
            } catch (ignored: NullPointerException) {
            } catch (ignored: UninitializedPropertyAccessException) {
            }
        }
    }

    private fun getCurrentTimeData(): ZonedDateTime {
        return Instant.now().atZone(ZoneId.of(timezone))
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
        fun newInstance(): Time {
            val args = Bundle()
            val fragment = Time()
            fragment.arguments = args
            return fragment
        }
    }
}
