package app.simple.positional.ui

import android.content.*
import android.content.res.Configuration
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
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
import app.simple.positional.singleton.DistanceSingleton
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
import kotlinx.android.synthetic.main.info_panel_gps.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private var marker: Bitmap? = null

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<CoordinatorLayout>

    private var location: Location? = null

    private var backPress: OnBackPressedDispatcher? = null

    private var isMapMoved = false
    private var isMetric = true
    private var isCustomCoordinate = false

    private var customLatitude = 0.0
    private var customLongitude = 0.0
    private var lastLatitude = 0.0
    private var lastLongitude = 0.0

    private var distanceSingleton = DistanceSingleton

    private lateinit var mapFragment: SupportMapFragment
    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.frag_gps, container, false)

        accuracy = view.findViewById(R.id.gps_accuracy)
        address = view.findViewById(R.id.gps_address)
        latitude = view.findViewById(R.id.latitude_input)
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
        isCustomCoordinate = MainPreferences().isCustomCoordinate(requireContext())

        if (isCustomCoordinate) {
            customLatitude = MainPreferences().getCoordinates(requireContext())[0].toDouble()
            customLongitude = MainPreferences().getCoordinates(requireContext())[1].toDouble()
        }

        distanceSingleton.isNotificationAllowed = if (GPSPreferences().isNotificationOn(requireContext())) {
            view.movement_notification.setImageResource(R.drawable.ic_notifications)
            true
        } else {
            view.movement_notification.setImageResource(R.drawable.ic_notifications_off)
            false
        }

        lastLatitude = GPSPreferences().getLastCoordinates(requireContext())[0].toDouble()
        lastLongitude = GPSPreferences().getLastCoordinates(requireContext())[1].toDouble()

        bottomSheetSlide = requireActivity() as BottomSheetSlide

        backPress = requireActivity().onBackPressedDispatcher

        mapFragment = (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)!!
        mapFragment.getMapAsync(callback)

        distanceSingleton.isMapPanelVisible = true

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

        if (isCustomCoordinate) {
            specified_location_notice_gps.visibility = View.VISIBLE
            gps_divider.visibility = View.VISIBLE

            marker = R.drawable.ic_place_custom.getBitmapFromVectorDrawable(requireContext(), 400)
        } else {
            marker = R.drawable.ic_place.getBitmapFromVectorDrawable(requireContext(), 400)
        }

        providerStatus.text = fromHtml("<b>Status:</b> ${if (getLocationStatus(requireContext())) "Enabled" else "Disabled"}")

        locationIconStatusUpdates()
        checkGooglePlayServices()

        movement_reset.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.button_pressed_scale))
            distanceSingleton.totalDistance = 0f
            if (location != null) {
                distanceSingleton.initialPointCoordinates = LatLng(location!!.latitude, location!!.longitude)
            } else {
                distanceSingleton.isInitialLocationSet = false
            }

            Toast.makeText(requireContext(), "Reset Complete", Toast.LENGTH_SHORT).show()
        }

        movement_notification.setOnClickListener {
            if (distanceSingleton.isNotificationAllowed == true) {
                loadImageResources(R.drawable.ic_notifications_off, movement_notification, requireContext(), 0)
                distanceSingleton.isNotificationAllowed = false
                GPSPreferences().setNotificationMode(requireContext(), false)
            } else {
                loadImageResources(R.drawable.ic_notifications, movement_notification, requireContext(), 0)
                distanceSingleton.isNotificationAllowed = true
                GPSPreferences().setNotificationMode(requireContext(), true)
            }
        }

        locationBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent != null) {
                    when (intent.action) {
                        "location" -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                location = intent.getParcelableExtra("location")!!
                                if (location == null) return@launch

                                if (!distanceSingleton.isInitialLocationSet!!) {
                                    distanceSingleton.initialPointCoordinates = LatLng(location!!.latitude, location!!.longitude)
                                    distanceSingleton.distanceCoordinates = LatLng(location!!.latitude, location!!.longitude)
                                    distanceSingleton.isInitialLocationSet = true
                                }

                                GPSPreferences().setLastLatitude(requireContext(), location!!.latitude.toFloat())
                                GPSPreferences().setLastLongitude(requireContext(), location!!.longitude.toFloat())

                                val providerSource = fromHtml("<b>Source:</b> ${location!!.provider.toUpperCase(Locale.getDefault())}")
                                val providerStatus = fromHtml("<b>Status:</b> ${if (getLocationStatus(requireContext())) "Enabled" else "Disabled"}")

                                val accuracy = if (isMetric) {
                                    fromHtml("<b>Accuracy:</b> ${buildSpannableString("${round(location!!.accuracy.toDouble(), 2)} m", 1)}")
                                } else {
                                    fromHtml("<b>Accuracy:</b> ${buildSpannableString("${round(location!!.accuracy.toDouble().toFeet(), 2)} ft", 1)}")
                                }

                                val altitude = if (isMetric) {
                                    fromHtml("<b>Altitude:</b> ${buildSpannableString("${round(location!!.altitude, 2)} m", 1)}")
                                } else {
                                    fromHtml("<b>Altitude:</b> ${buildSpannableString("${round(location!!.altitude.toFeet(), 2)} ft", 1)}")
                                }

                                val speed = if (isMetric) {
                                    fromHtml("<b>Speed:</b> ${buildSpannableString("${round(location!!.speed.toDouble().toKiloMetersPerHour(), 2)} km/h", 1)}")
                                } else {
                                    fromHtml("<b>Speed:</b> ${buildSpannableString("${round(location!!.speed.toDouble().toKiloMetersPerHour().toMilesPerHour(), 2)} mph", 1)}")
                                }

                                val bearing = fromHtml("<b>Bearing:</b> ${location!!.bearing}°")

                                val displacement: Spanned?
                                val direction: Spanned?

                                val displacementValue = FloatArray(1)
                                val distanceValue = FloatArray(1)
                                Location.distanceBetween(
                                        distanceSingleton.initialPointCoordinates!!.latitude,
                                        distanceSingleton.initialPointCoordinates!!.longitude,
                                        location!!.latitude,
                                        location!!.longitude,
                                        displacementValue
                                )

                                Location.distanceBetween(
                                        distanceSingleton.initialPointCoordinates!!.latitude,
                                        distanceSingleton.initialPointCoordinates!!.longitude,
                                        location!!.latitude,
                                        location!!.longitude,
                                        distanceValue
                                )

                                if (location!!.speed > 0f) {
                                    distanceSingleton.totalDistance = distanceSingleton.totalDistance?.plus(distanceValue[0])
                                    val dir = getDirection(
                                            distanceSingleton.distanceCoordinates!!.latitude,
                                            distanceSingleton.distanceCoordinates!!.longitude,
                                            location!!.latitude,
                                            location!!.longitude
                                    )
                                    distanceSingleton.distanceCoordinates = LatLng(location!!.latitude, location!!.longitude)
                                    direction = fromHtml("<b>Direction:</b> ${round(dir, 2)}° ${getDirectionCodeFromAzimuth(dir)}")
                                } else {
                                    direction = fromHtml("<b>Direction:</b> N/A")
                                }

                                displacement = if (displacementValue[0] < 1000) {
                                    if (isMetric) {
                                        fromHtml("<b>Displacement:</b> ${round(displacementValue[0].toDouble(), 2)} m")
                                    } else {
                                        fromHtml("<b>Displacement:</b> ${round(displacementValue[0].toDouble().toFeet(), 2)} ft")
                                    }
                                } else {
                                    if (isMetric) {
                                        fromHtml("<b>Displacement:</b> ${round(displacementValue[0].toKilometers().toDouble(), 2)} km")
                                    } else {
                                        fromHtml("<b>Displacement:</b> ${round(displacementValue[0].toMiles().toDouble().toFeet(), 2)} miles")
                                    }
                                }

                                val distance: Spanned? = if (distanceSingleton.totalDistance!! < 1000) {
                                    if (isMetric) {
                                        fromHtml("<b>Distance:</b> ${round(distanceSingleton.totalDistance!!.toDouble(), 2)} m")
                                    } else {
                                        fromHtml("<b>Distance:</b> ${round(distanceSingleton.totalDistance!!.toDouble().toFeet(), 2)} ft")
                                    }
                                } else {
                                    if (isMetric) {
                                        fromHtml("<b>Distance:</b> ${round(distanceSingleton.totalDistance!!.toKilometers().toDouble(), 2)} km")
                                    } else {
                                        fromHtml("<b>Distance:</b> ${round(distanceSingleton.totalDistance!!.toMiles().toDouble().toFeet(), 2)} miles")
                                    }
                                }

                                withContext(Dispatchers.Main) {
                                    gps_location_indicator.setImageResource(R.drawable.ic_gps_fixed)
                                    gps_location_indicator.isClickable = true
                                    this@GPS.providerSource.text = providerSource
                                    this@GPS.providerStatus.text = providerStatus
                                    this@GPS.altitude.text = altitude
                                    this@GPS.speed.text = speed
                                    this@GPS.bearing.text = bearing
                                    this@GPS.accuracy.text = accuracy
                                    gps_displacement.text = displacement
                                    gps_distance.text = distance
                                    gps_direction.text = direction

                                    if (!isCustomCoordinate) {
                                        updateViews(location!!.latitude, location!!.longitude, location!!.bearing)
                                    }
                                }
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
            if (isCustomCoordinate) {
                updateViews(customLatitude, customLongitude, 0f)
            } else
                if (location != null) {
                    isMapMoved = false
                    moveMapCamera(LatLng(location!!.latitude, location!!.longitude), location!!.bearing)
                    handler.removeCallbacks(mapMoved)
                }
        }

        gps_copy.setOnClickListener {
            handler.removeCallbacks(textAnimationRunnable)
            val clipboard: ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            if (gps_accuracy.text != "") {
                val stringBuilder = StringBuilder()

                stringBuilder.append("Provider\n")
                stringBuilder.append("${provider_status.text}\n")
                stringBuilder.append("${provider_source.text}\n\n")

                stringBuilder.append("Location\n")
                stringBuilder.append("${gps_accuracy.text}\n")
                stringBuilder.append("${gps_altitude.text}\n")
                stringBuilder.append("${gps_bearing.text}\n\n")

                stringBuilder.append("Movement\n")
                stringBuilder.append("${gps_distance.text}\n")
                stringBuilder.append("${gps_displacement.text}\n")
                stringBuilder.append("${gps_direction.text}\n")
                stringBuilder.append("${gps_speed.text}\n")

                if (isCustomCoordinate) {
                    stringBuilder.append("\n${specified_location_notice_gps.text}\n")
                }

                stringBuilder.append("\nCoordinates\n")
                stringBuilder.append("${latitude.text}\n")
                stringBuilder.append("${longitude.text}\n\n")

                stringBuilder.append("Address: ${address.text}")

                if (BuildConfig.FLAVOR == "lite") {
                    stringBuilder.append("\n\nInformation is copied using Positional Lite\n")
                    stringBuilder.append("Get the app from:\nhttps://play.google.com/store/apps/details?id=app.simple.positional.lite")
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
        handler.removeCallbacks(customDataUpdater)
        gps_info_text.clearAnimation()
        if (backPress!!.hasEnabledCallbacks()) {
            backPressed(false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        distanceSingleton.isMapPanelVisible = false
    }

    override fun onResume() {
        super.onResume()
        if (isCustomCoordinate) {
            handler.post(customDataUpdater)
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(locationBroadcastReceiver, filter)
    }

    private val customDataUpdater: Runnable = object : Runnable {
        override fun run() {
            updateViews(customLatitude, customLongitude, 0f)
            handler.postDelayed(this, 1000)
        }
    }

    private fun updateViews(latitude_: Double, longitude_: Double, bearing: Float) {
        getAddress(latitude_, longitude_)

        moveMapCamera(LatLng(latitude_, longitude_), bearing)

        latitude.text = fromHtml("<b>Latitude:</b> ${LocationConverter.latitudeAsDMS(latitude_, 3)}")
        longitude.text = fromHtml("<b>Longitude:</b> ${LocationConverter.longitudeAsDMS(longitude_, 3)}")
    }

    private val callback = OnMapReadyCallback { googleMap ->

        val latLng = if (isCustomCoordinate) LatLng(customLatitude, customLongitude) else LatLng(lastLatitude, lastLongitude)

        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder().target(latLng).tilt(0f).zoom(18f).build()))
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

    private fun getAddress(latitude: Double, longitude: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            val address: String

            address = try {
                if (context == null) {
                    "!Error Fetching Address "
                } else if (!isNetworkAvailable(requireContext())) {
                    "Internet connection not available"
                } else {
                    val addresses: List<Address>
                    val geocoder = Geocoder(context, Locale.getDefault())

                    @Suppress("BlockingMethodInNonBlockingContext")
                    /**
                     * [Dispatchers.IO] can withstand blocking calls
                     */
                    addresses = geocoder.getFromLocation(latitude, longitude, 1)

                    if (addresses != null && addresses.isNotEmpty()) {
                        addresses[0].getAddressLine(0) //"$city, $state, $country, $postalCode, $knownName"
                    } else {
                        "N/A"
                    }
                }
            } catch (e: IOException) {
                "${e.message}\n!Error Fetching Address"
            } catch (e: NullPointerException) {
                "${e.message}\n!No Address Found"
            } catch (e: IllegalArgumentException) {
                "Invalid Coordinates Supplied"
            }

            withContext(Dispatchers.Main) {
                try {
                    gps_address.text = address
                } catch (e: java.lang.NullPointerException) {
                } catch (e: UninitializedPropertyAccessException) {
                }
            }
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

    private fun moveMapCamera(latLng: LatLng, bearing: Float) {
        if (googleMap == null) return
        if (isMapMoved) return

        val cameraPosition = googleMap?.cameraPosition?.tilt?.let { CameraPosition.builder().target(latLng).tilt(it).zoom(18f).bearing(bearing).build() }

        clearMap()

        val markerOptions = MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(marker))
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