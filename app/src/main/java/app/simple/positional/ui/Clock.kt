package app.simple.positional.ui

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.callbacks.BottomSheetSlide
import app.simple.positional.constants.faces
import app.simple.positional.constants.needleSkins
import app.simple.positional.menu.clock.configuration.MovementType
import app.simple.positional.menu.clock.face.Face
import app.simple.positional.menu.clock.needle.Needle
import app.simple.positional.preference.ClockPreferences
import app.simple.positional.util.*
import com.elyeproj.loaderviewlibrary.LoaderTextView
import com.github.zawadz88.materialpopupmenu.popupMenu
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.shredzone.commons.suncalc.SunPosition
import org.shredzone.commons.suncalc.SunTimes
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

class Clock : Fragment() {
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

    private lateinit var toolbar: MaterialToolbar

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<CoordinatorLayout>

    private lateinit var clockLayout: FrameLayout

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

    var isMovementTypeSmooth = false
    var delay: Long = 1000

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.frag_clock, container, false)

        handler = Handler()

        filter.addAction("location")
        filter.addAction("status")
        filter.addAction("enabled")

        hour = view.findViewById(R.id.hour)
        minutes = view.findViewById(R.id.minutes)
        seconds = view.findViewById(R.id.seconds)
        dial = view.findViewById(R.id.clock_face)
        dial.alpha = ClockPreferences().getFaceOpacity(requireContext())
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

        menu = view.findViewById(R.id.clock_menu)

        bottomSheetSlide = requireActivity() as BottomSheetSlide

        scrollView = view.findViewById(R.id.clock_panel_scrollview)

        isMovementTypeSmooth = ClockPreferences().getMovementType(requireContext())
        setMotionDelay()

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            toolbar = view.findViewById(R.id.clock_appbar)
            clockLayout = view.findViewById(R.id.clock_layout)

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
        date.text = df.format(calendar.time)

        locationBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null) {
                    when (intent.action) {
                        "location" -> {
                            val location: Location = intent.getParcelableExtra("location") ?: return

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

                        "status" -> {
                            // No reason to implement yet
                        }

                        "enabled" -> {
                            // No reason to implement yet
                        }
                    }
                }
            }
        }

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
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
            val popupMenu = popupMenu {
                style = R.style.popupMenu
                dropdownGravity = Gravity.END
                if (BuildConfig.FLAVOR == "full") {
                    section {
                        title = "Appearances"
                        item {
                            label = "Face"
                            hasNestedItems = true
                            icon = R.drawable.ic_minimal
                            callback = {
                                Face().faceSkinsOptions(context = requireContext(), clock = this@Clock)
                            }
                        }
                        item {
                            label = "Needle"
                            hasNestedItems = true
                            icon = R.drawable.ic_clock_needle
                            callback = {
                                Needle().openNeedleMenu(context = requireContext(), clock = this@Clock)
                            }
                        }
                    }
                }
                section {
                    title = "Configuration"
                    item {
                        label = "Motion Type"
                        hasNestedItems = true
                        icon = R.drawable.ic_motion_type
                        callback = {
                            MovementType().setMovementType(requireContext(), this@Clock)
                        }
                    }
                }
            }

            popupMenu.show(context = requireContext(), anchor = menu)
        }
    }

    private fun updateClock(calendar: Calendar) {
        animate(hour, getHoursInDegrees(calendar))
        animate(minutes, getMinutesInDegrees(calendar))
        animate(seconds, getSecondsInDegrees(calendar))
    }

    private val clock: Runnable = object : Runnable {
        override fun run() {
            calendar = Calendar.getInstance()

            hour.rotation = getHoursInDegrees(calendar)
            minutes.rotation = getMinutesInDegrees(calendar)

            seconds.rotation = if (isMovementTypeSmooth) {
                getSecondsInDegreesWithDecimalPrecision(calendar)
            } else {
                getSecondsInDegrees(calendar)
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

    private fun animate(imageView: ImageView, value: Float) {
        val animator = ObjectAnimator.ofFloat(imageView, "rotation", 0f, value)
        animator.duration = 500
        animator.interpolator = DecelerateInterpolator()
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
                handler.removeCallbacks(clock)
            }

            override fun onAnimationEnd(animation: Animator?) {
                handler.post(clock)
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationRepeat(animation: Animator?) {
            }

        })
        animator.start()
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
        setDial(ClockPreferences().getClockFaceTheme(requireContext()))
    }

    fun setNeedle(value: Int) {
        loadImageResources(needleSkins[value][0], hour, requireContext())
        loadImageResources(needleSkins[value][1], minutes, requireContext())
        loadImageResources(needleSkins[value][2], seconds, requireContext())
    }

    fun setDial(value: Int) {
        if (dial.tag != faces[value]) {
            loadImageResources(faces[value], dial, requireContext())
            dial.tag = faces[value]
        }
    }

    fun setFaceAlpha(value: Float) {
        dial.animate().alpha(value).setDuration(1500).setInterpolator(AccelerateDecelerateInterpolator()).start()
    }

    fun setMotionDelay() {
        delay = if (isMovementTypeSmooth) {
            (1000 / requireActivity().windowManager.defaultDisplay.refreshRate).toLong()
        } else {
            1000
        }
    }

    private fun dateUpdater(calendar: Calendar) {
        class GetData : AsyncTask<Void, Void, Array<String>>() {
            override fun doInBackground(vararg params: Void?): Array<String>? {
                return try {
                    val timeZone = TimeZone.getTimeZone(TimeZone.getDefault().id).getDisplayName(false, TimeZone.SHORT)
                    val digitalTime24 = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(calendar.time).toString()
                    val digitalTime12 = SimpleDateFormat("hh:mm:ss", Locale.getDefault()).format(calendar.time).toString()
                    val digitalDaytime = SimpleDateFormat("a", Locale.getDefault()).format(calendar.time).toString().toUpperCase(Locale.getDefault())
                    val utcTimeZone = "GMT ${SimpleDateFormat("XXX", Locale.getDefault()).format(calendar.time)}"
                    val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(OffsetDateTime.now(ZoneOffset.UTC).toString())
                    val utcTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date!!)
                    val utcDate = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(date)

                    arrayOf(timeZone, digitalTime24, digitalTime12, digitalDaytime, utcTimeZone, utcTime, utcDate)
                } catch (e: ParseException) {
                    return null //arrayOf("error!!", "error!!", "error!!", "error!!", "error!!", "error!!", "error!!")
                }
            }

            override fun onPostExecute(result: Array<String>?) {
                super.onPostExecute(result)
                if (result == null) return
                timeZoneView.text = result[0]
                digitalTime24.text = result[1]
                digitalTime12.text = result[2]
                digitalDaytime.text = result[3]
                utcTimeZone.text = result[4]
                utcTime.text = result[5]
                utcDate.text = result[6]
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