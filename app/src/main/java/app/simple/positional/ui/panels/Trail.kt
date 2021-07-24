package app.simple.positional.ui.panels

import android.content.*
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.transition.TransitionInflater
import androidx.transition.TransitionManager
import app.simple.positional.R
import app.simple.positional.activities.fragment.ScopedFragment
import app.simple.positional.activities.subactivity.TrailDataActivity
import app.simple.positional.activities.subactivity.TrailsViewerActivity
import app.simple.positional.callbacks.BottomSheetSlide
import app.simple.positional.decorations.trails.TrailMapCallbacks
import app.simple.positional.decorations.trails.TrailMaps
import app.simple.positional.decorations.trails.TrailToolbar
import app.simple.positional.decorations.trails.TrailTools
import app.simple.positional.dialogs.trail.AddMarker
import app.simple.positional.dialogs.trail.AddTrail
import app.simple.positional.dialogs.trail.TrailMenu
import app.simple.positional.model.TrailData
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.preferences.TrailPreferences
import app.simple.positional.util.ConditionUtils.isNotNull
import app.simple.positional.util.ConditionUtils.isZero
import app.simple.positional.viewmodels.factory.TrailDataFactory
import app.simple.positional.viewmodels.viewmodel.TrailDataViewModel
import app.simple.positional.viewmodels.viewmodel.TrailsViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class Trail : ScopedFragment() {

    private lateinit var toolbar: TrailToolbar
    private lateinit var tools: TrailTools
    private lateinit var locationBroadcastReceiver: BroadcastReceiver
    private lateinit var bottomSheetSlide: BottomSheetSlide

    private var maps: TrailMaps? = null
    private val handler = Handler(Looper.getMainLooper())
    private var filter: IntentFilter = IntentFilter()
    private var location: Location? = null
    private var isFullScreen = false

    private var currentTrail: String? = null

    private lateinit var trailDataViewModel: TrailDataViewModel
    private val trailsViewModel: TrailsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_trail, container, false)

        toolbar = view.findViewById(R.id.toolbar)
        tools = view.findViewById(R.id.trail_tools)
        bottomSheetSlide = requireActivity() as BottomSheetSlide

        initViewModel()

        maps = view.findViewById(R.id.map_view)
        maps?.onCreate(savedInstanceState)
        maps?.resume()

        filter.addAction("location")
        filter.addAction("provider")

        updateToolsGravity(view)

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
                location ?: Toast.makeText(requireContext(), R.string.location_not_available, Toast.LENGTH_SHORT).show()

                if (currentTrail!!.isEmpty()) {
                    addTrail()
                } else {
                    val trailData = TrailData(
                            location!!.latitude,
                            location!!.longitude,
                            System.currentTimeMillis(),
                            position,
                            null,
                            null
                    )

                    trailDataViewModel.saveTrailData(currentTrail!!, trailData)
                    maps?.addPolyline(trailData)
                }
            }

            override fun onAddWithInfo(position: Int) {
                location ?: Toast.makeText(requireContext(), R.string.location_not_available, Toast.LENGTH_SHORT).show().also {
                    return
                }

                if (currentTrail!!.isEmpty()) {
                    addTrail()
                } else {
                    val dialog = AddMarker.newInstance(position, LatLng(location!!.latitude, location!!.longitude))

                    dialog.onNewTrailAddedSuccessfully = {
                        trailDataViewModel.saveTrailData(currentTrail!!, it)
                        maps?.addPolyline(it)
                    }

                    dialog.show(parentFragmentManager, "add_marker")
                }
            }

            override fun onRemove() {
                maps?.removePolyline()
            }

            override fun onWrapUnwrap() {
                maps?.wrapUnwrap()
            }
        })

        toolbar.setOnTrailToolbarEventListener(object : TrailToolbar.Companion.TrailToolsCallbacks {
            override fun onTrailsClicked() {
                startActivity(Intent(requireContext(), TrailsViewerActivity::class.java))
            }

            override fun onTrailsLongClicked() {
                if (currentTrail!!.isNotEmpty()) {
                    val intent = Intent(requireContext(), TrailDataActivity::class.java)
                    intent.putExtra("trail_name", currentTrail)
                    startActivity(intent)
                }
            }

            override fun onAddClicked() {
                addTrail()
            }

            override fun onMenuClicked() {
                TrailMenu.newInstance()
                    .show(requireActivity().supportFragmentManager, "trail_menu")
            }

        })

        maps?.setOnTrailMapCallbackListener(object : TrailMapCallbacks {
            override fun onMapInitialized() {
                if (savedInstanceState.isNotNull()) {
                    maps?.setCamera(savedInstanceState!!.getParcelable("camera"))
                }

                trailDataViewModel.trailDataAscending.observe(viewLifecycleOwner, {
                    maps?.addPolylines(it)
                })
            }

            override fun onMapClicked() {
                setFullScreen(true)
            }

            override fun onLineDeleted(trailData: TrailData?) {
                trailDataViewModel.deleteTrailData(trailData)
            }

            override fun onLineCountChanged(lineCount: Int) {
                tools.changeButtonState(lineCount.isZero())
            }
        })
    }

    private fun initViewModel() {
        currentTrail = TrailPreferences.getLastUsedTrail()
        val trailDataFactory = TrailDataFactory(currentTrail!!, requireActivity().application)
        trailDataViewModel = ViewModelProvider(requireActivity(), trailDataFactory).get(TrailDataViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        maps?.resume()
        currentTrail = TrailPreferences.getLastUsedTrail()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(locationBroadcastReceiver, filter)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        maps?.lowMemory()
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
            TrailPreferences.toolsMenuGravity -> {
                updateToolsGravity(requireView())
            }
            TrailPreferences.lastSelectedTrail -> {
                currentTrail = TrailPreferences.getLastUsedTrail()
                trailDataViewModel.loadTrailData(currentTrail!!)
                maps?.clear()
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

    private fun updateToolsGravity(view: View) {
        TransitionManager.beginDelayedTransition(
                view as ViewGroup,
                TransitionInflater.from(requireContext())
                    .inflateTransition(R.transition.tools_transition))

        val params = CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                    ViewGroup.LayoutParams.WRAP_CONTENT)

        params.apply {
            gravity = if (TrailPreferences.isToolsGravityToleft()) {
                Gravity.START or Gravity.CENTER_VERTICAL
            } else {
                Gravity.END or Gravity.CENTER_VERTICAL
            }

            marginStart = resources.getDimensionPixelSize(R.dimen.trail_tools_margin)
            marginEnd = resources.getDimensionPixelSize(R.dimen.trail_tools_margin)
        }

        tools.layoutParams = params
    }

    private fun addTrail() {
        val p0 = AddTrail.newInstance()

        p0.onNewTrailAddedSuccessfully = {
            trailsViewModel.addTrail(it)
        }

        p0.show(requireActivity().supportFragmentManager, "add_trail")
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
