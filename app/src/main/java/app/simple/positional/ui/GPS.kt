package app.simple.positional.ui

import android.content.*
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
import android.widget.ImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.callbacks.BottomSheetSlide
import app.simple.positional.dialogs.gps.GPSMenu
import app.simple.positional.preference.GPSPreferences
import app.simple.positional.util.*
import com.elyeproj.loaderviewlibrary.LoaderTextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.frag_gps.*
import kotlinx.android.synthetic.main.gps_info_cards.*
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

// TODO - Add manual longitude adn latitude information checker
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

    private var isMapMoved: Boolean = false

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap

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

        handler = Handler()

        filter.addAction("location")
        filter.addAction("provider")

        //gpsLayout.rotationX = 40f

        bottomSheetSlide = requireActivity() as BottomSheetSlide

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

        providerStatus.text = fromHtml("<b>Status:</b> ${if (getLocationStatus()) "Enabled" else "Disabled"}")

        locationBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null) {
                    when (intent.action) {
                        "location" -> {

                            location = intent.getParcelableExtra("location")!!

                            if (location != null) {
                                altitude.text = fromHtml("<b>Altitude:</b> ${buildSpannableString("${round(location!!.altitude, 2)} m", 1)}")
                                speed.text = fromHtml("<b>Speed:</b> ${buildSpannableString("${round(location!!.speed.toDouble(), 2)} m", 1)}")
                                bearing.text = fromHtml("<b>Bearing:</b> ${location!!.bearing}°")

                                accuracy.text = fromHtml("<b>Accuracy:</b> ${buildSpannableString("${round(location!!.accuracy.toDouble(), 2)} m", 1)}")

                                // For screenshots
                                //location!!.latitude = 48.8584
                                //location!!.longitude = 2.2945

                                getAddress(location!!.latitude, location!!.longitude)

                                moveMapCamera(LatLng(location!!.latitude, location!!.longitude))

                                providerSource.text = fromHtml("<b>Source:</b> ${location!!.provider.toUpperCase(Locale.getDefault())}")
                                providerStatus.text = fromHtml("<b>Status:</b> ${if (getLocationStatus()) "Enabled" else "Disabled"}")

                                latitude.text = fromHtml("<b>Latitude:</b> ${LocationConverter.latitudeAsDMS(location!!.latitude, 3)}")
                                longitude.text = fromHtml("<b>Longitude:</b> ${LocationConverter.longitudeAsDMS(location!!.longitude, 3)}°")
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

        gps_menu.setOnClickListener {
            val weakReference = WeakReference(GPSMenu(WeakReference(this@GPS)))
            weakReference.get()?.show(parentFragmentManager, "gps_menu")
        }

        gps_location_reset.setOnClickListener {
            isMapMoved = false
            moveMapCamera(LatLng(48.8584, 2.2945))
            handler.removeCallbacks(mapMoved)
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

                handler.postDelayed({
                    gps_info_text.setTextAnimation("GPS Info", 300)
                }, 3000)
            } else {
                gps_info_text.setTextAnimation(getString(R.string.info_error), 300)

                handler.postDelayed({
                    gps_info_text.setTextAnimation("GPS Info", 300)
                }, 3000)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationBroadcastReceiver)
        handler.removeCallbacks(mapMoved)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(locationBroadcastReceiver, filter)
    }

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */

        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder().target(LatLng(48.8584, 2.2945)).tilt(90f).zoom(18f).build()))
        googleMap.uiSettings.isCompassEnabled = false
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.uiSettings.isMyLocationButtonEnabled = false
        this.googleMap = googleMap

        showLabel(GPSPreferences().isLabelOn(requireContext()))

        this.googleMap.setOnCameraMoveListener {
            isMapMoved = true
            handler.removeCallbacks(mapMoved)
        }

        this.googleMap.setOnCameraIdleListener {
            handler.postDelayed(mapMoved, 10000)
        }
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

    private val mapMoved: Runnable = object : Runnable {
        override fun run() {
            if (context == null) return
            isMapMoved = false
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

    private fun moveMapCamera(latLng: LatLng) {
        if (isMapMoved) return

        val cameraPosition = CameraPosition.builder().target(latLng).tilt(googleMap.cameraPosition.tilt).zoom(18f).bearing(location!!.bearing).build()

        clearMap()

        val markerOptions = MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(R.drawable.ic_place.getBitmapFromVectorDrawable(requireContext())))
        googleMap.addMarker(markerOptions)

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 3000, null)
    }

    private fun clearMap() {
        googleMap.clear()
    }

    fun showLabel(value: Boolean) {
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

        googleMap.setMapStyle(mapStyleOptions)
    }
}