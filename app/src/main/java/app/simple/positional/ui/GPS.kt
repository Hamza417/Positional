package app.simple.positional.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.positional.R
import app.simple.positional.callbacks.BottomSheetSlide
import app.simple.positional.util.*
import com.elyeproj.loaderviewlibrary.LoaderTextView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.frag_gps.*
import java.io.IOException
import java.util.*

// TODO - Add manual longitude adn latitude information checker
class GPS : Fragment() {

    private lateinit var range: ImageView
    private lateinit var dot: ImageView
    private lateinit var scanned: ImageView
    private lateinit var expandUp: ImageView
    private lateinit var locationPin: ImageView
    private lateinit var streetMap: ImageView

    private lateinit var scrollView: NestedScrollView

    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottomSheetSlide: BottomSheetSlide

    private lateinit var accuracy: LoaderTextView
    private lateinit var address: LoaderTextView
    private lateinit var latitude: LoaderTextView
    private lateinit var longitude: LoaderTextView
    private lateinit var providerStatus: LoaderTextView
    private lateinit var providerSource: LoaderTextView
    private lateinit var altitude: LoaderTextView
    private lateinit var bearing: LoaderTextView
    private lateinit var speed: LoaderTextView
    private lateinit var handler: Handler
    private var filter: IntentFilter = IntentFilter()
    private lateinit var locationBroadcastReceiver: BroadcastReceiver

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<CoordinatorLayout>

    private lateinit var gpsLayout: FrameLayout

    private var gpsEnabledAnimationCount = 1
    private var gpsDisabledAnimationCount = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.frag_gps, container, false)
        range = view.findViewById(R.id.gps_range_indicator)
        dot = view.findViewById(R.id.gps_dot)
        scanned = view.findViewById(R.id.gps_scanned_indicator)
        locationPin = view.findViewById(R.id.location_pin)
        streetMap = view.findViewById(R.id.background_street_map)

        accuracy = view.findViewById(R.id.gps_accuracy)
        address = view.findViewById(R.id.gps_address)
        latitude = view.findViewById(R.id.latitude)
        longitude = view.findViewById(R.id.longitude)
        speed = view.findViewById(R.id.gps_speed)
        altitude = view.findViewById(R.id.gps_altitude)
        bearing = view.findViewById(R.id.gps_bearing)
        providerSource = view.findViewById(R.id.provider_source)
        providerStatus = view.findViewById(R.id.provider_status)

        handler = Handler()

        filter.addAction("location")
        filter.addAction("provider")

        gpsLayout = view.findViewById(R.id.gps_layout)
        //gpsLayout.rotationX = 40f

        bottomSheetSlide = requireActivity() as BottomSheetSlide

        loadImageResources(R.drawable.map_random_street, streetMap, requireContext())

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            toolbar = view.findViewById(R.id.gps_appbar)

            scrollView = view.findViewById(R.id.gps_list_scroll_view)
            scrollView.alpha = 0f

            expandUp = view.findViewById(R.id.expand_up_gps_sheet)
            bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.gps_info_bottom_sheet))
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        providerStatus.text = fromHtml("<b>Status:</b> ${if (getLocationStatus()) "Enabled" else "Disabled"}")

        locationBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null) {
                    when (intent.action) {
                        "location" -> {
                            val location: Location? = intent.getParcelableExtra("location")
                            if (location != null) {
                                altitude.text = buildSpannableString("${round(location.altitude, 2)} m", 1)
                                speed.text = buildSpannableString("${round(location.speed.toDouble(), 2)} m/s", 3)
                                bearing.text = "${location.bearing} °"

                                // Bogus coding
                                //providerSource.text = Html.fromHtml("<b>Source:</b> ${location.provider.toUpperCase(Locale.getDefault())}")
                                //providerStatus.text = Html.fromHtml("<b>Status:</b> Enabled")

                                accuracy.text = buildSpannableString("${round(location.accuracy.toDouble(), 2)} m", 1)

                                // For screenshots
                                // location.latitude = -28.425751
                                // location.longitude = 134.239923

                                getAddress(location.latitude, location.longitude)

                                changeMapScale(15f / if (location.accuracy <= 15f) location.accuracy else 15f)

                                changeRangeSize(if (0.066f * location.accuracy <= 1f) 0.066f * location.accuracy else 1.0f)

                                providerSource.text = fromHtml("<b>Source:</b> ${location.provider.toUpperCase(Locale.getDefault())}")
                                providerStatus.text = fromHtml("<b>Status:</b> ${if (getLocationStatus()) "Enabled" else "Disabled"}")

                                latitude.text = fromHtml("<b>Latitude:</b> ${LocationConverter.latitudeAsDMS(location.latitude, 3)}")
                                longitude.text = fromHtml("<b>Longitude:</b> ${LocationConverter.longitudeAsDMS(location.longitude, 3)}°")
                            }
                        }
                        "provider" -> {
                            providerStatus.text = fromHtml("<b>Status:</b> ${if (getLocationStatus()) "Enabled" else "Disabled"}")
                            providerSource.text = fromHtml("<b>Source:</b> ${intent.getStringExtra("location_provider")?.toUpperCase(Locale.getDefault())}")
                        }
                    }
                }
            }
        }

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {

            gps_main_layout.setProxyView(view)

            bottomSheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onStateChanged(bottomSheet: View, newState: Int) {

                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    scrollView.alpha = slideOffset
                    expandUp.alpha = (1 - slideOffset)
                    expandUp.rotationX = (-180 * slideOffset)
                    // gpsLayout.translationY = 150 * -slideOffset
                    // gpsLayout.alpha = (1 - slideOffset)
                    view.findViewById<View>(R.id.gps_dim).alpha = slideOffset
                    bottomSheetSlide.onBottomSheetSliding(slideOffset)
                    toolbar.translationY = (toolbar.height * -slideOffset)
                }
            })
        }
    }

    override fun onPause() {
        super.onPause()
        scanned.clearAnimation()
        range.clearAnimation()
        locationPin.clearAnimation()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationBroadcastReceiver)
        handler.removeCallbacks(gpsRangeAnimation)
        handler.removeCallbacks(locationPinAnimation)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(locationBroadcastReceiver, filter)
        handler.post(gpsRangeAnimation)
        handler.post(locationPinAnimation)
    }

    private fun changeRangeSize(value: Float) {
        range.animate().scaleX(value).scaleY(value).setDuration(1500).setInterpolator(AccelerateDecelerateInterpolator()).start()
    }

    private fun changeMapScale(value: Float) {
        //if (streetMap.animation.hasEnded()) return
        streetMap.animate().scaleX(value).scaleY(value).setDuration(3000).setInterpolator(DecelerateInterpolator()).start()
    }

    private fun getAddress(latitude: Double, longitude: Double) {
        class GetAddress : AsyncTask<Void, Void, String>() {
            override fun doInBackground(vararg params: Void?): String? {

                if (context == null) {
                    return ""
                }

                if (!isNetworkAvailable(requireContext())) {
                    return "Internet connection not available"
                }

                try {
                    val addresses: List<Address>
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())

                    addresses = geocoder.getFromLocation(latitude, longitude, 1) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                    val address: String = addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                    val city: String = addresses[0].locality
                    val state: String = addresses[0].adminArea
                    val country: String = addresses[0].countryName
                    val postalCode: String = addresses[0].postalCode
                    val knownName: String = addresses[0].featureName // Only if available else return NULL

                    return address //"$city, $state, $country, $postalCode, $knownName"
                } catch (e: IOException) {
                    e.printStackTrace()
                    return "${e.message}\n!Error Fetching Address"
                }
            }

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
                address.text = result
            }
        }

        val getAddress = GetAddress()
        if (getAddress.status == AsyncTask.Status.RUNNING) {
            if (getAddress.cancel(true)) {
                getAddress.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
            }
        } else {
            getAddress.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    }

    private val gpsRangeAnimation: Runnable = object : Runnable {
        override fun run() {
            if (context == null) return
            this@GPS.scanned.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.gps_scanned_animation))
            handler.postDelayed(this, 4000)
        }
    }

    private val locationPinAnimation: Runnable = object : Runnable {
        override fun run() {
            if (context == null) return
            if (getLocationStatus()) {
                if (gpsEnabledAnimationCount != 0) {
                    this@GPS.locationPin.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.gps_enabled))
                    gpsEnabledAnimationCount = 0
                    gpsDisabledAnimationCount = 1
                } else {
                    this@GPS.locationPin.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.location_pin_animation))
                }
            } else {
                if (gpsDisabledAnimationCount != 0) {
                    this@GPS.locationPin.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.gps_disabled))
                    gpsDisabledAnimationCount = 0
                    gpsEnabledAnimationCount = 1
                }
            }

            handler.postDelayed(this, 3000)
        }
    }

    private fun getLocationStatus(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }
}