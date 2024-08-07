package app.simple.positional.ui.panels

import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedDispatcher
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import app.simple.positional.R
import app.simple.positional.activities.subacitivity.MeasuresActivity
import app.simple.positional.callbacks.BottomSheetSlide
import app.simple.positional.decorations.corners.DynamicCornerConstraintLayout
import app.simple.positional.decorations.measure.MeasureMaps
import app.simple.positional.decorations.measure.MeasureToolbar
import app.simple.positional.decorations.measure.MeasureToolbarCallbacks
import app.simple.positional.decorations.measure.MeasureTools
import app.simple.positional.decorations.measure.MeasureToolsCallbacks
import app.simple.positional.dialogs.measure.MeasureAdd
import app.simple.positional.dialogs.measure.MeasureAdd.Companion.showMeasureAdd
import app.simple.positional.dialogs.measure.MeasureMenu.Companion.showMeasureMenu
import app.simple.positional.extensions.fragment.ScopedFragment
import app.simple.positional.extensions.maps.MapsCallbacks
import app.simple.positional.math.MathExtensions.round
import app.simple.positional.math.UnitConverter.toFeet
import app.simple.positional.math.UnitConverter.toKilometers
import app.simple.positional.math.UnitConverter.toMiles
import app.simple.positional.model.MeasurePoint
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.preferences.MeasurePreferences
import app.simple.positional.singleton.FloatingButtonStateCommunicator
import app.simple.positional.util.ConditionUtils.invert
import app.simple.positional.util.ConditionUtils.isNotNull
import app.simple.positional.util.LocationExtension
import app.simple.positional.util.LocationPrompt
import app.simple.positional.util.ParcelUtils.parcelable
import app.simple.positional.util.ViewUtils.gone
import app.simple.positional.util.ViewUtils.visible
import app.simple.positional.viewmodels.viewmodel.LocationViewModel
import app.simple.positional.viewmodels.viewmodel.MeasureViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import app.simple.positional.model.Measure as Measure_Model

class Measure : ScopedFragment(), FloatingButtonStateCommunicator.FloatingButtonStateCallbacks {

    private lateinit var toolbar: MeasureToolbar
    private lateinit var tools: MeasureTools
    private lateinit var crossHair: ImageView
    private lateinit var bottomContainer: DynamicCornerConstraintLayout
    private lateinit var name: TextView
    private lateinit var currentDistance: TextView
    private lateinit var totalDistance: TextView
    private lateinit var totalPoints: TextView

    private var location: Location? = null
    private var backPress: OnBackPressedDispatcher? = null
    private var maps: MeasureMaps? = null
    private var bottomSheetSlide: BottomSheetSlide? = null

    private lateinit var locationViewModel: LocationViewModel
    private lateinit var measureViewModel: MeasureViewModel

    private var isFullScreen = false
    private var x = 0F
    private var y = 0F

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_measure, container, false)

        toolbar = view.findViewById(R.id.toolbar)
        tools = view.findViewById(R.id.tools)
        maps = view.findViewById(R.id.map)
        crossHair = view.findViewById(R.id.cross_hair)
        bottomContainer = view.findViewById(R.id.bottom_container)
        name = view.findViewById(R.id.name)
        currentDistance = view.findViewById(R.id.current_distance)
        totalDistance = view.findViewById(R.id.total_distance)
        totalPoints = view.findViewById(R.id.total_points)

        backPress = requireActivity().onBackPressedDispatcher
        maps?.onCreate(savedInstanceState)
        bottomSheetSlide = requireActivity() as BottomSheetSlide

        locationViewModel = ViewModelProvider(requireActivity())[LocationViewModel::class.java]
        measureViewModel = ViewModelProvider(requireActivity())[MeasureViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.setMeasureToolbarCallbacks(object : MeasureToolbarCallbacks {
            override fun onAdd(view: View?) {
                showMeasureAdd().setOnMeasureAddCallbacks(object :
                    MeasureAdd.Companion.MeasureAddCallbacks {
                    override fun onSave(name: String, note: String) {
                        measureViewModel.addMeasure(name, note)
                    }
                })
            }

            override fun onMeasures(view: View?) {
                startActivity(Intent(requireContext(), MeasuresActivity::class.java))
            }

            override fun onMenu(view: View?) {
                showMeasureMenu()
            }
        })

        tools.setMeasureToolsCallbacks(object : MeasureToolsCallbacks {
            override fun onLocation(view: View?, reset: Boolean) {
                if (LocationExtension.getLocationStatus(requireContext())) {
                    if (location.isNotNull()) {
                        if (reset) {
                            maps?.resetCamera(18F)
                        } else {
                            maps?.moveMapCamera(LatLng(location!!.latitude, location!!.longitude),
                                MeasurePreferences.getMapZoom(),
                                MeasurePreferences.getMapTilt())
                        }
                    }
                } else {
                    LocationPrompt.displayLocationSettingsRequest(requireActivity())
                }
            }

            override fun onNewAdd(view: View?) {
                maps?.addPolyline()
            }

            override fun onClearRecentMarker(view: View?) {
                maps?.removeRecentPolyline()
            }
        })

        maps?.setOnMapsCallbackListener(object : MapsCallbacks {
            override fun onMapInitialized() {
                if (savedInstanceState.isNotNull()) {
                    maps?.setCamera(savedInstanceState!!.parcelable(CAMERA))
                }

                locationViewModel.getLocation().observe(viewLifecycleOwner) { location ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        withContext(Dispatchers.Default) {
                            this@Measure.location = location

                            withContext(Dispatchers.Main) {
                                maps?.setFirstLocation(LatLng(location.latitude, location.longitude))
                                maps?.location = location
                                maps?.addMarker(LatLng(location.latitude, location.longitude))
                                tools.locationIndicatorUpdate(true)
                            }
                        }
                    }
                }

                measureViewModel.getMeasure().observe(viewLifecycleOwner) { measure ->
                    if (MeasurePreferences.isMeasureSelected() && measure.isNotNull()) {
                        crossHair.visible(animate = true)
                        maps?.createMeasurePolylines(measure)
                        name.text = measure.name
                        setTotalPoints(measure)
                        setTotalDistance(measure)
                        setCurrentDistance(null)
                        tools.measureMode()
                        bottomContainer.visible(true)
                    } else {
                        clearMeasure()
                        tools.noMeasureMode()
                    }
                }
            }

            override fun onMapClicked() {
                if (isLandscapeOrientation.invert()) {
                    setFullScreen()
                }
            }

            override fun onLineAdded(measurePoint: MeasurePoint) {
                measureViewModel.addMeasurePoint(measurePoint) {
                    requireActivity().runOnUiThread {
                        setTotalPoints(it)
                        setTotalDistance(it)
                        setCurrentDistance(measurePoint.latLng)
                    }
                }
            }

            override fun onLineDeleted(measurePoint: MeasurePoint?) {
                measureViewModel.removeMeasurePoint(measurePoint) {
                    requireActivity().runOnUiThread {
                        setTotalPoints(it)
                        setTotalDistance(it)
                    }
                }
            }

            override fun onCameraDistance(latLng: LatLng?) {
                setCurrentDistance(latLng!!)
            }
        })
    }

    private fun clearMeasure() {
        maps?.clear()
        bottomContainer.gone(true)
        crossHair.gone(true)
    }

    private fun setTotalPoints(measure: Measure_Model) {
        totalPoints.text = buildString {
            append(getString(R.string.total))
            append(" ")
            append(measure.measurePoints?.size.toString())
        }
    }

    private fun setTotalDistance(measure: Measure_Model) {
        totalDistance.text = buildString {
            append(getString(R.string.gps_displacement))
            append(" ")

            val distance: Double = (measure.measurePoints?.map { LatLng(it.latitude, it.longitude) }?.toTypedArray()?.let {
                LocationExtension.measureDisplacement(it)
            } ?: 0.0).toDouble()

            if (MainPreferences.isMetric()) {
                if (distance < 1000) {
                    append(round(distance, 2))
                    append(" ")
                    append(getString(R.string.meter))
                } else {
                    append(round(distance.toKilometers(), 2))
                    append(" ")
                    append(getString(R.string.kilometer))
                }
            } else {
                if (distance < 1609) {
                    append(distance.toFeet())
                    append(" ")
                    append(getString(R.string.feet))
                } else {
                    append(round(distance.toMiles(), 2))
                    append(" ")
                    append(getString(R.string.miles))
                }
            }
        }
    }

    private fun setCurrentDistance(latLng: LatLng?) {
        runCatching {
            if (maps?.getMeasurePoints()?.isNotEmpty() == true) {
                currentDistance.visible(false)
                val points: Array<LatLng> = arrayOf(maps?.getMeasurePoints()?.lastOrNull()!!.latLng, latLng!!)
                val distance = LocationExtension.measureDisplacement(points).toDouble()

                currentDistance.text = buildString {
                    if (MainPreferences.isMetric()) {
                        if (distance < 1000) {
                            append(round(distance, 2))
                            append(" ")
                            append(getString(R.string.meter))
                        } else {
                            append(round(distance.toKilometers(), 2))
                            append(" ")
                            append(getString(R.string.kilometer))
                        }
                    } else {
                        if (distance < 1609) {
                            append(round(distance.toFeet(), 2))
                            append(" ")
                            append(getString(R.string.feet))
                        } else {
                            append(round(distance.toMiles(), 2))
                            append(" ")
                            append(getString(R.string.miles))
                        }
                    }
                }
            } else {
                currentDistance.gone()
            }
        }
    }

    private fun setFullScreen() {
        if (isFullScreen) {
            toolbar.show()
        } else {
            toolbar.hide()
        }

        bottomSheetSlide?.onMapClicked(isFullScreen)
        isFullScreen = !isFullScreen
    }

    override fun onResume() {
        super.onResume()
        maps?.onResume()
        FloatingButtonStateCommunicator.addFloatingButtonStateCallbacks(this)
    }

    override fun onPause() {
        super.onPause()
        maps?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        maps?.onDestroy()
        FloatingButtonStateCommunicator.removeFloatingButtonStateCallbacks(this)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        maps?.onLowMemory()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            MeasurePreferences.LAST_SELECTED_MEASURE -> {
                if (MeasurePreferences.isMeasureSelected().invert()) {
                    clearMeasure()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putFloat(TRANSLATION, toolbar.translationY)
        outState.putBoolean(FULL_SCREEN, isFullScreen)
        outState.putParcelable(CAMERA, maps?.getCamera())
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            isFullScreen = it.getBoolean(FULL_SCREEN)
            if (isLandscapeOrientation) {
                setFullScreen()
            } else {
                setFullScreen()
            }
        }

        super.onViewStateRestored(savedInstanceState)
    }

    override fun onFloatingButtonStateChange(size: Int) {
        TransitionManager.beginDelayedTransition(bottomContainer)
        val marginLayoutParams = bottomContainer.layoutParams as FrameLayout.LayoutParams
        marginLayoutParams.rightMargin = size
        bottomContainer.layoutParams = marginLayoutParams
        bottomContainer.requestLayout()
    }

    companion object {
        fun newInstance(): Measure {
            val args = Bundle()
            val fragment = Measure()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "Measure"

        private const val CAMERA = "camera"
        private const val FULL_SCREEN = "fullscreen"
        private const val TRANSLATION = "translation"
    }
}
