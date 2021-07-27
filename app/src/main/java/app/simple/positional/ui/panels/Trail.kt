package app.simple.positional.ui.panels

import android.content.*
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionInflater
import androidx.transition.TransitionManager
import app.simple.positional.R
import app.simple.positional.activities.fragment.ScopedFragment
import app.simple.positional.activities.subactivity.TrailsViewerActivity
import app.simple.positional.adapters.trail.AdapterTrailData
import app.simple.positional.callbacks.BottomSheetSlide
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout
import app.simple.positional.decorations.trails.TrailMapCallbacks
import app.simple.positional.decorations.trails.TrailMaps
import app.simple.positional.decorations.trails.TrailToolbar
import app.simple.positional.decorations.trails.TrailTools
import app.simple.positional.dialogs.trail.AddMarker
import app.simple.positional.dialogs.trail.AddTrail
import app.simple.positional.dialogs.trail.TrailMenu
import app.simple.positional.model.TrailData
import app.simple.positional.popups.miscellaneous.DeletePopupMenu
import app.simple.positional.popups.trail.PopupMarkers
import app.simple.positional.popups.trail.PopupTrailsDataMenu
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.preferences.TrailPreferences
import app.simple.positional.util.ConditionUtils.isNotNull
import app.simple.positional.util.ConditionUtils.isZero
import app.simple.positional.util.StatusBarHeight
import app.simple.positional.util.TimeFormatter.formatDate
import app.simple.positional.util.ViewUtils.invisible
import app.simple.positional.util.ViewUtils.visible
import app.simple.positional.viewmodels.factory.TrailDataFactory
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
    private lateinit var locationBroadcastReceiver: BroadcastReceiver
    private lateinit var trailRecyclerView: RecyclerView
    private lateinit var bottomSheetPanel: BottomSheetBehavior<CoordinatorLayout>
    private lateinit var bottomSheetSlide: BottomSheetSlide
    private lateinit var expandUp: ImageView
    private lateinit var art: ImageView
    private lateinit var dim: View

    private var backPress: OnBackPressedDispatcher? = null
    private var maps: TrailMaps? = null
    private val handler = Handler(Looper.getMainLooper())
    private var filter: IntentFilter = IntentFilter()
    private var location: Location? = null
    private var isFullScreen = false
    private var peekHeight = 0
    private var currentTrail: String? = null

    private lateinit var trailDataViewModel: TrailDataViewModel
    private val trailsViewModel: TrailsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_trail, container, false)

        toolbar = view.findViewById(R.id.toolbar)
        tools = view.findViewById(R.id.trail_tools)
        trailRecyclerView = view.findViewById(R.id.trail_data_recycler_view)
        bottomSheetPanel = BottomSheetBehavior.from(view.findViewById(R.id.trail_bottom_sheet))
        expandUp = view.findViewById(R.id.expand_up)
        art = view.findViewById(R.id.art)
        dim = view.findViewById(R.id.dim)
        bottomSheetSlide = requireActivity() as BottomSheetSlide

        currentTrail = TrailPreferences.getCurrentTrail()
        val trailDataFactory = TrailDataFactory(currentTrail!!, requireActivity().application)
        trailDataViewModel = ViewModelProvider(this, trailDataFactory).get(TrailDataViewModel::class.java)

        maps = view.findViewById(R.id.map_view)
        maps?.onCreate(savedInstanceState)
        maps?.resume()

        filter.addAction("location")
        filter.addAction("provider")

        backPress = requireActivity().onBackPressedDispatcher

        updateToolsGravity(view)

        trailRecyclerView.apply {
            setPadding(paddingLeft,
                       paddingTop + StatusBarHeight.getStatusBarHeight(resources),
                       paddingRight,
                       paddingBottom)
        }

        peekHeight = bottomSheetPanel.peekHeight

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

        trailDataViewModel.trailDataDescendingWithInfo.observe(viewLifecycleOwner, {
            val adapter = AdapterTrailData(it)

            adapter.setOnTrailsDataCallbackListener(object : AdapterTrailData.Companion.AdapterTrailsDataCallbacks {
                override fun onTrailsDataLongPressed(trailData: TrailData, view: View, position: Int) {
                    val popup = PopupTrailsDataMenu(
                            layoutInflater.inflate(R.layout.popup_trails_data,
                                                   DynamicCornerLinearLayout(requireContext())), view)

                    popup.setOnPopupCallbacksListener(object : PopupTrailsDataMenu.Companion.PopupTrailsCallbacks {
                        override fun onDelete() {
                            val deletePopupMenu = DeletePopupMenu(
                                    layoutInflater.inflate(R.layout.popup_delete_confirmation,
                                                           DynamicCornerLinearLayout(requireContext())), view)

                            deletePopupMenu.setOnPopupCallbacksListener(object : DeletePopupMenu.Companion.PopupDeleteCallbacks {
                                override fun delete() {
                                    trailDataViewModel.deleteTrailData(trailData)
                                }
                            })
                        }

                        override fun onCopy() {
                            val builder = StringBuilder().apply {
                                append(trailData.name ?: getString(R.string.not_available))
                                append("\n\n")
                                append(trailData.note ?: getString(R.string.not_available))
                                append("\n\n")
                                append(String.format(Locale.ENGLISH, "geo:%f,%f", trailData.latitude, trailData.longitude))
                                append("\n\n")
                                append(trailData.timeAdded.formatDate())
                            }

                            val clip: ClipData = ClipData.newPlainText("GPS Data", builder)
                            val clipboard: ClipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(clip)
                        }

                        override fun onShare() {
                            kotlin.runCatching {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:${trailData.latitude},${trailData.longitude}"))
                                startActivity(intent)
                            }.getOrElse { throwable ->
                                Toast.makeText(requireContext(), throwable.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                }

                override fun onAdd(view: View) {
                    addMarker(view)
                }
            })

            if (it.first.isNullOrEmpty()) {
                art.visible(false)
                trailRecyclerView.adapter = null
            } else {
                art.invisible(false)
                trailRecyclerView.adapter = adapter
            }
        })

        bottomSheetPanel.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
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
                trailRecyclerView.alpha = slideOffset
                expandUp.alpha = 1 - slideOffset
                dim.alpha = slideOffset
                if (!isFullScreen) {
                    bottomSheetSlide.onBottomSheetSliding(slideOffset)
                }
            }
        })

        tools.setTrailCallbacksListener(object : TrailTools.Companion.TrailCallbacks {
            override fun onLocation() {
                if (location.isNotNull()) {
                    maps?.moveMapCamera(LatLng(location!!.latitude, location!!.longitude),
                                        TrailPreferences.getMapZoom(),
                                        500)
                }
            }

            override fun onAdd(view: View) {
                addMarker(view)
            }

            override fun onRemove(remove: View) {
                val popup = DeletePopupMenu(
                        layoutInflater.inflate(R.layout.popup_delete_confirmation,
                                               DynamicCornerLinearLayout(requireContext())), remove)

                popup.setOnPopupCallbacksListener(object : DeletePopupMenu.Companion.PopupDeleteCallbacks {
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

    override fun onResume() {
        super.onResume()
        maps?.resume()
        currentTrail = TrailPreferences.getCurrentTrail()
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
        outState.putInt("bottom_sheet_state", bottomSheetPanel.state)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        if (savedInstanceState.isNotNull()) {
            isFullScreen = !savedInstanceState!!.getBoolean("fullscreen")
            setFullScreen(false)
            bottomSheetPanel.state = savedInstanceState.getInt("bottom_sheet_state")
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
        maps?.removeCallbacks { }
        maps?.destroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationBroadcastReceiver)
        handler.removeCallbacksAndMessages(null)
    }

    private fun setFullScreen(forBottomBar: Boolean) {
        if (isFullScreen) {
            toolbar.show()
            bottomSheetPanel.peekHeight = peekHeight
        } else {
            toolbar.hide()
            bottomSheetPanel.peekHeight = 0
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

    private fun addMarker(view: View) {
        val popup = PopupMarkers(
                layoutInflater.inflate(R.layout.popup_trail_markers,
                                       DynamicCornerLinearLayout(requireContext())), view)

        popup.setOnPopupMarkersCallbackListener(object : PopupMarkers.Companion.PopupMarkersCallbacks {
            override fun onMarkerClicked(position: Int) {
                location ?: Toast.makeText(requireContext(), R.string.location_not_available, Toast.LENGTH_SHORT).show().also {
                    return
                }

                if (currentTrail!!.isEmpty()) {
                    addTrail()
                } else {
                    val dialog = AddMarker.newInstance(position, location!!)

                    dialog.onNewTrailAddedSuccessfully = {
                        trailDataViewModel.saveTrailData(currentTrail!!, it)
                        maps?.addPolyline(it)
                    }

                    dialog.show(parentFragmentManager, "add_marker")
                }
            }

            override fun onMarkerLongClicked(position: Int) {
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
                            null,
                            location?.accuracy ?: -1F
                    )

                    trailDataViewModel.saveTrailData(currentTrail!!, trailData)
                    maps?.addPolyline(trailData)
                }
            }
        })
    }

    private fun backPressed(value: Boolean) {
        /**
         * @see Clock.backPressed
         */
        backPress?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(value) {
            override fun handleOnBackPressed() {
                if (bottomSheetPanel.state == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetPanel.state = BottomSheetBehavior.STATE_COLLAPSED
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
