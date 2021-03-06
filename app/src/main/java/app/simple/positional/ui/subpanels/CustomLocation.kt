package app.simple.positional.ui.subpanels

import android.content.Intent
import android.content.res.ColorStateList
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.ContentLoadingProgressBar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import app.simple.positional.R
import app.simple.positional.activities.fragment.ScopedFragment
import app.simple.positional.activities.subactivity.WebPageViewerActivity
import app.simple.positional.adapters.settings.LocationsAdapter
import app.simple.positional.database.instances.LocationDatabase
import app.simple.positional.decorations.popup.PopupLinearLayout
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.model.Locations
import app.simple.positional.popups.CustomLocationPopupMenu
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.TextViewUtils.capitalizeText
import app.simple.positional.util.ViewUtils.makeInvisible
import app.simple.positional.util.ViewUtils.makeVisible
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import gov.nasa.worldwind.geom.Angle.isValidLatitude
import gov.nasa.worldwind.geom.Angle.isValidLongitude
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CustomLocation : ScopedFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var art: ImageView
    private lateinit var options: DynamicRippleImageButton
    private lateinit var loadingProgressBar: ContentLoadingProgressBar
    private lateinit var addressInputEditText: TextInputEditText
    private lateinit var latitudeInputEditText: TextInputEditText
    private lateinit var longitudeInputEditText: TextInputEditText
    private lateinit var latitudeInputLayout: TextInputLayout
    private lateinit var longitudeInputLayout: TextInputLayout

    private lateinit var locationsAdapter: LocationsAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper
    private val handler = Handler(Looper.getMainLooper())

    private var address = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_custom_location, container, false)

        recyclerView = view.findViewById(R.id.custom_locations_recycler_view)
        art = view.findViewById(R.id.art_empty)
        options = view.findViewById(R.id.options_custom_coordinates)
        loadingProgressBar = view.findViewById(R.id.address_indicator)
        addressInputEditText = view.findViewById(R.id.address_input)
        latitudeInputEditText = view.findViewById(R.id.latitude_input)
        longitudeInputEditText = view.findViewById(R.id.longitude_input)
        latitudeInputLayout = view.findViewById(R.id.latitude_container)
        longitudeInputLayout = view.findViewById(R.id.longitude_container)

        locationsAdapter = LocationsAdapter()
        itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (MainPreferences.isCustomCoordinate()) {
            address = MainPreferences.getAddress()
            handler.postDelayed(geoCoderRunnable, 500L)
            addressInputEditText.setText(address)
            latitudeInputEditText.setText(MainPreferences.getCoordinates()[0].toString())
            longitudeInputEditText.setText(MainPreferences.getCoordinates()[1].toString())
        } else {
            loadingProgressBar.hide()
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
            val db = Room.databaseBuilder(
                    requireContext(),
                    LocationDatabase::class.java,
                    "locations.db")
                    .fallbackToDestructiveMigration()
                    .build()

            val list = db.locationDao()!!.getAllLocations()
            db.close()

            withContext(Dispatchers.Main) {
                if (list.isEmpty()) {
                    art.makeVisible(true)
                } else {
                    art.makeInvisible(true)
                }
                locationsAdapter.setList(list)
                recyclerView.adapter = locationsAdapter
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

                            var list = mutableListOf<Locations>()

                            withContext(Dispatchers.Default) {
                                if (latitudeInputEditText.text.toString().isNotEmpty() || longitudeInputEditText.text.toString().isNotEmpty()) {
                                    if (isValidLatitude(latitudeInputEditText.text.toString().toDouble()) && isValidLongitude(longitudeInputEditText.text.toString().toDouble())) {
                                        val db = Room.databaseBuilder(requireContext(), LocationDatabase::class.java, "locations.db").fallbackToDestructiveMigration().build()
                                        val locations = Locations()

                                        try {
                                            locations.address = if (addressInputEditText.text.isNullOrEmpty()) {
                                                "----"
                                            } else {
                                                addressInputEditText.text.toString().capitalizeText()
                                            }
                                            locations.latitude = latitudeInputEditText.text.toString().toDouble()
                                            locations.longitude = longitudeInputEditText.text.toString().toDouble()
                                            locations.date = System.currentTimeMillis()
                                            db.locationDao()?.insetLocation(location = locations)
                                            list = db.locationDao()!!.getAllLocations()
                                        } catch (e: NumberFormatException) {
                                            showToast(e.message!!)
                                        } finally {
                                            db.close()
                                        }
                                    }
                                }
                            }

                            if (list.isNotEmpty()) {
                                locationsAdapter.setList(list)
                                art.makeInvisible(true)
                            } else {
                                showToast(getString(R.string.failed))
                            }
                        }
                    }
                    getString(R.string.delete_all) -> {
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Default) {
                            val db = Room.databaseBuilder(requireContext(), LocationDatabase::class.java, "locations.db").build()
                            db.locationDao()?.nukeTable()
                            val list = db.locationDao()!!.getAllLocations()

                            withContext(Dispatchers.Main) {
                                if (list.isEmpty()) {
                                    locationsAdapter.clearList()
                                    art.makeVisible(true)
                                }
                            }
                        }
                    }
                    getString(R.string.set_and_save) -> {
                        viewLifecycleOwner.lifecycleScope.launch {
                            withContext(Dispatchers.Default) {
                                if (latitudeInputEditText.text.toString().isNotEmpty() || longitudeInputEditText.text.toString().isNotEmpty()) {
                                    if (isValidLatitude(latitudeInputEditText.text.toString().toDouble()) && isValidLongitude(longitudeInputEditText.text.toString().toDouble())) {
                                        val db = Room.databaseBuilder(requireContext(), LocationDatabase::class.java, "locations.db").fallbackToDestructiveMigration().build()
                                        val locations = Locations()

                                        try {
                                            locations.address = if (addressInputEditText.text.isNullOrEmpty()) {
                                                "----"
                                            } else {
                                                addressInputEditText.text.toString().capitalizeText()
                                            }
                                            locations.latitude = latitudeInputEditText.text.toString().toDouble()
                                            locations.longitude = longitudeInputEditText.text.toString().toDouble()
                                            locations.date = System.currentTimeMillis()
                                            db.locationDao()?.insetLocation(location = locations)
                                            MainPreferences.setCustomCoordinates(true)
                                            MainPreferences.setLatitude(latitudeInputEditText.text.toString().toFloat())
                                            MainPreferences.setLongitude(longitudeInputEditText.text.toString().toFloat())
                                            MainPreferences.setAddress(addressInputEditText.text.toString())
                                        } catch (e: NumberFormatException) {
                                            MainPreferences.setCustomCoordinates(false)
                                        } finally {
                                            db.close()
                                        }
                                    }
                                }
                            }

                            if (MainPreferences.isCustomCoordinate()) {
                                requireActivity().finishAfterTransition()
                            } else {
                                showToast(getString(R.string.failed))
                            }
                        }
                    }
                    getString(R.string.set_only) -> {
                        viewLifecycleOwner.lifecycleScope.launch {
                            withContext(Dispatchers.Default) {
                                if (latitudeInputEditText.text.toString().isNotEmpty() || longitudeInputEditText.text.toString().isNotEmpty()) {
                                    if (isValidLatitude(latitudeInputEditText.text.toString().toDouble()) && isValidLongitude(longitudeInputEditText.text.toString().toDouble())) {
                                        try {
                                            MainPreferences.setCustomCoordinates(true)
                                            MainPreferences.setLatitude(latitudeInputEditText.text.toString().toFloat())
                                            MainPreferences.setLongitude(longitudeInputEditText.text.toString().toFloat())
                                            MainPreferences.setAddress(addressInputEditText.text.toString().capitalizeText())
                                        } catch (e: NumberFormatException) {
                                            MainPreferences.setCustomCoordinates(false)
                                        }
                                    }
                                }
                            }

                            if (MainPreferences.isCustomCoordinate()) {
                                requireActivity().finishAfterTransition()
                            } else {
                                showToast(getString(R.string.failed))
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

        addressInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                handler.removeCallbacks(geoCoderRunnable)
                address = s.toString()
                handler.postDelayed(geoCoderRunnable, 500)
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        latitudeInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                /* no-op */
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    if (isValidLatitude(s.toString().toDouble())) {
                        latitudeInputLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.valid_input)
                        latitudeInputLayout.hintTextColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.valid_input))
                    } else {
                        throw NumberFormatException()
                    }
                } catch (e: NumberFormatException) {
                    latitudeInputLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.invalid_input)
                    latitudeInputLayout.hintTextColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.invalid_input))
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        longitudeInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                /* no-op */
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    if (isValidLongitude(s.toString().toDouble())) {
                        longitudeInputLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.valid_input)
                        longitudeInputLayout.hintTextColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.valid_input))
                    } else {
                        throw NumberFormatException()
                    }
                } catch (e: NumberFormatException) {
                    longitudeInputLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.invalid_input)
                    longitudeInputLayout.hintTextColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.invalid_input))
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        locationsAdapter.setOnLocationsCallbackListener(object : LocationsAdapter.LocationsCallback {
            override fun onLocationClicked(locations: Locations) {
                latitudeInputEditText.setText(locations.latitude.toString())
                longitudeInputEditText.setText(locations.longitude.toString())
                addressInputEditText.setText(locations.address)
            }
        })
    }

    /**
     * Always runs the toast message from the main thread
     */
    private fun showToast(message: String) {
        handler.post {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }

    private val geoCoderRunnable: Runnable = Runnable { getCoordinatesFromAddress(address) }

    private fun getCoordinatesFromAddress(address: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            loadingProgressBar.show()

            val geocoder = Geocoder(context)
            var addresses: MutableList<Address>?
            var latitude: Double? = null
            var longitude: Double? = null

            withContext(Dispatchers.IO) {
                runCatching {
                    MainPreferences.setAddress(address)

                    addresses = geocoder.getFromLocationName(address, 1)
                    if (addresses != null && addresses!!.isNotEmpty()) {
                        latitude = addresses!![0].latitude
                        longitude = addresses!![0].longitude
                    }
                }
            }

            try {
                if (latitude != null && longitude != null) {
                    latitudeInputEditText.setText(latitude.toString())
                    longitudeInputEditText.setText(longitude.toString())
                } else {
                    latitudeInputEditText.text?.clear()
                    longitudeInputEditText.text?.clear()
                }
                loadingProgressBar.hide()
            } catch (ignored: NullPointerException) {
            } catch (ignored: UninitializedPropertyAccessException) {
            }
        }
    }

    private val simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object
        : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
            //Remove swiped item from list and notify the RecyclerView
            val p0 = locationsAdapter.removeItem(viewHolder.absoluteAdapterPosition)

            CoroutineScope(Dispatchers.Default).launch {
                val db = Room.databaseBuilder(requireContext(), LocationDatabase::class.java, "locations.db").build()
                db.locationDao()?.deleteLocation(p0)
                if (db.locationDao()!!.getAllLocations().isEmpty()) {
                    handler.post {
                        art.makeVisible(true)
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