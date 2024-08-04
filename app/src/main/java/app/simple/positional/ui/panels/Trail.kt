package app.simple.positional.ui.panels

import android.annotation.SuppressLint
import android.content.*
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import androidx.transition.TransitionManager
import app.simple.positional.R
import app.simple.positional.activities.subactivity.TrailsViewerActivity
import app.simple.positional.adapters.trail.AdapterTrailPoints
import app.simple.positional.callbacks.BottomSheetSlide
import app.simple.positional.decorations.trails.TrailMaps
import app.simple.positional.decorations.trails.TrailToolbar
import app.simple.positional.decorations.trails.TrailTools
import app.simple.positional.dialogs.trail.AddMarker
import app.simple.positional.dialogs.trail.AddTrail
import app.simple.positional.dialogs.trail.TrailMenu
import app.simple.positional.extensions.fragment.ScopedFragment
import app.simple.positional.extensions.maps.MapsCallbacks
import app.simple.positional.model.TrailPoint
import app.simple.positional.popups.miscellaneous.DeletePopupMenu
import app.simple.positional.popups.trail.PopupMarkers
import app.simple.positional.popups.trail.PopupTrailsDataMenu
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.preferences.TrailPreferences
import app.simple.positional.util.ConditionUtils.invert
import app.simple.positional.util.ConditionUtils.isNotNull
import app.simple.positional.util.ConditionUtils.isZero
import app.simple.positional.util.LocationExtension
import app.simple.positional.util.LocationPrompt
import app.simple.positional.util.ParcelUtils.parcelable
import app.simple.positional.util.StatusBarHeight
import app.simple.positional.util.TimeFormatter.formatDate
import app.simple.positional.util.ViewUtils.invisible
import app.simple.positional.util.ViewUtils.visible
import app.simple.positional.viewmodels.factory.TrailDataFactory
import app.simple.positional.viewmodels.viewmodel.LocationViewModel
import app.simple.positional.viewmodels.viewmodel.TrailDataViewModel
import app.simple.positional.viewmodels.viewmodel.TrailsViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class Trail : ScopedFragment() {

    private lateinit var toolbar: TrailToolbar
    private lateinit var tools: TrailTools
    private lateinit var recyclerView: RecyclerView
    private var bottomSheetPanel: BottomSheetBehavior<CoordinatorLayout>? = null
    private lateinit var bottomSheetSlide: BottomSheetSlide
    private lateinit var expandUp: ImageView
    private lateinit var art: ImageView
    private lateinit var dim: View

    private var backPress: OnBackPressedDispatcher? = null
    private var maps: TrailMaps? = null
    private val handler = Handler(Looper.getMainLooper())
    private var location: Location? = null
    private lateinit var locationViewModel: LocationViewModel

    private var isFullScreen = false
    private var peekHeight = 0
    private var x = 0F
    private var y = 0F
    private var currentTrail: String? = null

    private lateinit var trailDataViewModel: TrailDataViewModel
    private val trailsViewModel: TrailsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_trail, container, false)

        toolbar = view.findViewById(R.id.toolbar)
        tools = view.findViewById(R.id.trail_tools)
        recyclerView = view.findViewById(R.id.trail_data_recycler_view)

        kotlin.runCatching {
            bottomSheetPanel = BottomSheetBehavior.from(view.findViewById(R.id.trail_bottom_sheet))
        }

        expandUp = view.findViewById(R.id.expand_up)
        art = view.findViewById(R.id.art)
        dim = view.findViewById(R.id.dim)
        bottomSheetSlide = requireActivity() as BottomSheetSlide

        currentTrail = TrailPreferences.getCurrentTrail()
        trailDataViewModel = ViewModelProvider(this, TrailDataFactory())[TrailDataViewModel::class.java]
        locationViewModel = ViewModelProvider(requireActivity())[LocationViewModel::class.java]

        maps = view.findViewById(R.id.map_view)
        maps?.onCreate(savedInstanceState)

        backPress = requireActivity().onBackPressedDispatcher

        updateToolsGravity(view)

        tools.locationIndicatorUpdate(false)

        recyclerView.apply {
            setPadding(paddingLeft,
                       paddingTop + StatusBarHeight.getStatusBarHeight(resources),
                       paddingRight,
                       paddingBottom)

            alpha = if (isLandscapeOrientation) 1F else 0F
        }

        peekHeight = bottomSheetPanel?.peekHeight ?: 0

        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationViewModel.getLocation().observe(viewLifecycleOwner) {
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.Default) {
                    location = it

                    MainPreferences.setLastLatitude(location!!.latitude.toFloat())
                    MainPreferences.setLastLongitude(location!!.longitude.toFloat())

                    withContext(Dispatchers.Main) {
                        maps?.setFirstLocation(location)
                        maps?.location = location
                        maps?.addMarker(LatLng(location!!.latitude, location!!.longitude))
                        tools.locationIndicatorUpdate(true)
                    }
                }
            }
        }

        locationViewModel.getProvider().observe(viewLifecycleOwner) {
            tools.locationIconStatusUpdates()
        }

        trailDataViewModel.trailPointDescendingWithInfo.observe(viewLifecycleOwner) {
            val adapter = AdapterTrailPoints(it)

            adapter.setOnTrailsDataCallbackListener(object : AdapterTrailPoints.Companion.AdapterTrailsDataCallbacks {
                override fun onTrailsDataLongPressed(trailPoint: TrailPoint, view: View, position: Int) {
                    PopupTrailsDataMenu(view).setOnPopupCallbacksListener(object : PopupTrailsDataMenu.Companion.PopupTrailsCallbacks {
                        override fun onDelete() {
                            DeletePopupMenu(view).setOnPopupCallbacksListener(object : DeletePopupMenu.Companion.PopupDeleteCallbacks {
                                override fun delete() {
                                    trailDataViewModel.deleteTrailData(trailPoint)
                                }
                            })
                        }

                        override fun onCopy() {
                            val builder = StringBuilder().apply {
                                append(trailPoint.name ?: getString(R.string.not_available))
                                append("\n\n")
                                append(trailPoint.note ?: getString(R.string.not_available))
                                append("\n\n")
                                append(String.format(Locale.ENGLISH, "geo:%f,%f", trailPoint.latitude, trailPoint.longitude))
                                append("\n\n")
                                append(trailPoint.timeAdded.formatDate())
                            }

                            val clip: ClipData = ClipData.newPlainText("GPS Data", builder)
                            val clipboard: ClipboardManager =
                                    requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(clip)
                        }

                        override fun onShare() {
                            kotlin.runCatching {
                                startActivity(
                                        Intent(
                                                Intent.ACTION_VIEW,
                                            Uri.parse("geo:${trailPoint.latitude},${trailPoint.longitude}")))
                            }.getOrElse { throwable ->
                                Toast.makeText(requireContext(), throwable.message, Toast.LENGTH_SHORT)
                                        .show()
                            }
                        }

                        override fun onNavigate() {
                            kotlin.runCatching {
                                val uri: Uri =
                                    Uri.parse("google.navigation:q=" + trailPoint.latitude.toString() + "," + trailPoint.longitude.toString() + "&mode=d")
                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                intent.setPackage("com.google.android.apps.maps")
                                startActivity(intent)
                            }.getOrElse {
                                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                }

                override fun onAdd(view: View) {
                    if (currentTrail!!.isEmpty()) {
                        addTrail()
                    } else {
                        location
                                ?: Toast.makeText(requireContext(), R.string.location_not_available, Toast.LENGTH_SHORT)
                                        .show().also {
                                            return
                                        }

                        addMarker(view, location!!.latitude, location!!.longitude, location!!.accuracy, view.x, view.y)
                    }
                }

                override fun onTrailClicked(latLng: LatLng) {
                    maps?.moveMapCamera(latLng, 15F, TrailPreferences.getMapTilt(), 1500)

                    if (bottomSheetPanel?.state == BottomSheetBehavior.STATE_EXPANDED) {
                        bottomSheetPanel?.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                }
            })

            if (it.first.isEmpty()) {
                art.visible(false)
                recyclerView.adapter = null
            } else {
                art.invisible(false)
                recyclerView.adapter = adapter
            }
        }

        bottomSheetPanel?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED  -> {
                        backPressed(true)
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        backPressed(false)
                        if (backPress!!.hasEnabledCallbacks()) {
                            backPress?.onBackPressed()
                        }
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                recyclerView.alpha = slideOffset
                expandUp.alpha = 1 - slideOffset
                dim.alpha = slideOffset
                if (!isFullScreen) {
                    bottomSheetSlide.onBottomSheetSliding(slideOffset, true)
                }
            }
        })

        tools.setTrailCallbacksListener(object : TrailTools.Companion.TrailCallbacks {
            override fun onLocation(reset: Boolean) {
                if (LocationExtension.getLocationStatus(requireContext())) {
                    if (location.isNotNull()) {
                        if (reset) {
                            maps?.resetCamera(18F)
                        } else {
                            maps?.moveMapCamera(LatLng(location!!.latitude, location!!.longitude),
                                                TrailPreferences.getMapZoom(),
                                                TrailPreferences.getMapTilt(),
                                                1000)
                        }
                    }
                } else {
                    LocationPrompt.displayLocationSettingsRequest(requireActivity())
                }
            }

            override fun onAdd(view: View) {
                if (currentTrail!!.isEmpty()) {
                    addTrail()
                } else {
                    location
                        ?: Toast.makeText(requireContext(), R.string.location_not_available, Toast.LENGTH_SHORT)
                            .show().also {
                                return
                            }

                    addMarker(view, location!!.latitude, location!!.longitude, location!!.accuracy, view.x, view.y)
                }
            }

            override fun onRemove(remove: View) {
                DeletePopupMenu(remove).setOnPopupCallbacksListener(object : DeletePopupMenu.Companion.PopupDeleteCallbacks {
                    override fun delete() {
                        maps?.removePolyline()
                    }
                })
            }

            override fun onWrapUnwrap() {
                maps?.wrapUnwrap()
            }
        })

        toolbar.setOnTrailToolbarEventListener(object : TrailToolbar.Companion.TrailToolsCallbacks {
            override fun onTrailsClicked() {
                startActivity(Intent(requireContext(), TrailsViewerActivity::class.java))
            }

            override fun onAddClicked() {
                addTrail()
            }

            override fun onMenuClicked() {
                TrailMenu.newInstance()
                    .show(childFragmentManager, "trail_menu")
            }

        })

        maps?.setOnMapsCallbackListener(object : MapsCallbacks {
            override fun onMapInitialized() {
                if (savedInstanceState.isNotNull()) {
                    maps?.setCamera(savedInstanceState!!.parcelable("camera"))
                }

                trailDataViewModel.trailPointAscending.observe(viewLifecycleOwner) {
                    maps?.addPolylines(it)
                }
            }

            override fun onMapClicked() {
                if (isLandscapeOrientation.invert()) {
                    setFullScreen()
                }
            }

            override fun onMapLongClicked(latLng: LatLng) {
                if (currentTrail!!.isEmpty()) {
                    addTrail()
                } else {
                    addMarker(maps!!, latLng.latitude, latLng.longitude, 0F, x / 2, y / 2)
                }
            }

            override fun onLineDeleted(trailPoint: TrailPoint?) {
                trailDataViewModel.deleteTrailData(trailPoint)
            }

            override fun onLineCountChanged(lineCount: Int) {
                tools.changeWrapButtonState(lineCount.isZero())
            }
        })

        dim.setOnTouchListener { _, event ->
            when (event!!.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = event.x
                    y = event.y
                }
            }

            false
        }
    }

    override fun onResume() {
        super.onResume()
        maps?.onResume()
        currentTrail = TrailPreferences.getCurrentTrail()
    }

    override fun onPause() {
        super.onPause()
        maps?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        maps?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putFloat("translation", toolbar.translationY)
        outState.putBoolean("fullscreen", isFullScreen)
        outState.putParcelable("camera", maps?.getCamera())
        outState.putInt("bottom_sheet_state", bottomSheetPanel?.state ?: 4)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        if (savedInstanceState.isNotNull()) {
            isFullScreen = savedInstanceState!!.getBoolean("fullscreen")
            if (isLandscapeOrientation) {
                setFullScreen()
            } else {
                setFullScreen()
            }
            bottomSheetPanel?.state = savedInstanceState.getInt("bottom_sheet_state")
        }
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            TrailPreferences.toolsMenuGravity -> {
                updateToolsGravity(requireView())
            }
            TrailPreferences.selectedTrail -> {
                currentTrail = TrailPreferences.getCurrentTrail()
                trailDataViewModel.setTrailName(currentTrail!!)
                maps?.clear()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        maps?.onDestroy()
        handler.removeCallbacksAndMessages(null)
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

    private fun updateToolsGravity(view: View) {
        if (isLandscapeOrientation) return

        TransitionManager.beginDelayedTransition(
                view as ViewGroup,
                TransitionInflater.from(requireContext())
                    .inflateTransition(R.transition.tools_transition))

        val params = CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                    ViewGroup.LayoutParams.WRAP_CONTENT)

        params.apply {
            gravity = if (TrailPreferences.isToolsGravityToLeft()) {
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

    private fun addMarker(view: View, lat: Double, lon: Double, accuracy: Float, x: Float, y: Float) {
        PopupMarkers(view, x, y).setOnPopupMarkersCallbackListener(object : PopupMarkers.Companion.PopupMarkersCallbacks {
            override fun onMarkerClicked(position: Int) {
                val dialog = AddMarker.newInstance(position, LatLng(lat, lon), accuracy)

                dialog.onNewTrailAddedSuccessfully = {
                    trailDataViewModel.saveTrailData(currentTrail!!, it)
                    maps?.addPolyline(it)
                }

                dialog.show(parentFragmentManager, "add_marker")
            }

            override fun onMarkerLongClicked(position: Int) {
                val trailPoint = TrailPoint(
                    lat,
                    lon,
                    System.currentTimeMillis(),
                    position,
                    null,
                    null,
                    accuracy
                )

                trailDataViewModel.saveTrailData(currentTrail!!, trailPoint)
                maps?.addPolyline(trailPoint)
            }
        })
    }

    private fun backPressed(value: Boolean) {
        /**
         * @see Time.backPressed
         */
        backPress?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(value) {
            override fun handleOnBackPressed() {
                if (bottomSheetPanel?.state == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetPanel?.state = BottomSheetBehavior.STATE_COLLAPSED
                }
                /**
                 * Remove this callback as soon as it's been called
                 * to prevent any further registering
                 */
                remove()
            }
        })
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
