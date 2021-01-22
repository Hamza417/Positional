package app.simple.positional.dialogs.settings

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
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
import android.widget.Button
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.core.widget.ContentLoadingProgressBar
import androidx.room.Room
import app.simple.positional.R
import app.simple.positional.callbacks.CoordinatesCallback
import app.simple.positional.callbacks.LocationAdapterCallback
import app.simple.positional.callbacks.TimeZoneSelected
import app.simple.positional.database.LocationDatabase
import app.simple.positional.math.TimeConverter.isValidTimeZone
import app.simple.positional.model.Locations
import app.simple.positional.preference.MainPreferences
import app.simple.positional.util.resolveAttrColor
import app.simple.positional.views.CustomDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
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
    private lateinit var setCoordinatesButton: Button
    private lateinit var cancel: Button
    private lateinit var addressInputEditText: TextInputEditText
    private lateinit var latitudeInputEditText: TextInputEditText
    private lateinit var longitudeInputEditText: TextInputEditText
    private lateinit var timezoneInputEditText: TextInputEditText
    private lateinit var latitudeInputLayout: TextInputLayout
    private lateinit var longitudeInputLayout: TextInputLayout
    private lateinit var timezoneInputLayout: TextInputLayout

    var coordinatesCallback: CoordinatesCallback? = null
    private var address = ""
    private var isCoordinateSet = false
    private var isValidTimeZone = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
    }

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
            HtmlViewer().newInstance("Custom Coordinates Help").show(childFragmentManager, "help")
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
            try {
                MainPreferences.setLatitude(latitudeInputEditText.text.toString().toFloat())
                MainPreferences.setLongitude(longitudeInputEditText.text.toString().toFloat())
                isCoordinateSet = true
            } catch (e: java.lang.NumberFormatException) {
                isCoordinateSet = false
                this.dialog?.dismiss()
            } catch (e: java.lang.NullPointerException) {
                isCoordinateSet = false
                this.dialog?.dismiss()
            }
            coordinatesCallback?.isCoordinatesSet(true)
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
        return (longitude >= -180 && longitude <= 180)
    }

    private fun isValidLatitude(latitude: Double): Boolean {
        return (latitude >= -90 && latitude <= 90)
    }

    private fun getCoordinatesFromAddress(address: String) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                addressIndicator.show()
            }

            val geocoder = Geocoder(requireContext())
            val addresses: MutableList<Address>?
            var latitude: Double? = null
            var longitude: Double? = null

            MainPreferences.setAddress(address)

            try {
                @Suppress("BlockingMethodInNonBlockingContext")
                /**
                 * [Dispatchers.IO] can withstand blocking calls
                 */
                addresses = geocoder.getFromLocationName(address, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    latitude = addresses[0].latitude
                    longitude = addresses[0].longitude
                }
            } catch (e: IOException) {
            } catch (e: NullPointerException) {
            }
            withContext(Dispatchers.Main) {
                try {
                    if (latitude != null && longitude != null) {
                        latitudeInputEditText.setText(latitude.toString())
                        longitudeInputEditText.setText(longitude.toString())
                    } else {
                        latitudeInputEditText.text?.clear()
                        longitudeInputEditText.text?.clear()
                    }
                    addressIndicator.hide()
                } catch (e: NullPointerException) {
                } catch (e: UninitializedPropertyAccessException) {
                }
            }
        }
    }

    private fun setButton() {
        try {
            setCoordinatesButton.isClickable =
                    (isValidLatitude(latitudeInputEditText.text.toString().toDouble()) &&
                            isValidLongitude(longitudeInputEditText.text.toString().toDouble()) &&
                            isValidTimeZone)

            if (setCoordinatesButton.isClickable) {
                setCoordinatesButton.setTextColor(requireContext().resolveAttrColor(R.attr.textPrimary))
            } else {
                setCoordinatesButton.setTextColor(Color.DKGRAY)
            }
        } catch (e: NumberFormatException) {
            setCoordinatesButton.setTextColor(Color.DKGRAY)
            setCoordinatesButton.isClickable = false
        } catch (e: NullPointerException) {
            setCoordinatesButton.setTextColor(Color.DKGRAY)
            setCoordinatesButton.isClickable = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
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
        CoroutineScope(Dispatchers.Default).launch {
            MainPreferences.setCustomCoordinates(isCoordinateSet)
            if (isCoordinateSet && isValidTimeZone) {
                MainPreferences.setTimeZone(timezoneInputEditText.text.toString())

                if (latitudeInputEditText.text.toString().isNotEmpty() || longitudeInputEditText.text.toString().isNotEmpty() || timezoneInputEditText.text.toString().isNotEmpty()) {
                    val db = Room.databaseBuilder(requireContext(), LocationDatabase::class.java, "locations.db").build()
                    val locations = Locations()

                    try {
                        locations.address = addressInputEditText.text.toString()
                        locations.latitude = latitudeInputEditText.text.toString().toDouble()
                        locations.longitude = longitudeInputEditText.text.toString().toDouble()
                        locations.timeZone = timezoneInputEditText.text.toString()
                        locations.date = System.currentTimeMillis()
                        db.locationDao()?.insetLocation(location = locations)
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                    } finally {
                        db.close()
                    }
                }
            }

            withContext(Dispatchers.Main) {
                coordinatesCallback?.isCoordinatesSet(isCoordinateSet)
                dialog?.dismiss()
            }
        }
    }
}
