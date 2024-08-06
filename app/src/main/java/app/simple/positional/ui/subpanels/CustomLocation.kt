package app.simple.positional.ui.subpanels

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import app.simple.positional.R
import app.simple.positional.activities.subactivity.MapSearchActivity
import app.simple.positional.activities.subactivity.WebPageViewerActivity
import app.simple.positional.adapters.settings.LocationsAdapter
import app.simple.positional.database.instances.LocationDatabase
import app.simple.positional.decorations.padding.PaddingAwareLinearLayout
import app.simple.positional.decorations.popup.PopupLinearLayout
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.extensions.fragment.ScopedFragment
import app.simple.positional.model.Locations
import app.simple.positional.popups.miscellaneous.DeletePopupMenu
import app.simple.positional.popups.settings.CustomLocationPopupMenu
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.ConditionUtils.isZero
import app.simple.positional.util.TextViewUtils.capitalizeText
import app.simple.positional.util.ViewUtils
import app.simple.positional.util.ViewUtils.invisible
import app.simple.positional.util.ViewUtils.visible
import app.simple.positional.viewmodels.viewmodel.CustomLocationViewModel
import gov.nasa.worldwind.geom.Angle.isValidLatitude
import gov.nasa.worldwind.geom.Angle.isValidLongitude
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomLocation : ScopedFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var art: ImageView
    private lateinit var search: DynamicRippleImageButton
    private lateinit var options: DynamicRippleImageButton
    private lateinit var addressInputEditText: EditText
    private lateinit var latitudeInputEditText: EditText
    private lateinit var longitudeInputEditText: EditText
    private lateinit var inputLayoutsContainer: PaddingAwareLinearLayout

    private val customLocationViewModel: CustomLocationViewModel by viewModels()
    private lateinit var locationsAdapter: LocationsAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var linearLayoutManager: LinearLayoutManager

    private var address = ""

    private val registerForActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                result.data?.let { intent ->
                    intent.getStringExtra("address")?.let { address ->
                        intent.getDoubleExtra("latitude", 0.0).let { latitude ->
                            intent.getDoubleExtra("longitude", 0.0).let { longitude ->
                                this.latitudeInputEditText.setText(latitude.toString())
                                this.longitudeInputEditText.setText(longitude.toString())
                                if (address.isNotEmpty()) {
                                    this.addressInputEditText.setText(address)
                                }
                            }
                        }
                    }
                }
            }

            Activity.RESULT_CANCELED -> {
                Log.d("DirectionTarget", "Search cancelled")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_custom_location, container, false)

        recyclerView = view.findViewById(R.id.custom_locations_recycler_view)
        art = view.findViewById(R.id.art_empty)
        search = view.findViewById(R.id.search)
        options = view.findViewById(R.id.options_custom_coordinates)
        addressInputEditText = view.findViewById(R.id.address)
        latitudeInputEditText = view.findViewById(R.id.latitude)
        longitudeInputEditText = view.findViewById(R.id.longitude)
        inputLayoutsContainer = view.findViewById(R.id.custom_location_input_container)

        locationsAdapter = LocationsAdapter()
        itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        linearLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        recyclerView.layoutManager = linearLayoutManager

        ViewUtils.addShadow(inputLayoutsContainer)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (MainPreferences.isCustomCoordinate()) {
            address = MainPreferences.getAddress()
            addressInputEditText.setText(address)
            latitudeInputEditText.setText(MainPreferences.getCoordinates()[0].toString())
            longitudeInputEditText.setText(MainPreferences.getCoordinates()[1].toString())
        }

        search.setOnClickListener {
            registerForActivityResult.launch(Intent(requireContext(), MapSearchActivity::class.java))
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (linearLayoutManager.findFirstCompletelyVisibleItemPosition() > 0) {
                        inputLayoutsContainer.animateElevation(200F)
                    } else {
                        inputLayoutsContainer.animateElevation(0F)
                    }
                }
            }
        })

        customLocationViewModel.customLocations.observe(viewLifecycleOwner) {
            locationsAdapter.setList(it)
            recyclerView.adapter = locationsAdapter
        }

        customLocationViewModel.artState.observe(viewLifecycleOwner) {
            if (it) {
                art.visible(true)
                inputLayoutsContainer.animateElevation(0F)
            } else {
                art.invisible(true)
            }
        }

        options.setOnClickListener {
            val popup = CustomLocationPopupMenu(
                    layoutInflater.inflate(
                            R.layout.popup_custom_coordinates, PopupLinearLayout(requireContext()), true), options)

            popup.setOnPopupCallbackListener { source ->
                when (source) {
                    getString(R.string.save) -> {
                        viewLifecycleOwner.lifecycleScope.launch {
                            withContext(Dispatchers.Default) {
                                if (!latitudeInputEditText.text.length.isZero() || !longitudeInputEditText.text.length.isZero()) {
                                    if (isValidLatitude(latitudeInputEditText.text.toString().toDouble()) && isValidLongitude(longitudeInputEditText.text.toString().toDouble())) {
                                        kotlin.runCatching {
                                            val locations = Locations()

                                            locations.address = if (addressInputEditText.text.isNullOrEmpty()) {
                                                "----"
                                            } else {
                                                addressInputEditText.text.toString().capitalizeText()
                                            }
                                            locations.latitude = latitudeInputEditText.text.toString().toDouble()
                                            locations.longitude = longitudeInputEditText.text.toString().toDouble()
                                            locations.date = System.currentTimeMillis()

                                            customLocationViewModel.saveLocation(locations)

                                            withContext(Dispatchers.Main) {
                                                locationsAdapter.addLocation(locations)
                                            }
                                        }.getOrElse {
                                            showToast(getString(R.string.failed))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    getString(R.string.delete_all) -> {
                        DeletePopupMenu(view) {
                            customLocationViewModel.deleteAll()
                        }
                    }

                    getString(R.string.set_and_save) -> {
                        viewLifecycleOwner.lifecycleScope.launch {
                            withContext(Dispatchers.Default) {
                                kotlin.runCatching {
                                    if (latitudeInputEditText.text.toString().isNotEmpty() || longitudeInputEditText.text.toString().isNotEmpty()) {
                                        if (isValidLatitude(latitudeInputEditText.text.toString().toDouble()) && isValidLongitude(longitudeInputEditText.text.toString().toDouble())) {
                                            val locations = Locations()

                                            locations.address = if (addressInputEditText.text.isNullOrEmpty()) {
                                                "----"
                                            } else {
                                                addressInputEditText.text.toString().capitalizeText()
                                            }
                                            locations.latitude = latitudeInputEditText.text.toString().toDouble()
                                            locations.longitude = longitudeInputEditText.text.toString().toDouble()
                                            locations.date = System.currentTimeMillis()

                                            customLocationViewModel.saveLocation(locations)

                                            MainPreferences.setCustomCoordinates(true)
                                            MainPreferences.setLatitude(latitudeInputEditText.text.toString().toFloat())
                                            MainPreferences.setLongitude(longitudeInputEditText.text.toString().toFloat())
                                            MainPreferences.setAddress(addressInputEditText.text.toString())
                                        }
                                    }

                                    withContext(Dispatchers.Main) {
                                        requireActivity().finishAfterTransition()
                                    }
                                }.getOrElse {
                                    MainPreferences.setCustomCoordinates(false)
                                    showToast(getString(R.string.failed))
                                }
                            }
                        }
                    }

                    getString(R.string.set_only) -> {
                        viewLifecycleOwner.lifecycleScope.launch {
                            withContext(Dispatchers.Default) {
                                kotlin.runCatching {
                                    if (latitudeInputEditText.text.toString().isNotEmpty() || longitudeInputEditText.text.toString().isNotEmpty()) {
                                        if (isValidLatitude(latitudeInputEditText.text.toString().toDouble()) && isValidLongitude(longitudeInputEditText.text.toString().toDouble())) {
                                            MainPreferences.setCustomCoordinates(true)
                                            MainPreferences.setLatitude(latitudeInputEditText.text.toString().toFloat())
                                            MainPreferences.setLongitude(longitudeInputEditText.text.toString().toFloat())
                                            MainPreferences.setAddress(addressInputEditText.text.toString())
                                        }
                                    }

                                    withContext(Dispatchers.Main) {
                                        requireActivity().finishAfterTransition()
                                    }
                                }.getOrElse {
                                    MainPreferences.setCustomCoordinates(false)
                                    showToast(getString(R.string.failed))
                                }
                            }
                        }
                    }

                    getString(R.string.help) -> {
                        val intent = Intent(requireActivity(), WebPageViewerActivity::class.java)
                        intent.putExtra("source", "Custom Coordinates Help")
                        startActivity(intent)
                    }
                }
            }
        }

        addressInputEditText.doOnTextChanged { text, _, _, _ ->
            address = text.toString()
        }

        locationsAdapter.setOnLocationsCallbackListener(object : LocationsAdapter.LocationsCallback {
            override fun onLocationClicked(locations: Locations) {
                latitudeInputEditText.setText(locations.latitude.toString())
                longitudeInputEditText.setText(locations.longitude.toString())
                addressInputEditText.setText(locations.address)
            }

            override fun onLocationLongClicked(locations: Locations) {
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(Dispatchers.Default) {
                        kotlin.runCatching {
                            MainPreferences.setCustomCoordinates(true)
                            MainPreferences.setLatitude(locations.latitude.toFloat())
                            MainPreferences.setLongitude(locations.longitude.toFloat())
                            MainPreferences.setAddress(locations.address)

                            withContext(Dispatchers.Main) {
                                requireActivity().finishAfterTransition()
                            }
                        }.getOrElse {
                            MainPreferences.setCustomCoordinates(false)
                            showToast(getString(R.string.failed))
                        }
                    }
                }
            }
        })
    }

    /**
     * Always runs the toast message from the main thread
     */
    private fun showToast(message: String) {
        requireActivity().runOnUiThread {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun LinearLayout.animateElevation(elevation: Float) {
        val valueAnimator = ValueAnimator.ofFloat(this.elevation, elevation)
        valueAnimator.duration = 500L
        valueAnimator.interpolator = LinearOutSlowInInterpolator()
        valueAnimator.addUpdateListener {
            this.elevation = it.animatedValue as Float
        }
        valueAnimator.start()
    }

    private val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object
        : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
            // Remove swiped item from list and notify the RecyclerView
            val p0 = locationsAdapter.removeItem(viewHolder.absoluteAdapterPosition)

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                val db = Room.databaseBuilder(requireContext(), LocationDatabase::class.java, "locations.db").build()
                db.locationDao()?.deleteLocation(p0)
                if (db.locationDao()!!.getAllLocations().isEmpty()) {
                    handler.post {
                        art.visible(true)
                        inputLayoutsContainer.animateElevation(0F)
                    }
                }
                db.close()
            }
        }
    }

    companion object {
        fun newInstance(): CustomLocation {
            val args = Bundle()
            val fragment = CustomLocation()
            fragment.arguments = args
            return fragment
        }
    }
}
