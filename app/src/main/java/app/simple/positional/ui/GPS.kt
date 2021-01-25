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
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.room.Room
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.callbacks.BottomSheetSlide
import app.simple.positional.database.LocationDatabase
import app.simple.positional.dialogs.app.PlayServiceIssue
import app.simple.positional.dialogs.gps.GPSMenu
import app.simple.positional.math.MathExtensions.round
import app.simple.positional.math.UnitConverter.toFeet
import app.simple.positional.math.UnitConverter.toKiloMetersPerHour
import app.simple.positional.math.UnitConverter.toKilometers
import app.simple.positional.math.UnitConverter.toMiles
import app.simple.positional.math.UnitConverter.toMilesPerHour
import app.simple.positional.model.Locations
import app.simple.positional.preference.GPSPreferences
import app.simple.positional.preference.MainPreferences
import app.simple.positional.singleton.DistanceSingleton
import app.simple.positional.util.*
import app.simple.positional.util.Direction.getDirectionCodeFromAzimuth
import app.simple.positional.util.HtmlHelper.fromHtml
import app.simple.positional.util.LocationExtension.getDirection
import app.simple.positional.util.LocationExtension.getLocationStatus
import app.simple.positional.util.NullSafety.isNull
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

class GPS : Fragment() {

    fun newInstance(): GPS {
        val args = Bundle()
        val fragment = GPS()
        fragment.arguments = args
        return fragment
    }

    private lateinit var expandUp: ImageView

    private lateinit var scrollView: NestedScrollView
    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottomSheetSlide: BottomSheetSlide
    private lateinit var divider: View
    private lateinit var locationIndicator: ImageButton
    private lateinit var menu: ImageButton
    private lateinit var copy: ImageButton
    private lateinit var save: ImageButton
    private lateinit var movementReset: ImageButton

    private lateinit var accuracy: TextView
    private lateinit var address: TextView
    private lateinit var latitude: TextView
    private lateinit var longitude: TextView
    private lateinit var providerStatus: TextView
    private lateinit var providerSource: TextView
    private lateinit var altitude: TextView
    private lateinit var bearing: TextView
    private lateinit var displacement: TextView
    private lateinit var direction: TextView
    private lateinit var speed: TextView
    private lateinit var specifiedLocationTextView: TextView
    private lateinit var infoText: TextView

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
    private var mapView: MapView? = null
    private var googleMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.frag_gps, container, false)

        toolbar = view.findViewById(R.id.gps_appbar)
        scrollView = view.findViewById(R.id.gps_list_scroll_view)
        scrollView.alpha = 0f
        divider = view.findViewById(R.id.gps_divider)
        locationIndicator = view.findViewById(R.id.gps_location_indicator)
        menu = view.findViewById(R.id.gps_menu)
        copy = view.findViewById(R.id.gps_copy)
        save = view.findViewById(R.id.gps_save)
        movementReset = view.findViewById(R.id.movement_reset)
        expandUp = view.findViewById(R.id.expand_up_gps_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.gps_info_bottom_sheet))

        accuracy = view.findViewById(R.id.gps_accuracy)
        address = view.findViewById(R.id.gps_address)
        latitude = view.findViewById(R.id.latitude_input)
        longitude = view.findViewById(R.id.longitude)
        providerSource = view.findViewById(R.id.provider_source)
        providerStatus = view.findViewById(R.id.provider_status)
        altitude = view.findViewById(R.id.gps_altitude)
        bearing = view.findViewById(R.id.gps_bearing)
        displacement = view.findViewById(R.id.gps_displacement)
        direction = view.findViewById(R.id.gps_direction)
        speed = view.findViewById(R.id.gps_speed)
        specifiedLocationTextView = view.findViewById(R.id.specified_location_notice_gps)
        infoText = view.findViewById(R.id.gps_info_text)

        handler = Handler(Looper.getMainLooper())

        filter.addAction("location")
        filter.addAction("provider")

        isMetric = MainPreferences.getUnit()

        if (MainPreferences.isCustomCoordinate()) {
            isCustomCoordinate = true
            customLatitude = MainPreferences.getCoordinates()[0].toDouble()
            customLongitude = MainPreferences.getCoordinates()[1].toDouble()
        }

        distanceSingleton.isMapPanelVisible = true

        lastLatitude = GPSPreferences.getLastCoordinates()[0].toDouble()
        lastLongitude = GPSPreferences.getLastCoordinates()[1].toDouble()

        bottomSheetSlide = requireActivity() as BottomSheetSlide
        backPress = requireActivity().onBackPressedDispatcher

        handler.postDelayed({
            /**
             * This prevents the lag when fragment is switched
             */
            mapView = view.findViewById(R.id.map)
            mapView?.alpha = 0F
            mapView?.onCreate(savedInstanceState)
            mapView?.getMapAsync(callback)
        }, 250)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isCustomCoordinate) {
            specifiedLocationTextView.isVisible = true
            divider.isVisible = true
            marker = R.drawable.ic_place_custom.getBitmapFromVectorDrawable(requireContext(), 400)
        } else {
            marker = R.drawable.ic_place.getBitmapFromVectorDrawable(requireContext(), 400)
        }

        providerStatus.text = fromHtml("<b>${getString(R.string.gps_status)}</b> ${if (getLocationStatus(requireContext())) getString(R.string.gps_enabled) else getString(R.string.gps_disabled)}")

        locationIconStatusUpdates()
        checkGooglePlayServices()

        movementReset.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.button_pressed_scale))
            distanceSingleton.totalDistance = 0f
            if (location != null) {
                distanceSingleton.initialPointCoordinates = LatLng(location!!.latitude, location!!.longitude)
            } else {
                distanceSingleton.isInitialLocationSet = false
            }

            Toast.makeText(requireContext(), R.string.reset_complete, Toast.LENGTH_SHORT).show()
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

                                GPSPreferences.setLastLatitude(location!!.latitude.toFloat())
                                GPSPreferences.setLastLongitude(location!!.longitude.toFloat())

                                val providerSource = fromHtml("<b>${getString(R.string.gps_source)}</b> ${location!!.provider.toUpperCase(Locale.getDefault())}")
                                val providerStatus = fromHtml("<b>${getString(R.string.gps_status)}</b> ${if (getLocationStatus(requireContext())) getString(R.string.gps_enabled) else getString(R.string.gps_disabled)}")

                                val accuracy = if (isMetric) {
                                    fromHtml("<b>${getString(R.string.gps_accuracy)}</b> ${round(location!!.accuracy.toDouble(), 2)} ${getString(R.string.meter)}")
                                } else {
                                    fromHtml("<b>${getString(R.string.gps_accuracy)}</b> ${round(location!!.accuracy.toDouble().toFeet(), 2)} ${getString(R.string.feet)}")
                                }

                                val altitude = if (isMetric) {
                                    fromHtml("<b>${getString(R.string.gps_altitude)}</b> ${round(location!!.altitude, 2)} ${getString(R.string.meter)}")
                                } else {
                                    fromHtml("<b>${getString(R.string.gps_altitude)}</b> ${round(location!!.altitude.toFeet(), 2)} ${getString(R.string.feet)}")
                                }

                                val speed = if (isMetric) {
                                    fromHtml("<b>${getString(R.string.gps_speed)}</b> ${round(location!!.speed.toDouble().toKiloMetersPerHour(), 2)} ${getString(R.string.kilometer_hour)}")
                                } else {
                                    fromHtml("<b>${getString(R.string.gps_speed)}</b> ${round(location!!.speed.toDouble().toKiloMetersPerHour().toMilesPerHour(), 2)} ${getString(R.string.miles_hour)}")
                                }

                                val bearing = fromHtml("<b>${getString(R.string.gps_bearing)}</b> ${location!!.bearing}°")

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
                                    direction = fromHtml("<b>${getString(R.string.gps_direction)}</b> ${round(dir, 2)}° ${getDirectionCodeFromAzimuth(requireContext(), dir)}")
                                } else {
                                    direction = fromHtml("<b>${getString(R.string.gps_direction)}</b> N/A")
                                }

                                displacement = if (displacementValue[0] < 1000) {
                                    if (isMetric) {
                                        fromHtml("<b>${getString(R.string.gps_displacement)}</b> ${round(displacementValue[0].toDouble(), 2)} ${getString(R.string.meter)}")
                                    } else {
                                        fromHtml("<b>${getString(R.string.gps_displacement)}</b> ${round(displacementValue[0].toDouble().toFeet(), 2)} ${getString(R.string.feet)}")
                                    }
                                } else {
                                    if (isMetric) {
                                        fromHtml("<b>${getString(R.string.gps_displacement)}</b> ${round(displacementValue[0].toKilometers().toDouble(), 2)} ${getString(R.string.kilometer)}")
                                    } else {
                                        fromHtml("<b>${getString(R.string.gps_displacement)}</b> ${round(displacementValue[0].toMiles().toDouble().toFeet(), 2)} ${getString(R.string.miles)}")
                                    }
                                }

                                withContext(Dispatchers.Main) {
                                    this@GPS.locationIndicator.setImageResource(R.drawable.ic_gps_fixed)
                                    this@GPS.locationIndicator.isClickable = true
                                    this@GPS.providerSource.text = providerSource
                                    this@GPS.providerStatus.text = providerStatus
                                    this@GPS.altitude.text = altitude
                                    this@GPS.speed.text = speed
                                    this@GPS.bearing.text = bearing
                                    this@GPS.accuracy.text = accuracy
                                    this@GPS.displacement.text = displacement
                                    this@GPS.direction.text = direction

                                    if (!isCustomCoordinate) {
                                        updateViews(location!!.latitude, location!!.longitude, location!!.bearing)
                                    }
                                }
                            }
                        }
                        "provider" -> {
                            providerStatus.text = fromHtml("<b>${getString(R.string.gps_status)}</b> ${if (getLocationStatus(requireContext())) getString(R.string.gps_enabled) else getString(R.string.gps_disabled)}")
                            providerSource.text = fromHtml("<b>${getString(R.string.gps_source)}</b> ${intent.getStringExtra("location_provider")?.toUpperCase(Locale.getDefault())}")
                            locationIconStatusUpdates()
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
                expandUp.rotationX = (-180 * slideOffset)
                view.findViewById<View>(R.id.gps_dim).alpha = slideOffset
                bottomSheetSlide.onBottomSheetSliding(slideOffset)
                toolbar.translationY = (toolbar.height * -slideOffset)
            }
        })

        menu.setOnClickListener {
            val weakReference = WeakReference(GPSMenu(WeakReference(this@GPS)))
            weakReference.get()?.show(parentFragmentManager, "gps_menu")
        }

        locationIndicator.setOnClickListener {
            if (isCustomCoordinate) {
                isMapMoved = false
                updateViews(customLatitude, customLongitude, 0f)
            } else
                if (location != null) {
                    isMapMoved = false
                    moveMapCamera(LatLng(location!!.latitude, location!!.longitude), location!!.bearing)
                    handler.removeCallbacks(mapMoved)
                }
        }

        save.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                val isLocationSaved: Boolean
                val db = Room.databaseBuilder(requireContext(), LocationDatabase::class.java, "locations.db").build()
                val locations = Locations()
                if (location.isNull()) {
                    Toast.makeText(requireContext(), R.string.location_not_available, Toast.LENGTH_SHORT).show()
                    isLocationSaved = false
                } else {
                    locations.latitude = location?.latitude!!
                    locations.longitude = location?.longitude!!
                    locations.address = address.text.toString()
                    locations.timeZone = Calendar.getInstance().timeZone.id
                    locations.date = System.currentTimeMillis()

                    db.locationDao()?.insetLocation(locations)
                    db.close()

                    isLocationSaved = true
                }

                withContext(Dispatchers.Main) {
                    handler.removeCallbacks(textAnimationRunnable)
                    if (isLocationSaved) {
                        infoText.setTextAnimation(getString(R.string.location_saved), 300)
                        handler.postDelayed(textAnimationRunnable, 3000)
                    } else {
                        infoText.setTextAnimation(getString(R.string.location_not_saved), 300)
                        handler.postDelayed(textAnimationRunnable, 3000)
                    }
                }
            }
        }

        copy.setOnClickListener {
            handler.removeCallbacks(textAnimationRunnable)
            val clipboard: ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            if (accuracy.text != "") {
                val stringBuilder = StringBuilder()

                stringBuilder.append("${getString(R.string.gps_provider)}\n")
                stringBuilder.append("${providerStatus.text}\n")
                stringBuilder.append("${providerSource.text}\n\n")

                stringBuilder.append("${getString(R.string.gps_location)}\n")
                stringBuilder.append("${accuracy.text}\n")
                stringBuilder.append("${altitude.text}\n")
                stringBuilder.append("${bearing.text}\n\n")

                stringBuilder.append("${getString(R.string.gps_movement)}\n")
                stringBuilder.append("${displacement.text}\n")
                stringBuilder.append("${direction.text}\n")
                stringBuilder.append("${speed.text}\n")

                if (isCustomCoordinate) {
                    stringBuilder.append("\n${specifiedLocationTextView.text}\n")
                }

                stringBuilder.append("\n${getString(R.string.gps_coordinates)}\n")
                stringBuilder.append("${latitude.text}\n")
                stringBuilder.append("${longitude.text}\n\n")

                stringBuilder.append("${getString(R.string.gps_address)}: ${address.text}")

                if (BuildConfig.FLAVOR == "lite") {
                    stringBuilder.append("\n\nInformation is copied using Positional Lite\n")
                    stringBuilder.append("Get the app from:\nhttps://play.google.com/store/apps/details?id=app.simple.positional.lite")
                }

                val clip: ClipData = ClipData.newPlainText("GPS Data", stringBuilder)
                clipboard.setPrimaryClip(clip)
            }

            if (clipboard.hasPrimaryClip()) {
                infoText.setTextAnimation(getString(R.string.info_copied), 300)
                handler.postDelayed(textAnimationRunnable, 3000)
            } else {
                infoText.setTextAnimation(getString(R.string.info_error), 300)
                handler.postDelayed(textAnimationRunnable, 3000)
            }
        }
    }

    private val textAnimationRunnable: Runnable = Runnable {
        infoText.setTextAnimation(getString(R.string.gps_info), 300)
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationBroadcastReceiver)
        handler.removeCallbacks(mapMoved)
        handler.removeCallbacks(textAnimationRunnable)
        handler.removeCallbacks(customDataUpdater)
        infoText.clearAnimation()
        if (backPress!!.hasEnabledCallbacks()) {
            backPressed(false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
        distanceSingleton.isMapPanelVisible = false
    }

    override fun onResume() {
        super.onResume()
        if (isCustomCoordinate) {
            handler.post(customDataUpdater)
        }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(locationBroadcastReceiver, filter)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
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

        latitude.text = fromHtml("<b>${getString(R.string.gps_latitude)}</b> ${LocationConverter.latitudeAsDMS(latitude_, 3)}")
        longitude.text = fromHtml("<b>${getString(R.string.gps_longitude)}</b> ${LocationConverter.longitudeAsDMS(longitude_, 3)}")
    }

    private val callback = OnMapReadyCallback { googleMap ->
        MapsInitializer.initialize(requireContext())
        mapView?.onResume()

        /**
         * Workaround for flashing of view when map is
         * Initialized
         */
        mapView?.animate()?.alpha(1F)?.setDuration(500)?.start()

        val latLng = if (isCustomCoordinate) LatLng(customLatitude, customLongitude) else LatLng(lastLatitude, lastLongitude)

        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder().target(latLng).tilt(0f).zoom(18f).build()))
        googleMap.uiSettings.isCompassEnabled = false
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.uiSettings.isMyLocationButtonEnabled = false

        this.googleMap = googleMap

        showLabel(GPSPreferences.isLabelOn())
        setSatellite(GPSPreferences.isSatelliteOn())

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
            val address = try {
                if (context == null) {
                    getString(R.string.error)
                } else if (!isNetworkAvailable(requireContext())) {
                    getString(R.string.internet_connection_alert)
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
                "${e.message}"
            } catch (e: NullPointerException) {
                "${e.message}\n${getString(R.string.no_address_found)}"
            } catch (e: IllegalArgumentException) {
                getString(R.string.invalid_coordinates)
            }

            withContext(Dispatchers.Main) {
                try {
                    this@GPS.address.text = address
                } catch (e: NullPointerException) {
                } catch (e: UninitializedPropertyAccessException) {
                }
            }
        }
    }

    private val mapMoved = object : Runnable {
        override fun run() {
            if (context == null) return
            isMapMoved = false
        }
    }

    private fun locationIconStatusUpdates() {
        if (getLocationStatus(requireContext())) {
            locationIndicator.setImageResource(R.drawable.ic_gps_not_fixed)
        } else {
            locationIndicator.setImageResource(R.drawable.ic_gps_off)
            locationIndicator.isClickable = false
        }
    }

    private fun moveMapCamera(latLng: LatLng, bearing: Float) {
        if (googleMap.isNull()) return
        if (isMapMoved) return

        val cameraPosition = googleMap?.cameraPosition?.tilt?.let {
            CameraPosition.builder().target(latLng).tilt(it).zoom(18f).bearing(bearing).build()
        }

        googleMap?.clear()
        googleMap?.addMarker(MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(marker)))
        googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 3000, null)
    }

    fun showLabel(value: Boolean) {
        if (!googleMap.isNull()) {
            googleMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    when (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                        Configuration.UI_MODE_NIGHT_YES -> {
                            if (value) {
                                R.raw.maps_dark_labelled
                            } else {
                                R.raw.maps_dark_no_label
                            }
                        }
                        Configuration.UI_MODE_NIGHT_NO -> {
                            if (value) {
                                R.raw.maps_light_labelled
                            } else {
                                R.raw.maps_no_label
                            }
                        }
                        else -> 0
                    }
            ))
        }

        GPSPreferences.setLabelMode(value)
    }

    fun setSatellite(value: Boolean) {
        if (!googleMap.isNull())
            googleMap?.mapType = if (value) {
                GoogleMap.MAP_TYPE_SATELLITE
            } else {
                GoogleMap.MAP_TYPE_NORMAL
            }
    }

    private fun checkGooglePlayServices(): Boolean {
        val availability = GoogleApiAvailability.getInstance()
        val resultCode = availability.isGooglePlayServicesAvailable(requireContext())
        if (resultCode != ConnectionResult.SUCCESS) {
            if (MainPreferences.getShowPlayServiceDialog()) {
                val playServiceIssue = PlayServiceIssue().newInstance()
                playServiceIssue.show(parentFragmentManager, "null")
            }
            return false
        }
        return true
    }

    private fun backPressed(value: Boolean) {
        /**
         * @see Clock.backPressed
         */
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
