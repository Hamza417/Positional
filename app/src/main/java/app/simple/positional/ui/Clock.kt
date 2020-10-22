package app.simple.positional.ui

import android.animation.Animator
import android.animation.ObjectAnimator
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.fragment.app.Fragment
import app.simple.positional.R
import app.simple.positional.location.LocalLocationProvider
import app.simple.positional.location.callbacks.LocationProviderListener
import app.simple.positional.util.getHoursInDegrees
import app.simple.positional.util.getMinutesInDegrees
import app.simple.positional.util.getSecondsInDegrees
import app.simple.positional.util.round
import app.simple.positional.views.SquareImageView
import com.elyeproj.loaderviewlibrary.LoaderTextView
import org.shredzone.commons.suncalc.SunPosition
import org.shredzone.commons.suncalc.SunTimes
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*


class Clock : Fragment(), LocationProviderListener {
    private lateinit var sunsetTextView: LoaderTextView
    private lateinit var sunriseTextView: LoaderTextView
    private lateinit var digitalTime24: LoaderTextView
    private lateinit var digitalTime12: LoaderTextView
    private lateinit var digitalDaytime: LoaderTextView
    private lateinit var timeZoneView: LoaderTextView
    private lateinit var sunAzimuth: LoaderTextView
    private lateinit var sunDistance: LoaderTextView
    private lateinit var sunAltitude: LoaderTextView
    private lateinit var date: LoaderTextView
    private lateinit var utcDate: LoaderTextView
    private lateinit var utcTime: LoaderTextView
    private lateinit var utcTimeZone: LoaderTextView

    lateinit var calendar: Calendar

    lateinit var hour: SquareImageView
    lateinit var minutes: SquareImageView
    lateinit var seconds: SquareImageView

    lateinit var handler: Handler

    lateinit var locationProvider: LocalLocationProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        handler = Handler()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.frag_clock, container, false)
        hour = view.findViewById(R.id.hour)
        minutes = view.findViewById(R.id.minutes)
        seconds = view.findViewById(R.id.seconds)
        sunriseTextView = view.findViewById(R.id.sunrise_time)
        sunsetTextView = view.findViewById(R.id.sunset_time)
        digitalTime24 = view.findViewById(R.id.digital_time_24_hour)
        digitalTime12 = view.findViewById(R.id.digital_time_12_hour)
        digitalDaytime = view.findViewById(R.id.daytime)
        timeZoneView = view.findViewById(R.id.time_zone)
        sunAzimuth = view.findViewById(R.id.sun_azimuth)
        sunDistance = view.findViewById(R.id.sun_distance)
        sunAltitude = view.findViewById(R.id.sun_altitude)
        date = view.findViewById(R.id.today_date)
        utcTimeZone = view.findViewById(R.id.time_zone_UTC)
        utcTime = view.findViewById(R.id.time_UTC)
        utcDate = view.findViewById(R.id.date_UTC)

        locationProvider = LocalLocationProvider()
        locationProvider.init(requireActivity(), this)
        locationProvider.delay = 5000
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        calendar = Calendar.getInstance()
        updateDigitalTime(calendar)
        val df = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
        date.text = df.format(calendar.time)
        animate(hour, getHoursInDegrees(calendar))
        animate(minutes, getMinutesInDegrees(calendar))
        animate(seconds, getSecondsInDegrees(calendar))
    }

    private val clock: Runnable = object : Runnable {
        override fun run() {
            calendar = Calendar.getInstance()

            hour.rotation = getHoursInDegrees(calendar)
            minutes.rotation = getMinutesInDegrees(calendar)
            seconds.rotation = getSecondsInDegrees(calendar)

            updateDigitalTime(calendar)

            handler.postDelayed(this, 1000)
        }
    }

    fun updateDigitalTime(calendar: Calendar) {
        timeZoneView.text = TimeZone.getTimeZone(TimeZone.getDefault().id).getDisplayName(false, TimeZone.SHORT)
        digitalTime24.text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(calendar.time).toString()
        digitalTime12.text = SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(calendar.time).toString()
        digitalDaytime.text = SimpleDateFormat("a", Locale.getDefault()).format(calendar.time).toString().toUpperCase()
        utcTimeZone.text = "GMT ${SimpleDateFormat("XXX", Locale.getDefault()).format(calendar.time)}"
        try {
            val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(OffsetDateTime.now(ZoneOffset.UTC).toString())
            utcTime.text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date)
            utcDate.text = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(date)
        } catch (e: ParseException) {
            utcDate.resetLoader()
            utcTime.resetLoader()
        }
    }

    private fun animate(imageView: ImageView, value: Float) {
        val animator = ObjectAnimator.ofFloat(imageView, "rotation", value)
        animator.duration = 1000
        animator.interpolator = DecelerateInterpolator()
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                handler.post(clock)
            }

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationRepeat(animation: Animator?) {}

        })
        animator.start()
    }

    override fun onLocationChanged(location: Location) {
        if (location.provider == LocationManager.GPS_PROVIDER) {

            // Sunset and Sunrise
            run {
                val sunTimes = SunTimes.compute().at(location.latitude, location.longitude).execute()

                val sunSet: String
                val sunRise: String

                val pattern: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
                sunSet = pattern.format(sunTimes.set)
                sunRise = pattern.format(sunTimes.rise)

                sunriseTextView.text = sunRise
                sunsetTextView.text = sunSet
            }

            run {
                // Twilight
                val pos: SunPosition = SunPosition.compute().today().at(location.latitude, location.longitude).execute()
                sunAzimuth.text = Html.fromHtml("<b>Azimuth:</b> ${round(pos.azimuth, 2)}°")
                sunAltitude.text = Html.fromHtml("<b>Altitude:</b> ${round(pos.trueAltitude, 2)}°")
                sunDistance.text = Html.fromHtml("<b>Distance:</b> ${String.format("%.3E", pos.distance)} km")
            }

        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

    }

    override fun onProviderEnabled(provider: String?) {

    }

    override fun onProviderDisabled(provider: String?) {

    }

    override fun onPause() {
        super.onPause()
        locationProvider.removeLocationCallbacks()
        handler.removeCallbacks(clock)
    }

    override fun onResume() {
        super.onResume()
        locationProvider.initLocationCallbacks()
        handler.post(clock)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(clock)
    }
}