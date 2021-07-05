package app.simple.positional.ui.panels

import android.content.*
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.positional.R
import app.simple.positional.activities.fragment.ScopedFragment
import app.simple.positional.callbacks.BottomSheetSlide
import app.simple.positional.decorations.trails.TrailMaps
import app.simple.positional.decorations.views.TrailToolbar
import app.simple.positional.decorations.views.TrailTools
import app.simple.positional.dialogs.trail.TrailMenu
import app.simple.positional.preferences.GPSPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.preferences.TrailPreferences
import app.simple.positional.util.NullSafety.isNotNull
import app.simple.positional.viewmodels.factory.TrailDataFactory
import app.simple.positional.viewmodels.viewmodel.TrailDataViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class Trail : ScopedFragment() {

    private var maps: TrailMaps? = null
    private lateinit var toolbar: TrailToolbar
    private lateinit var tools: TrailTools
    private val handler = Handler(Looper.getMainLooper())
    private var filter: IntentFilter = IntentFilter()
    private lateinit var locationBroadcastReceiver: BroadcastReceiver
    private lateinit var bottomSheetSlide: BottomSheetSlide
    private var location: Location? = null
    private var isFullScreen = false

    private lateinit var trailDataViewModel: TrailDataViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_trail, container, false)

        toolbar = view.findViewById(R.id.toolbar)
        tools = view.findViewById(R.id.trail_tools)
        bottomSheetSlide = requireActivity() as BottomSheetSlide

        val trailDataFactory = TrailDataFactory(TrailPreferences.getLastUsedTrail(), requireActivity().application)
        trailDataViewModel = ViewModelProvider(this, trailDataFactory).get(TrailDataViewModel::class.java)

        maps = view.findViewById(R.id.map_view)
        maps?.onCreate(savedInstanceState)
        maps?.resume()

        filter.addAction("location")
        filter.addAction("provider")

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent!!.action) {
                    "location" -> {
                        viewLifecycleOwner.lifecycleScope.launch {
                            withContext(Dispatchers.Default) {
                                location = intent.getParcelableExtra("location")
                                    ?: return@withContext

                                MainPreferences.setLastLatitude(location!!.latitude.toFloat())
                                MainPreferences.setLastLongitude(location!!.longitude.toFloat())

                                withContext(Dispatchers.Main) {
                                    maps?.location = location
                                    maps?.addMarker(LatLng(location!!.latitude, location!!.longitude))
                                }
                            }
                        }
                    }
                }
            }
        }

        tools.setTrailCallbacksListener(object : TrailTools.Companion.TrailCallbacks {
            override fun onLocation() {
                if (location.isNotNull()) {
                    maps?.moveMapCamera(LatLng(location!!.latitude, location!!.longitude),
                                        TrailPreferences.getMapZoom(),
                                        500)
                }
            }

            override fun onAdd(position: Int) {
                maps?.addPolyline(LatLng((25..30).random().toDouble(), (80..85).random().toDouble()),
                                  position)
            }

            override fun onRemove() {
                maps?.removePolyline()
            }

            override fun onWrapUnwrap() {
                maps?.wrapUnwrap()
            }
        })

        toolbar.onFlagClicked = {

        }

        toolbar.onMenuClicked = {
            TrailMenu.newInstance()
                .show(requireActivity().supportFragmentManager, "trail_menu")
        }

        maps?.onMapClicked = {
            setFullScreen(true)
        }

        trailDataViewModel.getTrailData().observe(viewLifecycleOwner, {
            maps?.addPolyline(it)
        })
    }

    override fun onResume() {
        super.onResume()
        maps?.resume()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(locationBroadcastReceiver, filter)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        maps?.lowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putFloat("translation", toolbar.translationY)
        outState.putBoolean("fullscreen", isFullScreen)
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
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        maps?.removeCallbacks { }
        maps?.destroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationBroadcastReceiver)
        handler.removeCallbacksAndMessages(null)
    }

    private fun setFullScreen(forBottomBar: Boolean) {
        if (isFullScreen) {
            toolbar.show()
        } else {
            toolbar.hide()
        }

        if (forBottomBar) {
            bottomSheetSlide.onMapClicked(fullScreen = isFullScreen)
        }
        isFullScreen = !isFullScreen
    }

    companion object {
        fun newInstance(): Trail {
            val args = Bundle()
            val fragment = Trail()
            fragment.arguments = args
            return fragment
        }
    }
}