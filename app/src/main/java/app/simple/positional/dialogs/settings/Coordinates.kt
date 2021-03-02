package app.simple.positional.dialogs.settings

import android.content.DialogInterface
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
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.core.widget.ContentLoadingProgressBar
import androidx.room.Room
import app.simple.positional.R
import app.simple.positional.callbacks.CoordinatesCallback
import app.simple.positional.callbacks.LocationAdapterCallback
import app.simple.positional.callbacks.TimeZoneSelected
import app.simple.positional.corners.DynamicCornerButton
import app.simple.positional.database.LocationDatabase
import app.simple.positional.math.TimeConverter.isValidTimeZone
import app.simple.positional.model.Locations
import app.simple.positional.preference.MainPreferences
import app.simple.positional.views.CustomDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.ZoneOffset
import java.util.*

class Coordinates : CustomDialogFragment(), TimeZoneSelected, LocationAdapterCallback {

    private val handler = Handler(Looper.getMainLooper())

    fun newInstance(): Coordinates {
        return Coordinates()
    }

    private lateinit var addressIndicator: ContentLoadingProgressBar
    private lateinit var timezoneList: ImageButton
    private lateinit var savedLocations: ImageButton
    private lateinit var help: ImageButton
    private lateinit var setCoordinatesButton: DynamicCornerButton
    private lateinit var cancel: Button
    private lateinit var addressInputEditText: TextInputEditText
    private lateinit var latitudeInputEditText: TextInputEditText
    private lateinit var longitudeInputEditText: TextInputEditText
    private lateinit var timezoneInputEditText: AutoCompleteTextView
    private lateinit var latitudeInputLayout: TextInputLayout
    private lateinit var longitudeInputLayout: TextInputLayout
    private lateinit var timezoneInputLayout: TextInputLayout

    var coordinatesCallback: CoordinatesCallback? = null
    private var address = ""
    private var isCoordinateSet = false
    private var isValidTimeZone = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_input_coordinates, container, false)

        addressIndicator = view.findViewById(R.id.address_indicator)
        timezoneList = view.findViewById(R.id.timezone_list)
        savedLocations = view.findViewById(R.id.open_saved_locations)
        help = view.findViewById(R.id.help_custom_coordinates)
        setCoordinatesButton = view.findViewById(R.id.set_coordinates)
        cancel = view.findViewById(R.id.cancel_coordinate_input)
        addressInputEditText = view.findViewById(R.id.address_input)
        latitudeInputEditText = view.findViewById(R.id.latitude_input)
        longitudeInputEditText = view.findViewById(R.id.longitude_input)
        timezoneInputEditText = view.findViewById(R.id.timezone_input)
        latitudeInputLayout = view.findViewById(R.id.latitude_container)
        longitudeInputLayout = view.findViewById(R.id.longitude_container)
        timezoneInputLayout = view.findViewById(R.id.timezone_container)

        timezoneInputEditText.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, ZoneOffset.getAvailableZoneIds().toTypedArray()))

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addressIndicator.hide()
        setButton()

        if (MainPreferences.isCustomCoordinate()) {
            timezoneInputEditText.setText(MainPreferences.getTimeZone())
            isValidTimeZone = isValidTimeZone(timezoneInputEditText.text.toString())
            addressInputEditText.setText(MainPreferences.getAddress())
            address = addressInputEditText.text.toString()
            handler.postDelayed(geoCoderRunnable, 250)
        } else {
            timezoneInputEditText.setText(Calendar.getInstance().timeZone.id)
        }

        timezoneList.setOnClickListener {
            val timeZones = TimeZones().newInstance()
            timeZones.timeZoneSelected = this
            timeZones.show(childFragmentManager, "time_zones")
        }

        help.setOnClickListener {
            HtmlViewer.newInstance("Custom Coordinates Help").show(childFragmentManager, "help")
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

                setButton()
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

                setButton()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        timezoneInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                /* no-op */
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isValidTimeZone = isValidTimeZone(s.toString())
                if (isValidTimeZone) {
                    timezoneInputLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.valid_input)
                    timezoneInputLayout.hintTextColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.valid_input))
                } else {
                    timezoneInputLayout.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.invalid_input)
                    timezoneInputLayout.hintTextColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.invalid_input))
                }

                setButton()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        setCoordinatesButton.setOnClickListener {
            isCoordinateSet = try {
                MainPreferences.setLatitude(latitudeInputEditText.text.toString().toFloat())
                MainPreferences.setLongitude(longitudeInputEditText.text.toString().toFloat())
                true
            } catch (e: java.lang.NumberFormatException) {
                false
            } catch (e: java.lang.NullPointerException) {
                false
            }
            finish()
        }

        savedLocations.setOnClickListener {
            val savedLocations = SavedLocations().newInstance()
            savedLocations.locationAdapterCallback = this
            savedLocations.show(requireParentFragment().childFragmentManager, "saved_locations")
        }

        cancel.setOnClickListener {
            this.dialog?.dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        isCoordinateSet = MainPreferences.isCustomCoordinate()
        coordinatesCallback?.isCoordinatesSet(isCoordinateSet)
    }

    private val geoCoderRunnable: Runnable = Runnable { getCoordinatesFromAddress(address) }

    private fun isValidLongitude(longitude: Double): Boolean {
        return longitude >= -180 && longitude <= 180
    }

    private fun isValidLatitude(latitude: Double): Boolean {
        return latitude >= -90 && latitude <= 90
    }

    private fun getCoordinatesFromAddress(address: String) {
        launch {
            addressIndicator.show()

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
                addressIndicator.hide()
            } catch (ignored: NullPointerException) {
            } catch (ignored: UninitializedPropertyAccessException) {
            }
        }
    }

    private fun setButton() {
        try {
            setCoordinatesButton.isClickable =
                    isValidLatitude(latitudeInputEditText.text.toString().toDouble()) &&
                            isValidLongitude(longitudeInputEditText.text.toString().toDouble()) &&
                            isValidTimeZone

        } catch (e: NumberFormatException) {
            setCoordinatesButton.isClickable = false
        } catch (e: NullPointerException) {
            setCoordinatesButton.isClickable = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        setCoordinatesButton.clearAnimation()
        handler.removeCallbacks(geoCoderRunnable)
    }

    override fun onTimeZoneSelected(p0: String) {
        timezoneInputEditText.setText(p0)
    }

    override fun onLocationItemClicked(locations: Locations) {
        addressInputEditText.setText(locations.address)
        latitudeInputEditText.setText(locations.latitude.toString())
        longitudeInputEditText.setText(locations.longitude.toString())
        timezoneInputEditText.setText(locations.timeZone)
    }

    private fun finish() {
        launch {
            withContext(Dispatchers.Default) {
                if (isCoordinateSet && isValidTimeZone) {
                    MainPreferences.setTimeZone(timezoneInputEditText.text.toString())

                    if (latitudeInputEditText.text.toString().isNotEmpty() || longitudeInputEditText.text.toString().isNotEmpty() || timezoneInputEditText.text.toString().isNotEmpty()) {
                        val db = Room.databaseBuilder(requireContext(), LocationDatabase::class.java, "locations.db").fallbackToDestructiveMigration().build()
                        val locations = Locations()

                        try {
                            locations.address = addressInputEditText.text.toString()
                            locations.latitude = latitudeInputEditText.text.toString().toDouble()
                            locations.longitude = longitudeInputEditText.text.toString().toDouble()
                            locations.timeZone = timezoneInputEditText.text.toString()
                            locations.date = System.currentTimeMillis()
                            db.locationDao()?.insetLocation(location = locations)
                        } catch (e: NumberFormatException) {
                            isCoordinateSet = false
                        } finally {
                            MainPreferences.setCustomCoordinates(isCoordinateSet)
                            db.close()
                        }
                    }
                }
            }

            coordinatesCallback?.isCoordinatesSet(isCoordinateSet)
            this@Coordinates.dialog?.dismiss()
        }
    }
}
