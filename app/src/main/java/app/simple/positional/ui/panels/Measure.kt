package app.simple.positional.ui.panels

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedDispatcher
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.simple.positional.R
import app.simple.positional.adapters.bottombar.BottomBarItems
import app.simple.positional.decorations.measure.MeasureMaps
import app.simple.positional.decorations.measure.MeasureToolbar
import app.simple.positional.decorations.measure.MeasureToolbarCallbacks
import app.simple.positional.decorations.measure.MeasureTools
import app.simple.positional.decorations.measure.MeasureToolsCallbacks
import app.simple.positional.dialogs.measure.MeasureMenu.Companion.showMeasureMenu
import app.simple.positional.extensions.fragment.ScopedFragment
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.preferences.MeasurePreferences
import app.simple.positional.util.ConditionUtils.isNotNull
import app.simple.positional.util.LocationExtension
import app.simple.positional.util.LocationPrompt
import app.simple.positional.viewmodels.viewmodel.LocationViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Measure : ScopedFragment() {

    private lateinit var toolbar: MeasureToolbar
    private lateinit var tools: MeasureTools

    private var location: Location? = null
    private var backPress: OnBackPressedDispatcher? = null
    private var maps: MeasureMaps? = null
    private var locationViewModel: LocationViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_measure, container, false)

        toolbar = view.findViewById(R.id.toolbar)
        tools = view.findViewById(R.id.tools)
        maps = view.findViewById(R.id.map)

        backPress = requireActivity().onBackPressedDispatcher
        maps?.onCreate(savedInstanceState)
        locationViewModel = ViewModelProvider(requireActivity())[LocationViewModel::class.java]

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

            override fun onWrap(view: View?) {
                Log.d(TAG, "onWrap: ")
            }

            override fun onNewAdd(view: View?) {
                Log.d(TAG, "onNewAdd: ")
            }

            override fun onClearRecentMarker(view: View?) {
                Log.d(TAG, "onClearRecentMarker: ")
            }
        })
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

    companion object {
        fun newInstance(): Measure {
            val args = Bundle()
            val fragment = Measure()
            fragment.arguments = args
            return fragment
        }

        const val TAG = BottomBarItems.MEASURE
    }
}
