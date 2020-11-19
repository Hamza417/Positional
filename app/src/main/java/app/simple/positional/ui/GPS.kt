package app.simple.positional.ui

import android.content.*
import android.content.res.Configuration
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import app.simple.positional.dialogs.app.PlayServiceIssue
import app.simple.positional.dialogs.gps.GPSMenu
import app.simple.positional.preference.GPSPreferences
import app.simple.positional.preference.MainPreferences
import app.simple.positional.util.*
import com.elyeproj.loaderviewlibrary.LoaderTextView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.frag_gps.*
import kotlinx.android.synthetic.main.info_panel_gps.*
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

class GPS : Fragment() {

    private lateinit var expandUp: ImageView

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

    private var location: Location? = null

    private var backPress: OnBackPressedDispatcher? = null

    private var isMapMoved = false
    private var isMetric = true

    private lateinit var mapFragment: SupportMapFragment
    private var googleMap: GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.frag_gps, container, false)

        accuracy = view.findViewById(R.id.gps_accuracy)
        address = view.findViewById(R.id.gps_address)
        latitude = view.findViewById(R.id.latitude)
        longitude = view.findViewById(R.id.longitude)
        speed = view.findViewById(R.id.gps_speed)
        altitude = view.findViewById(R.id.gps_altitude)
        bearing = view.findViewById(R.id.gps_bearing)
        providerSource = view.findViewById(R.id.provider_source)
        providerStatus = view.findViewById(R.id.provider_status)

        handler = Handler(Looper.getMainLooper())

        filter.addAction("location")
        filter.addAction("provider")

        isMetric = MainPreferences().getUnit(requireContext())

        bottomSheetSlide = requireActivity() as BottomSheetSlide

        backPress = requireActivity().onBackPressedDispatcher

        mapFragment = (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)!!
        mapFragment.getMapAsync(callback)

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

        providerStatus.text = fromHtml("<b>Status:</b> ${if (getLocationStatus(requireContext())) "Enabled" else "Disabled"}")

        locationIconStatusUpdates()
        checkGooglePlayServices()

        locationBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null) {
                    when (intent.action) {
                        "location" -> {

                            location = intent.getParcelableExtra("location")!!

                            if (location != null) {

                                gps_location_indicator.setImageResource(R.drawable.ic_gps_fixed)
                                gps_location_indicator.isClickable = true

                                altitude.text = if (isMetric) {
                                    fromHtml("<b>Altitude:</b> ${buildSpannableString("${round(location!!.altitude, 2)} m", 1)}")
                                } else {
                                    fromHtml("<b>Altitude:</b> ${buildSpannableString("${round(location!!.altitude.toFeet(), 2)} ft", 1)}")
                                }

                                speed.text = if (isMetric) {
                                    fromHtml("<b>Speed:</b> ${buildSpannableString("${round(location!!.speed.toDouble().toKiloMetersPerHour(), 2)} km/h", 1)}")
                                } else {
                                    fromHtml("<b>Speed:</b> ${buildSpannableString("${round(location!!.speed.toDouble().toKiloMetersPerHour().toMilesPerHour(), 2)} mph", 1)}")
                                }

                                bearing.text = fromHtml("<b>Bearing:</b> ${location!!.bearing}°")

                                accuracy.text = if (isMetric) {
                                    fromHtml("<b>Accuracy:</b> ${buildSpannableString("${round(location!!.accuracy.toDouble(), 2)} m", 1)}")
                                } else {
                                    fromHtml("<b>Accuracy:</b> ${buildSpannableString("${round(location!!.accuracy.toDouble().toFeet(), 2)} ft", 1)}")
                                }

                                // For screenshots
                                // location!!.latitude = 27.1751
                                // location!!.longitude = 78.0421

                                getAddress(location!!.latitude, location!!.longitude)

                                moveMapCamera(LatLng(location!!.latitude, location!!.longitude))

                                providerSource.text = fromHtml("<b>Source:</b> ${location!!.provider.toUpperCase(Locale.getDefault())}")
                                providerStatus.text = fromHtml("<b>Status:</b> ${if (getLocationStatus(requireContext())) "Enabled" else "Disabled"}")

                                latitude.text = fromHtml("<b>Latitude:</b> ${LocationConverter.latitudeAsDMS(location!!.latitude, 3)}")
                                longitude.text = fromHtml("<b>Longitude:</b> ${LocationConverter.longitudeAsDMS(location!!.longitude, 3)}")
                            }
                        }
                        "provider" -> {
                            providerStatus.text = fromHtml("<b>Status:</b> ${if (getLocationStatus(requireContext())) "Enabled" else "Disabled"}")
                            providerSource.text = fromHtml("<b>Source:</b> ${intent.getStringExtra("location_provider")?.toUpperCase(Locale.getDefault())}")
                            locationIconStatusUpdates()
                        }
                    }
                }
            }
        }

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {

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
                    expandUp.rotationX = (-180 * slideOffset)
                    // gpsLayout.translationY = 150 * -slideOffset
                    // gpsLayout.alpha = (1 - slideOffset)
                    view.findViewById<View>(R.id.gps_dim).alpha = slideOffset
                    bottomSheetSlide.onBottomSheetSliding(slideOffset)
                    toolbar.translationY = (toolbar.height * -slideOffset)
                }
            })
        }

        gps_menu.setOnClickListener {
            val weakReference = WeakReference(GPSMenu(WeakReference(this@GPS)))
            weakReference.get()?.show(parentFragmentManager, "gps_menu")
        }

        gps_location_indicator.setOnClickListener {
            if (location != null) {
                isMapMoved = false
                moveMapCamera(LatLng(location!!.latitude, location!!.longitude))
                handler.removeCallbacks(mapMoved)
            }
        }

        gps_copy.setOnClickListener {
            val clipboard: ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            if (gps_accuracy.text != "") {
                val stringBuilder = StringBuilder()

                stringBuilder.append("${provider_status.text}\n")
                stringBuilder.append("${provider_source.text}\n")
                stringBuilder.append("Accuracy: ${gps_accuracy.text}\n")
                stringBuilder.append("Altitude: ${gps_altitude.text}\n")
                stringBuilder.append("Speed: ${gps_speed.text}\n")
                stringBuilder.append("${latitude.text}\n")
                stringBuilder.append("${longitude.text}\n")
                stringBuilder.append("Address: ${address.text}\n\n")

                if (BuildConfig.FLAVOR == "lite") {
                    stringBuilder.append("Information is copied using Positional\n")
                    stringBuilder.append("Get the app from:\nhttps://play.google.com/store/apps/details?id=app.simple.positional")
                }

                val clip: ClipData = ClipData.newPlainText("GPS Data", stringBuilder)
                clipboard.setPrimaryClip(clip)
            }

            if (clipboard.hasPrimaryClip()) {
                gps_info_text.setTextAnimation(getString(R.string.info_copied), 300)

                handler.postDelayed(textAnimationRunnable, 3000)
            } else {
                gps_info_text.setTextAnimation(getString(R.string.info_error), 300)

                handler.postDelayed(textAnimationRunnable, 3000)
            }
        }
    }

    private val textAnimationRunnable: Runnable = Runnable { gps_info_text.setTextAnimation("GPS Info", 300) }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationBroadcastReceiver)
        handler.removeCallbacks(mapMoved)
        handler.removeCallbacks(textAnimationRunnable)
        gps_info_text.clearAnimation()
        if (backPress!!.hasEnabledCallbacks()) {
            backPressed(false)
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(locationBroadcastReceiver, filter)
    }

    private val callback = OnMapReadyCallback { googleMap ->
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder().target(LatLng(48.8584, 2.2945)).tilt(90f).zoom(18f).build()))
        googleMap.uiSettings.isCompassEnabled = false
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.uiSettings.isMyLocationButtonEnabled = false
        this.googleMap = googleMap

        showLabel(GPSPreferences().isLabelOn(requireContext()))
        setSatellite(GPSPreferences().isSatelliteOn(requireContext()))

        this.googleMap?.setOnCameraMoveListener {
            isMapMoved = true
            handler.removeCallbacks(mapMoved)
        }

        this.googleMap?.setOnCameraIdleListener {
            handler.postDelayed(mapMoved, 10000)
        }
    }

    @Suppress("deprecation")
    private fun getAddress(latitude: Double, longitude: Double) {
        class GetAddress : AsyncTask<Void, Void, String>() {
            override fun doInBackground(vararg params: Void?): String? {
                return try {
                    if (context == null) {
                        return "N/A"
                    }

                    if (!isNetworkAvailable(requireContext())) {
                        return "Internet connection not available"
                    }

                    val addresses: List<Address>
                    val geocoder = Geocoder(requireContext(), Locale.getDefault())

                    addresses = geocoder.getFromLocation(latitude, longitude, 1) // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                    if (addresses != null && addresses.isNotEmpty()) {
                        addresses[0].getAddressLine(0) //"$city, $state, $country, $postalCode, $knownName"
                    } else {
                        return "N/A"
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    "${e.message}\n!Error Fetching Address"
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                    "${e.message}\n!Error Fetching Address"
                }
            }

            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
                address.text = result ?: "N/A"
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

    private val mapMoved: Runnable = object : Runnable {
        override fun run() {
            if (context == null) return
            isMapMoved = false
        }
    }

    private fun locationIconStatusUpdates() {
        if (getLocationStatus(requireContext())) {
            gps_location_indicator.setImageResource(R.drawable.ic_gps_not_fixed)
        } else {
            gps_location_indicator.setImageResource(R.drawable.ic_gps_off)
            gps_location_indicator.isClickable = false
        }
    }

    private fun moveMapCamera(latLng: LatLng) {
        if (googleMap == null) return
        if (isMapMoved) return

        val cameraPosition = googleMap?.cameraPosition?.tilt?.let { CameraPosition.builder().target(latLng).tilt(it).zoom(18f).bearing(location!!.bearing).build() }

        clearMap()

        val markerOptions = MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(R.drawable.ic_place.getBitmapFromVectorDrawable(requireContext(), 400)))
        googleMap?.addMarker(markerOptions)

        googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 3000, null)
    }

    private fun clearMap() {
        googleMap?.clear()
    }

    fun showLabel(value: Boolean) {
        if (googleMap == null) return

        GPSPreferences().setLabelMode(requireContext(), value)

        var mapRawStyle = 0

        when (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                mapRawStyle = if (value) {
                    R.raw.maps_dark_labelled
                } else {
                    R.raw.maps_dark_no_label
                }
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                mapRawStyle = if (value) {
                    R.raw.maps_light_labelled
                } else {
                    R.raw.maps_no_label
                }
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {

            }
        }

        val mapStyleOptions: MapStyleOptions = MapStyleOptions.loadRawResourceStyle(requireContext(), mapRawStyle)

        googleMap?.setMapStyle(mapStyleOptions)
    }

    fun setSatellite(value: Boolean) {
        if (googleMap == null) return

        if (value) {
            googleMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
        } else {
            googleMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
        }
    }

    // return value preserved, might be used later
    private fun checkGooglePlayServices(): Boolean {
        val availability = GoogleApiAvailability.getInstance()
        val resultCode = availability.isGooglePlayServicesAvailable(requireContext())
        if (resultCode != ConnectionResult.SUCCESS) {
            if (MainPreferences().getShowPlayServiceDialog(requireContext())) {
                val playServiceIssue = PlayServiceIssue().newInstance()
                playServiceIssue.show(parentFragmentManager, "null")
            }
            return false
        }
        return true
    }

    private fun backPressed(value: Boolean) {
        backPress?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(value) {
            override fun handleOnBackPressed() {
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }

                // Remove this callback
                remove()
            }
        })
    }
}