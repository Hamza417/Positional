package app.simple.positional.ui.panels

import android.content.*
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spanned
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.activities.fragment.ScopedFragment
import app.simple.positional.callbacks.BottomSheetSlide
import app.simple.positional.constants.LocationPins
import app.simple.positional.database.instances.LocationDatabase
import app.simple.positional.decorations.maps.*
import app.simple.positional.dialogs.app.CustomLocationParameters
import app.simple.positional.dialogs.app.ErrorDialog
import app.simple.positional.dialogs.gps.CoordinatesExpansion
import app.simple.positional.dialogs.gps.GPSMenu
import app.simple.positional.dialogs.gps.LocationExpansion
import app.simple.positional.dialogs.gps.MovementExpansion
import app.simple.positional.math.MathExtensions.round
import app.simple.positional.math.UnitConverter.toFeet
import app.simple.positional.math.UnitConverter.toKiloMetersPerHour
import app.simple.positional.math.UnitConverter.toKilometers
import app.simple.positional.math.UnitConverter.toMiles
import app.simple.positional.math.UnitConverter.toMilesPerHour
import app.simple.positional.model.Locations
import app.simple.positional.preferences.GPSPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.singleton.DistanceSingleton
import app.simple.positional.util.*
import app.simple.positional.util.ConditionUtils.isNotNull
import app.simple.positional.util.ConditionUtils.isNull
import app.simple.positional.util.Direction.getDirectionNameFromAzimuth
import app.simple.positional.util.HtmlHelper.fromHtml
import app.simple.positional.util.LocationExtension.getLocationStatus
import app.simple.positional.util.TextViewUtils.setTextAnimation
import app.simple.positional.viewmodels.viewmodel.LocationViewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.*
import java.io.IOException
import java.lang.Runnable
import java.util.*

class GPS : ScopedFragment() {

    private lateinit var expandUp: ImageView
    private lateinit var scrollView: NestedScrollView
    private lateinit var toolbar: MapToolbar
    private lateinit var tools: MapsTools
    private lateinit var bottomSheetSlide: BottomSheetSlide
    private lateinit var divider: View
    private lateinit var dim: View
    private lateinit var locationBox: LinearLayout
    private lateinit var movementBox: LinearLayout
    private lateinit var coordinatesBox: FrameLayout
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
    private lateinit var bottomSheetInfoPanel: BottomSheetBehavior<CoordinatorLayout>
    private var location: Location? = null
    private var backPress: OnBackPressedDispatcher? = null
    private lateinit var locationViewModel: LocationViewModel

    private var isMetric = true
    private var isFullScreen = false
    private var isCustomCoordinate = false
    private var customLatitude = 0.0
    private var customLongitude = 0.0
    private var lastLatitude = 0.0
    private var lastLongitude = 0.0
    private var peekHeight = 0
    private var distanceSingleton = DistanceSingleton
    private var maps: Maps? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view: View = inflater.inflate(R.layout.fragment_gps, container, false)

        toolbar = view.findViewById(R.id.map_toolbar)
        tools = view.findViewById(R.id.maps_tools)
        scrollView = view.findViewById(R.id.gps_list_scroll_view)
        scrollView.alpha = 0f
        divider = view.findViewById(R.id.gps_divider)
        dim = view.findViewById(R.id.gps_dim)
        copy = view.findViewById(R.id.gps_copy)
        save = view.findViewById(R.id.gps_save)
        movementReset = view.findViewById(R.id.movement_reset)
        expandUp = view.findViewById(R.id.expand_up_gps_sheet)
        bottomSheetInfoPanel = BottomSheetBehavior.from(view.findViewById(R.id.gps_info_bottom_sheet))

        locationBox = view.findViewById(R.id.gps_panel_location)
        movementBox = view.findViewById(R.id.gps_panel_movement)
        coordinatesBox = view.findViewById(R.id.gps_panel_coordinates)

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
        locationViewModel = ViewModelProvider(requireActivity()).get(LocationViewModel::class.java)

        maps = view.findViewById(R.id.map)
        maps?.onCreate(savedInstanceState)
        maps?.resume()

        if (requireActivity().intent.isNotNull()) {
            if (requireActivity().intent.action == "action_map_panel_full") {
                isFullScreen = false
                setFullScreen(true)
                requireActivity().intent.action = null
            }
        }

        isMetric = MainPreferences.getUnit()

        if (MainPreferences.isCustomCoordinate()) {
            isCustomCoordinate = true
            customLatitude = MainPreferences.getCoordinates()[0].toDouble()
            customLongitude = MainPreferences.getCoordinates()[1].toDouble()
        }

        lastLatitude = MainPreferences.getLastCoordinates()[0].toDouble()
        lastLongitude = MainPreferences.getLastCoordinates()[1].toDouble()

        bottomSheetSlide = requireActivity() as BottomSheetSlide
        backPress = requireActivity().onBackPressedDispatcher

        peekHeight = bottomSheetInfoPanel.peekHeight
        tools.locationIndicatorUpdate(false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLocationPin()

        if (isCustomCoordinate) {
            specifiedLocationTextView.isVisible = true
            divider.isVisible = true
            updateViews(customLatitude, customLongitude)
        }

        if (GPSPreferences.isUsingVolumeKeys()) {
            view.isFocusableInTouchMode = true
            view.requestFocus()
        }

        providerStatus.text = fromHtml("<b>${getString(R.string.gps_status)}</b> ${if (getLocationStatus(requireContext())) getString(R.string.gps_enabled) else getString(R.string.gps_disabled)}")

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

        locationViewModel.location.observe(viewLifecycleOwner, {
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.Default) {
                    location = it

                    if (!distanceSingleton.isInitialLocationSet!!) {
                        distanceSingleton.initialPointCoordinates =
                                LatLng(location!!.latitude, location!!.longitude)
                        distanceSingleton.distanceCoordinates =
                                LatLng(location!!.latitude, location!!.longitude)
                        distanceSingleton.isInitialLocationSet = true
                    }

                    MainPreferences.setLastLatitude(location!!.latitude.toFloat())
                    MainPreferences.setLastLongitude(location!!.longitude.toFloat())
                    val providerSource = fromHtml(
                            "<b>${getString(R.string.gps_source)}</b> ${
                                location!!.provider.uppercase(
                                        Locale.getDefault()
                                )
                            }"
                    )
                    val providerStatus = fromHtml(
                            "<b>${getString(R.string.gps_status)}</b> ${
                                if (getLocationStatus(requireContext())) getString(R.string.gps_enabled) else getString(
                                        R.string.gps_disabled
                                )
                            }"
                    )
                    val accuracy = if (isMetric) {
                        fromHtml(
                                "<b>${getString(R.string.gps_accuracy)}</b> ${
                                    round(
                                            location!!.accuracy.toDouble(),
                                            2
                                    )
                                } ${getString(R.string.meter)}"
                        )
                    } else {
                        fromHtml(
                                "<b>${getString(R.string.gps_accuracy)}</b> ${
                                    round(
                                            location!!.accuracy.toDouble().toFeet(),
                                            2
                                    )
                                } ${getString(R.string.feet)}"
                        )
                    }
                    val altitude = if (isMetric) {
                        fromHtml(
                                "<b>${getString(R.string.gps_altitude)}</b> ${
                                    round(
                                            location!!.altitude,
                                            2
                                    )
                                } ${getString(R.string.meter)}"
                        )
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
                        distanceSingleton.distanceCoordinates = LatLng(location!!.latitude, location!!.longitude)
                        direction = fromHtml("<b>${getString(R.string.gps_direction)}</b> ${getDirectionNameFromAzimuth(requireContext(), location!!.bearing.toDouble())}")
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
                        this@GPS.tools.locationIndicatorUpdate(true)
                        this@GPS.providerSource.text = providerSource
                        this@GPS.providerStatus.text = providerStatus
                        this@GPS.altitude.text = altitude
                        this@GPS.speed.text = speed
                        this@GPS.bearing.text = bearing
                        this@GPS.accuracy.text = accuracy
                        this@GPS.displacement.text = displacement
                        this@GPS.direction.text = direction

                        if (!isCustomCoordinate) {
                            updateViews(location!!.latitude, location!!.longitude)
                            maps?.setFirstLocation(location)
                            maps?.location = location
                            maps?.addMarker(LatLng(location!!.latitude, location!!.longitude))
                        }
                    }
                }
            }
        })

        locationViewModel.provider.observe(viewLifecycleOwner, {
            providerStatus.text = fromHtml(
                    "<b>${getString(R.string.gps_status)}</b> ${
                        if (getLocationStatus(requireContext())) getString(R.string.gps_enabled) else getString(
                                R.string.gps_disabled
                        )
                    }"
            )

            providerSource.text = fromHtml(
                    "<b>${getString(R.string.gps_source)}</b> $it"
            )

            tools.locationIconStatusUpdates()
        })

        bottomSheetInfoPanel.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
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
                expandUp.alpha = 1 - slideOffset
                expandUp.rotationX = -180 * slideOffset
                dim.alpha = slideOffset
                if (!isFullScreen) {
                    bottomSheetSlide.onBottomSheetSliding(slideOffset)
                }
            }
        })

        toolbar.setOnMapToolbarCallbacks(object : MapToolbar.MapToolbarCallbacks {
            override fun onMenuClicked(view: View) {
                GPSMenu().show(parentFragmentManager, "gps_menu")
            }

            override fun onCustomLocationClicked(view: View) {
                CustomLocationParameters.newInstance()
                        .show(parentFragmentManager, "location_params")
            }
        })

        tools.setOnToolsCallbacksListener(object : MapsToolsCallbacks {
            override fun onLocationClicked(view: View, longPressed: Boolean) {
                if(longPressed) {
                    maps?.resetCamera(18F)
                } else {
                    updateViews(customLatitude, customLongitude)
                    maps?.resetCamera(GPSPreferences.getMapZoom())
                }
            }
        })

        save.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                val isLocationSaved: Boolean
                val db = Room.databaseBuilder(requireContext(), LocationDatabase::class.java, "locations.db").fallbackToDestructiveMigration().build()
                val locations = Locations()

                if (MainPreferences.isCustomCoordinate()) {
                    isLocationSaved = false
                } else {
                    if (location.isNull()) {
                        isLocationSaved = false
                    } else {
                        locations.latitude = location?.latitude!!
                        locations.longitude = location?.longitude!!
                        locations.address = address.text.toString()
                        locations.date = System.currentTimeMillis()

                        db.locationDao()?.insertLocation(locations)
                        db.close()

                        isLocationSaved = true
                    }
                }

                withContext(Dispatchers.Main) {
                    handler.removeCallbacks(textAnimationRunnable)
                    if (MainPreferences.isCustomCoordinate()) {
                        infoText.setTextAnimation(getString(R.string.already_saved), 300)
                        handler.postDelayed(textAnimationRunnable, 3000)
                    } else {
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
        }

        copy.setOnClickListener {
            handler.removeCallbacks(textAnimationRunnable)
            val clipboard: ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            if (accuracy.text != "") {
                val stringBuilder = StringBuilder().apply {
                    append("${getString(R.string.gps_provider)}\n")
                    append("${providerStatus.text}\n")
                    append("${providerSource.text}\n\n")

                    append("${getString(R.string.gps_location)}\n")
                    append("${accuracy.text}\n")
                    append("${altitude.text}\n")
                    append("${bearing.text}\n\n")

                    append("${getString(R.string.gps_movement)}\n")
                    append("${displacement.text}\n")
                    append("${direction.text}\n")
                    append("${speed.text}\n")

                    if (isCustomCoordinate) {
                        append("\n${specifiedLocationTextView.text}\n")
                    }

                    append("\n${getString(R.string.gps_coordinates)}\n")
                    append("${latitude.text}\n")
                    append("${longitude.text}\n\n")

                    append("${getString(R.string.gps_address)}: ${address.text}")

                    if (BuildConfig.FLAVOR == "lite") {
                        append("\n\nInformation is copied using Positional\n")
                        append("Get the app from:\nhttps://play.google.com/store/apps/details?id=app.simple.positional")
                    }
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

        locationBox.setOnClickListener {
            LocationExpansion.newInstance().show(childFragmentManager, "location_expansion")
        }

        movementBox.setOnClickListener {
            MovementExpansion.newInstance().show(childFragmentManager, "movement_expansion")
        }

        coordinatesBox.setOnClickListener {
            CoordinatesExpansion.newInstance()
                    .show(childFragmentManager, "coordinates_expansion")
        }

        maps?.setOnMapsCallbackListener(object : MapsCallbacks {
            override fun onMapInitialized() {
                if (savedInstanceState.isNotNull()) {
                    maps?.setCamera(savedInstanceState!!.getParcelable("camera"))
                }
            }

            override fun onMapClicked(view: MapView?) {
                setFullScreen(true)
            }
        })

        view.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_VOLUME_UP -> {
                        maps?.zoomIn()
                    }
                    KeyEvent.KEYCODE_VOLUME_DOWN -> {
                        maps?.zoomOut()
                    }
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                        maps?.resetCamera(GPSPreferences.getMapZoom())
                    }
                    KeyEvent.KEYCODE_BACK -> {
                        requireActivity().onBackPressed()
                    }
                }
            }

            true
        }
    }

    private fun setFullScreen(forBottomBar: Boolean) {
        if (isFullScreen) {
            toolbar.show()
            bottomSheetInfoPanel.peekHeight = peekHeight
        } else {
            toolbar.hide()
            bottomSheetInfoPanel.peekHeight = 0
        }

        if (forBottomBar) {
            bottomSheetSlide.onMapClicked(fullScreen = isFullScreen)
        }
        isFullScreen = !isFullScreen
    }

    private val textAnimationRunnable: Runnable = Runnable {
        infoText.setTextAnimation(getString(R.string.gps_info), 300)
    }

    override fun onPause() {
        super.onPause()
        maps?.pause()
        if (backPress!!.hasEnabledCallbacks()) {
            backPressed(false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        maps?.removeCallbacks { }
        maps?.destroy()
        handler.removeCallbacks(textAnimationRunnable)
        infoText.clearAnimation()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onResume() {
        super.onResume()
        maps?.resume()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        maps?.lowMemory()
    }

    private fun updateViews(latitude_: Double, longitude_: Double) {
        getAddress(latitude_, longitude_)
        latitude.text = fromHtml("<b>${getString(R.string.gps_latitude)}</b> ${DMSConverter.latitudeAsDMS(latitude_, requireContext())}")
        longitude.text = fromHtml("<b>${getString(R.string.gps_longitude)}</b> ${DMSConverter.longitudeAsDMS(longitude_, requireContext())}")
    }

    private fun getAddress(latitude: Double, longitude: Double) {
        viewLifecycleOwner.lifecycleScope.launch {
            var address: String = getString(R.string.not_available)

            withContext(Dispatchers.IO) {
                runCatching {
                    address = try {
                        if (context == null) {
                            getString(R.string.error)
                        } else if (!isNetworkAvailable(requireContext())) {
                            getString(R.string.internet_connection_alert)
                        } else {
                            val addresses: List<Address>
                            val geocoder = Geocoder(context, Locale.getDefault())

                            addresses = geocoder.getFromLocation(latitude, longitude, 1)

                            if (addresses != null && addresses.isNotEmpty()) {
                                addresses[0].getAddressLine(0) //"$city, $state, $country, $postalCode, $knownName"
                            } else {
                                getString(R.string.not_available)
                            }
                        }
                    } catch (e: IOException) {
                        "${e.message}"
                    } catch (e: NullPointerException) {
                        "${e.message}\n${getString(R.string.no_address_found)}"
                    } catch (e: IllegalArgumentException) {
                        getString(R.string.invalid_coordinates)
                    }
                }
            }

            try {
                this@GPS.address.text = address
            } catch (ignored: NullPointerException) {
            } catch (ignored: UninitializedPropertyAccessException) {
            }
        }
    }

    private fun setLocationPin() {
        view?.findViewById<ImageView>(R.id.coordinates_icon)!!
                .setImageResource(LocationPins.locationsPins[GPSPreferences.getPinSkin()])
    }

    private fun checkGooglePlayServices(): Boolean {
        val availability = GoogleApiAvailability.getInstance()
        val resultCode = availability.isGooglePlayServicesAvailable(requireContext())
        if (resultCode != ConnectionResult.SUCCESS) {
            ErrorDialog.newInstance(getString(R.string.play_services_error))
                    .show(childFragmentManager, "error_dialog")
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
                if (bottomSheetInfoPanel.state == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetInfoPanel.state = BottomSheetBehavior.STATE_COLLAPSED
                }
                /**
                 * Remove this callback as soon as it's been called
                 * to prevent any further registering
                 */
                remove()
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putFloat("translation", toolbar.translationY)
        outState.putBoolean("fullscreen", isFullScreen)
        outState.putParcelable("camera", maps?.getCamera())
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        if (savedInstanceState.isNotNull()) {
            isFullScreen = !savedInstanceState!!.getBoolean("fullscreen")
            setFullScreen(false)
        }
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            GPSPreferences.useVolumeKeys -> {
                view?.isFocusableInTouchMode = GPSPreferences.isUsingVolumeKeys()
                if (GPSPreferences.isUsingVolumeKeys()) {
                    view?.requestFocus()
                } else {
                    view?.clearFocus()
                }
            }
            GPSPreferences.pinSkin -> {
                setLocationPin()
            }
        }
    }

    companion object {
        fun newInstance(): GPS {
            val args = Bundle()
            val fragment = GPS()
            fragment.arguments = args
            return fragment
        }
    }
}
