package app.simple.positional.ui.panels

import android.content.*
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.text.toSpannable
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import androidx.transition.TransitionInflater
import androidx.transition.TransitionManager
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.callbacks.BottomSheetSlide
import app.simple.positional.constants.LocationPins
import app.simple.positional.database.instances.LocationDatabase
import app.simple.positional.decorations.maps.*
import app.simple.positional.dialogs.app.ErrorDialog
import app.simple.positional.dialogs.app.LocationParameters
import app.simple.positional.dialogs.gps.CoordinatesExpansion
import app.simple.positional.dialogs.gps.GPSMenu
import app.simple.positional.dialogs.gps.LocationExpansion
import app.simple.positional.dialogs.gps.MovementExpansion
import app.simple.positional.extensions.fragment.ScopedFragment
import app.simple.positional.math.MathExtensions.round
import app.simple.positional.math.UnitConverter.toFeet
import app.simple.positional.math.UnitConverter.toKiloMetersPerHour
import app.simple.positional.math.UnitConverter.toMilesPerHour
import app.simple.positional.model.Locations
import app.simple.positional.popups.miscellaneous.DeletePopupMenu
import app.simple.positional.preferences.GPSPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.*
import app.simple.positional.util.ConditionUtils.isNotNull
import app.simple.positional.util.ConditionUtils.isNull
import app.simple.positional.util.Direction.getDirectionNameFromAzimuth
import app.simple.positional.util.HtmlHelper.fromHtml
import app.simple.positional.util.LocationExtension.getLocationStatus
import app.simple.positional.util.TextViewUtils.setTextAnimation
import app.simple.positional.util.ViewUtils.gone
import app.simple.positional.util.ViewUtils.visible
import app.simple.positional.viewmodels.viewmodel.LocationViewModel
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.*
import java.io.IOException
import java.util.*

class GPS : ScopedFragment() {

    private lateinit var expandUp: ImageView
    private lateinit var crossHair: ImageView
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
    private lateinit var accuracy: TextView
    private lateinit var address: TextView
    private lateinit var latitude: TextView
    private lateinit var longitude: TextView
    private lateinit var providerStatus: TextView
    private lateinit var providerSource: TextView
    private lateinit var altitude: TextView
    private lateinit var bearing: TextView
    private lateinit var latency: TextView
    private lateinit var targetDisplacement: TextView
    private lateinit var targetDirection: TextView
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
    private var isCompassRotation = false
    private var customLatitude = 0.0
    private var customLongitude = 0.0
    private var lastLatitude = 0.0
    private var lastLongitude = 0.0
    private var peekHeight = 0

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
        crossHair = view.findViewById(R.id.cross_hair)
        expandUp = view.findViewById(R.id.expand_up_gps_sheet)
        bottomSheetInfoPanel =
            BottomSheetBehavior.from(view.findViewById(R.id.gps_info_bottom_sheet))

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
        latency = view.findViewById(R.id.gps_time_taken)
        targetDisplacement = view.findViewById(R.id.gps_target_displacement)
        targetDirection = view.findViewById(R.id.gps_target_direction)
        bearing = view.findViewById(R.id.gps_bearing)
        direction = view.findViewById(R.id.gps_direction)
        speed = view.findViewById(R.id.gps_speed)
        specifiedLocationTextView = view.findViewById(R.id.specified_location_notice_gps)
        infoText = view.findViewById(R.id.gps_info_text)

        handler = Handler(Looper.getMainLooper())
        locationViewModel = ViewModelProvider(requireActivity()).get(LocationViewModel::class.java)

        maps = view.findViewById(R.id.map)

        MapsInitializer.initialize(requireContext(), MapsInitializer.Renderer.LATEST, null)

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

        isCompassRotation = GPSPreferences.isCompassRotation()

        lastLatitude = MainPreferences.getLastCoordinates()[0].toDouble()
        lastLongitude = MainPreferences.getLastCoordinates()[1].toDouble()

        bottomSheetSlide = requireActivity() as BottomSheetSlide
        backPress = requireActivity().onBackPressedDispatcher

        peekHeight = bottomSheetInfoPanel.peekHeight
        tools.locationIndicatorUpdate(false)

        updateToolsGravity(view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setLocationPin()
        targetMode()

        if (isCustomCoordinate) {
            specifiedLocationTextView.isVisible = true
            divider.isVisible = true
            save.gone()
            updateCoordinates(customLatitude, customLongitude)
            getAddress(LatLng(customLatitude, customLongitude))
        }

        if (GPSPreferences.isUsingVolumeKeys()) {
            view.isFocusableInTouchMode = true
            view.requestFocus()
        }

        providerStatus.text =
            fromHtml("<b>${getString(R.string.gps_status)}</b> ${if (getLocationStatus(requireContext())) getString(R.string.gps_enabled) else getString(R.string.gps_disabled)}")

        if (!LocationPrompt.checkGooglePlayServices(requireContext())) {
            ErrorDialog.newInstance(getString(R.string.play_services_error))
                .show(childFragmentManager, "error_dialog")
        }

        locationViewModel.location.observe(viewLifecycleOwner, {
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.Default) {
                    location = it

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
                        fromHtml("<b>${getString(R.string.gps_speed)}</b> ${
                            round(location!!.speed.toDouble().toKiloMetersPerHour(), 2)
                        } ${getString(R.string.kilometer_hour)}")
                    } else {
                        fromHtml("<b>${getString(R.string.gps_speed)}</b> ${
                            round(location!!.speed.toDouble().toKiloMetersPerHour()
                                .toMilesPerHour(), 2)
                        } ${getString(R.string.miles_hour)}")
                    }

                    val bearing =
                        fromHtml("<b>${getString(R.string.gps_bearing)}</b> ${location!!.bearing}°")

                    val direction: Spanned = if (location!!.speed > 0f) {
                        fromHtml("<b>${getString(R.string.gps_direction)}</b> ${getDirectionNameFromAzimuth(requireContext(), location!!.bearing.toDouble())}")
                    } else {
                        fromHtml("<b>${getString(R.string.gps_direction)}</b> N/A")
                    }

                    withContext(Dispatchers.Main) {
                        this@GPS.tools.locationIndicatorUpdate(true)
                        this@GPS.providerSource.text = providerSource
                        this@GPS.providerStatus.text = providerStatus
                        this@GPS.altitude.text = altitude
                        this@GPS.speed.text = speed
                        this@GPS.bearing.text = bearing
                        this@GPS.accuracy.text = accuracy
                        this@GPS.direction.text = direction

                        if (!isCustomCoordinate) {
                            maps?.setFirstLocation(location)
                            maps?.location = location
                            maps?.addMarker(LatLng(location!!.latitude, location!!.longitude))
                            getAddress(LatLng(location!!.latitude, location!!.longitude))
                        }
                    }
                }
            }
        })

        locationViewModel.dms.observe(viewLifecycleOwner, {
            if (isCustomCoordinate) return@observe
            latitude.text = fromHtml("<b>${getString(R.string.gps_latitude)}</b> " + it.first)
            longitude.text = fromHtml("<b>${getString(R.string.gps_longitude)}</b> " + it.second)
        })

        locationViewModel.latency.observe(viewLifecycleOwner, {
            val str: Spannable = fromHtml("<b>${getString(R.string.gps_latency)}</b> " +
                    "${it.first} " +
                    if (it.second) getString(R.string.seconds) else getString(R.string.milliseconds)).toSpannable()

            if (it.second) {
                if (it.first.toDouble() > 5.0) {
                    str.setSpan(ForegroundColorSpan(Color.RED), getString(R.string.gps_latency).length, str.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }

            latency.text = str
        })

        locationViewModel.provider.observe(viewLifecycleOwner, {
            providerStatus.text = fromHtml(
                    "<b>${getString(R.string.gps_status)}</b> ${
                        if (getLocationStatus(requireContext())) {
                            getString(R.string.gps_enabled)
                        } else {
                            getString(R.string.gps_disabled)
                        }
                    }"
            )

            providerSource.text = fromHtml(
                    "<b>${getString(R.string.gps_source)}</b> $it"
            )

            tools.locationIconStatusUpdates()
            maps?.clearMarkers()
        })

        locationViewModel.targetDisplacement.observe(viewLifecycleOwner, {
            targetDisplacement.text = it
        })

        bottomSheetInfoPanel.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        maps?.unregister()
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        backPressed(true)
                        maps?.unregister()
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        backPressed(false)
                        if (backPress!!.hasEnabledCallbacks()) {
                            backPress?.onBackPressed()
                        }
                        maps?.registerWithRunnable()
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        /* no-op */
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        /* no-op */
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                        /* no-op */
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
                LocationParameters.newInstance()
                    .show(parentFragmentManager, "location_params")
            }
        })

        tools.setOnToolsCallbacksListener(object : MapsToolsCallbacks {
            override fun onLocationClicked(view: View, longPressed: Boolean) {
                if (getLocationStatus(requireContext())) {
                    if (longPressed) {
                        maps?.resetCamera(18F)
                    } else {
                        maps?.resetCamera(GPSPreferences.getMapZoom())
                    }
                } else {
                    LocationPrompt.displayLocationSettingsRequest(requireActivity())
                }
            }

            override fun onTargetAdd() {
                maps?.setTargetMarker()
            }

            override fun removeTarget(view: View) {
                DeletePopupMenu(view).setOnPopupCallbacksListener(object : DeletePopupMenu.Companion.PopupDeleteCallbacks {
                    override fun delete() {
                        GPSPreferences.setTargetMarker(false)
                    }
                })
            }
        })

        save.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                val isLocationSaved: Boolean
                val db =
                    Room.databaseBuilder(requireContext(), LocationDatabase::class.java, "locations.db")
                        .fallbackToDestructiveMigration().build()
                val locations = Locations()

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
            val clipboard: ClipboardManager =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

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

                    if (isCompassRotation && GPSPreferences.isMapAutoCenter()) {
                        handler.postDelayed(compassMapCamera, 6000L)
                    }
                }
            }

            override fun onMapClicked(view: MapView?) {
                setFullScreen(true)
            }

            override fun onTargetUpdated(target: LatLng?, current: LatLng?) {
                locationViewModel.targetData(target!!, current!!)
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

        maps?.onTouch = { event, _ ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    maps?.unregister()
                    handler.removeCallbacks(compassMapCamera)
                }
                MotionEvent.ACTION_UP -> {
                    if (bottomSheetInfoPanel.state != BottomSheetBehavior.STATE_EXPANDED) {
                        maps?.registerWithRunnable()

                        if (isCompassRotation && GPSPreferences.isMapAutoCenter()) {
                            handler.postDelayed(compassMapCamera, 6000L)
                        }
                    }
                }
            }
        }
    }

    private fun updateCoordinates(latitude_: Double, longitude_: Double) {
        latitude.text =
            fromHtml("<b>${getString(R.string.gps_latitude)}</b> ${DMSConverter.latitudeAsDMS(latitude_, requireContext())}")
        longitude.text =
            fromHtml("<b>${getString(R.string.gps_longitude)}</b> ${DMSConverter.longitudeAsDMS(longitude_, requireContext())}")
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

    private fun getAddress(latLng: LatLng) {
        lifecycleScope.launch(Dispatchers.Default) {
            var address: String = getString(R.string.not_available)

            runCatching {
                address = try {
                    if (!isNetworkAvailable(requireContext())) {
                        getString(R.string.internet_connection_alert)
                    } else {
                        val geocoder = Geocoder(requireContext(), Locale.getDefault())

                        with(geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)) {
                            if (this != null && this.isNotEmpty()) {
                                this[0].getAddressLine(0) //"$city, $state, $country, $postalCode, $knownName"
                            } else {
                                getString(R.string.not_available)
                            }
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

            withContext(Dispatchers.Main) {
                this@GPS.address.text = address
            }
        }
    }

    private val compassMapCamera = object : Runnable {
        override fun run() {
            if (isCompassRotation) {
                maps?.resetCamera(GPSPreferences.getMapZoom())
            }

            handler.postDelayed(this, 6000L)
        }
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
        handler.removeCallbacks(compassMapCamera)
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

    private fun setLocationPin() {
        view?.findViewById<ImageView>(R.id.coordinates_icon)!!
            .setImageResource(LocationPins.locationsPins[GPSPreferences.getPinSkin()])
    }

    private fun targetMode() {
        if (GPSPreferences.isTargetMarkerMode()) {
            crossHair.visible(true)
        } else {
            crossHair.gone(true)
        }
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

    private fun updateToolsGravity(view: View) {
        TransitionManager.beginDelayedTransition(
                view as ViewGroup,
                TransitionInflater.from(requireContext())
                    .inflateTransition(R.transition.tools_transition))

        val params = CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

        params.apply {
            gravity = if (GPSPreferences.isToolsGravityLeft()) {
                Gravity.START or Gravity.CENTER_VERTICAL
            } else {
                Gravity.END or Gravity.CENTER_VERTICAL
            }

            marginStart = resources.getDimensionPixelSize(R.dimen.trail_tools_margin)
            marginEnd = resources.getDimensionPixelSize(R.dimen.trail_tools_margin)
        }

        tools.layoutParams = params
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
            GPSPreferences.compassRotation,
            GPSPreferences.mapAutoCenter -> {
                isCompassRotation = GPSPreferences.isCompassRotation()

                if (isCompassRotation) {
                    if (GPSPreferences.isMapAutoCenter()) {
                        handler.postDelayed(compassMapCamera, 6000L)
                    } else {
                        handler.removeCallbacks(compassMapCamera)
                    }
                }
            }
            GPSPreferences.toolsGravity -> {
                updateToolsGravity(requireView())
            }
            GPSPreferences.isTargetMarkerMode -> {
                targetMode()
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
