package app.simple.positional.ui

import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import app.simple.positional.R
import app.simple.positional.util.*
import app.simple.positional.views.SquareImageView
import com.elyeproj.loaderviewlibrary.LoaderTextView
import org.shredzone.commons.suncalc.SunPosition
import org.shredzone.commons.suncalc.SunTimes
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*


class Clock : Fragment(), LocationListener {
    private lateinit var sunsetTextView: LoaderTextView
    private lateinit var sunriseTextView: LoaderTextView
    private lateinit var digitalTime: LoaderTextView
    private lateinit var digitalDaytime: LoaderTextView
    private lateinit var timeZoneView: LoaderTextView
    private lateinit var sunAzimuth: LoaderTextView
    private lateinit var sunDistance: LoaderTextView
    private lateinit var sunAltitude: LoaderTextView
    private lateinit var date: LoaderTextView

    lateinit var calendar: Calendar

    lateinit var hour: SquareImageView
    lateinit var minutes: SquareImageView
    lateinit var seconds: SquareImageView

    lateinit var handler: Handler

    lateinit var locationManager: LocationManager

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
        digitalTime = view.findViewById(R.id.digital_time)
        digitalDaytime = view.findViewById(R.id.daytime)
        timeZoneView = view.findViewById(R.id.time_zone)
        sunAzimuth = view.findViewById(R.id.sun_azimuth)
        sunDistance = view.findViewById(R.id.sun_distance)
        sunAltitude = view.findViewById(R.id.sun_altitude)
        date = view.findViewById(R.id.today_date)

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                val location: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

                if (location != null) {
                    onLocationChanged(location)
                }
            }
        }

        handler.post(locationRunnable)
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
        timeZoneView.text = calendar.timeZone.displayName
        digitalTime.text = SimpleDateFormat("HH:mm").format(calendar.time).toString()
        digitalDaytime.text = SimpleDateFormat("a").format(calendar.time).toString().toUpperCase()
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

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val pattern: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
                    sunSet = pattern.format(sunTimes.set)
                    sunRise = pattern.format(sunTimes.rise)
                } else {
                    sunSet = formatZonedTimeDate(sunTimes.set.toString())
                    sunRise = formatZonedTimeDate(sunTimes.rise.toString())
                }

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

    private val locationRunnable: Runnable = object : Runnable {
        @SuppressLint("MissingPermission")
        override fun run() {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0.2f, this@Clock)
            handler.postDelayed(this, 1000)
        }
    }
}