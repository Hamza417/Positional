package app.simple.positional.ui.panels

import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedDispatcher
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import app.simple.positional.R
import app.simple.positional.adapters.bottombar.BottomBarItems
import app.simple.positional.callbacks.BottomSheetSlide
import app.simple.positional.decorations.measure.MeasureMaps
import app.simple.positional.decorations.measure.MeasureToolbar
import app.simple.positional.decorations.measure.MeasureToolbarCallbacks
import app.simple.positional.decorations.measure.MeasureTools
import app.simple.positional.decorations.measure.MeasureToolsCallbacks
import app.simple.positional.dialogs.measure.MeasureMenu.Companion.showMeasureMenu
import app.simple.positional.extensions.fragment.ScopedFragment
import app.simple.positional.extensions.maps.MapsCallbacks
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.preferences.MeasurePreferences
import app.simple.positional.util.ConditionUtils.invert
import app.simple.positional.util.ConditionUtils.isNotNull
import app.simple.positional.util.LocationExtension
import app.simple.positional.util.LocationPrompt
import app.simple.positional.util.ParcelUtils.parcelable
import app.simple.positional.viewmodels.viewmodel.LocationViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Measure : ScopedFragment() {

    private lateinit var toolbar: MeasureToolbar
    private lateinit var tools: MeasureTools
    private lateinit var recyclerView: RecyclerView
    private var bottomSheetPanel: BottomSheetBehavior<CoordinatorLayout>? = null
    private lateinit var bottomSheetSlide: BottomSheetSlide
    private lateinit var expandUp: ImageView
    private lateinit var art: ImageView
    private lateinit var dim: View

    private var location: Location? = null
    private var backPress: OnBackPressedDispatcher? = null
    private var maps: MeasureMaps? = null
    private var locationViewModel: LocationViewModel? = null

    private var isFullScreen = false
    private var peekHeight = 0
    private var x = 0F
    private var y = 0F

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_measure, container, false)

        toolbar = view.findViewById(R.id.toolbar)
        tools = view.findViewById(R.id.tools)
        maps = view.findViewById(R.id.map)
        recyclerView = view.findViewById(R.id.recycler_view)
        expandUp = view.findViewById(R.id.expand_up)
        art = view.findViewById(R.id.art)
        dim = view.findViewById(R.id.dim)

        kotlin.runCatching {
            bottomSheetPanel = BottomSheetBehavior.from(view.findViewById(R.id.trail_bottom_sheet))
        }

        bottomSheetSlide = requireActivity() as BottomSheetSlide
        backPress = requireActivity().onBackPressedDispatcher
        maps?.onCreate(savedInstanceState)
        locationViewModel = ViewModelProvider(requireActivity())[LocationViewModel::class.java]

        peekHeight = bottomSheetPanel?.peekHeight ?: 0

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.setMeasureToolbarCallbacks(object : MeasureToolbarCallbacks {
            override fun onAdd(view: View?) {
                Log.d(TAG, "onAdd: ")
            }

            override fun onMeasures(view: View?) {
                Log.d(TAG, "onMeasures: ")
            }

            override fun onMenu(view: View?) {
                showMeasureMenu()
            }
        })

        locationViewModel?.getLocation()?.observe(viewLifecycleOwner) { location ->
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.Default) {
                    this@Measure.location = location

                    MainPreferences.setLastLatitude(location!!.latitude.toFloat())
                    MainPreferences.setLastLongitude(location.longitude.toFloat())

                    withContext(Dispatchers.Main) {
                        maps?.setFirstLocation(location)
                        maps?.location = location
                        maps?.addMarker(LatLng(location.latitude, location.longitude))
                        tools.locationIndicatorUpdate(true)
                    }
                }
            }
        }

        tools.setMeasureToolsCallbacks(object : MeasureToolsCallbacks {
            override fun onLocation(view: View?, reset: Boolean) {
                if (LocationExtension.getLocationStatus(requireContext())) {
                    if (location.isNotNull()) {
                        if (reset) {
                            maps?.resetCamera(18F)
                        } else {
                            maps?.moveMapCamera(LatLng(location!!.latitude, location!!.longitude),
                                MeasurePreferences.getMapZoom(),
                                MeasurePreferences.getMapTilt(),
                                1000)
                        }
                    }
                } else {
                    LocationPrompt.displayLocationSettingsRequest(requireActivity())
                }
            }

            override fun onNewAdd(view: View?) {
                Log.d(TAG, "onNewAdd: ")
            }

            override fun onClearRecentMarker(view: View?) {
                Log.d(TAG, "onClearRecentMarker: ")
            }
        })

        maps?.setOnMapsCallbackListener(object : MapsCallbacks {
            override fun onMapInitialized() {
                if (savedInstanceState.isNotNull()) {
                    maps?.setCamera(savedInstanceState!!.parcelable(CAMERA))
                }
            }

            override fun onMapClicked() {
                if (isLandscapeOrientation.invert()) {
                    setFullScreen()
                }
            }
        })
    }

    private fun setFullScreen() {
        if (isFullScreen) {
            toolbar.show()
            bottomSheetPanel?.peekHeight = peekHeight
        } else {
            toolbar.hide()
            bottomSheetPanel?.peekHeight = 0
        }

        if (isLandscapeOrientation) {
            toolbar.show()
            bottomSheetSlide.onMapClicked(fullScreen = true)
        } else {
            bottomSheetSlide.onMapClicked(fullScreen = isFullScreen)
        }

        isFullScreen = !isFullScreen
    }

    override fun onResume() {
        super.onResume()
        maps?.onResume()
    }

    override fun onPause() {
        super.onPause()
        maps?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        maps?.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        maps?.onLowMemory()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putFloat(TRANSLATION, toolbar.translationY)
        outState.putBoolean(FULL_SCREEN, isFullScreen)
        outState.putParcelable(CAMERA, maps?.getCamera())
        outState.putInt(BOTTOM_SHEET_STATE, bottomSheetPanel?.state ?: 4)
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
            bottomSheetPanel?.state = it.getInt(BOTTOM_SHEET_STATE)
        }

        super.onViewStateRestored(savedInstanceState)
    }

    companion object {
        fun newInstance(): Measure {
            val args = Bundle()
            val fragment = Measure()
            fragment.arguments = args
            return fragment
        }

        const val TAG = BottomBarItems.MEASURE

        private const val CAMERA = "camera"
        private const val FULL_SCREEN = "fullscreen"
        private const val TRANSLATION = "translation"
        private const val BOTTOM_SHEET_STATE = "bottom_sheet_state"
    }
}
