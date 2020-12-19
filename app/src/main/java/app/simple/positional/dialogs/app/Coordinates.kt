package app.simple.positional.dialogs.app

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
import android.widget.Button
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.core.widget.ContentLoadingProgressBar
import app.simple.positional.R
import app.simple.positional.callbacks.CoordinatesCallback
import app.simple.positional.callbacks.TimeZoneSelected
import app.simple.positional.preference.MainPreferences
import app.simple.positional.util.isValidTimeZone
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

class Coordinates : CustomDialogFragment(), TimeZoneSelected {

    private val handler = Handler(Looper.getMainLooper())

    fun newInstance(): Coordinates {
        return Coordinates()
    }

    private lateinit var addressIndicator: ContentLoadingProgressBar
    private lateinit var timezoneList: ImageButton
    private lateinit var setCoordinates: Button
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
        setCoordinates = view.findViewById(R.id.set_coordinates)
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

        if (MainPreferences().isCustomCoordinate(requireContext())) {
            timezoneInputEditText.setText(MainPreferences().getTimeZone(requireContext()))
            isValidTimeZone = isValidTimeZone(timezoneInputEditText.text.toString())
            addressInputEditText.setText(MainPreferences().getAddress(requireContext()))
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

        setCoordinates.setOnClickListener {
            try {
                MainPreferences().setLatitude(requireContext(), latitudeInputEditText.text.toString().toFloat())
                MainPreferences().setLongitude(requireContext(), longitudeInputEditText.text.toString().toFloat())
                isCoordinateSet = true
            } catch (e: java.lang.NumberFormatException) {
                isCoordinateSet = false
                this.dialog?.dismiss()
            } catch (e: java.lang.NullPointerException) {
                isCoordinateSet = false
                this.dialog?.dismiss()
            }
            coordinatesCallback?.isCoordinatesSet(true)
            this.dialog?.dismiss()
        }

        cancel.setOnClickListener {
            isCoordinateSet = MainPreferences().isCustomCoordinate(requireContext())
            this.dialog?.dismiss()
        }
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

            MainPreferences().setAddress(requireContext(), address)

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
            setCoordinates.isClickable =
                    (isValidLatitude(latitudeInputEditText.text.toString().toDouble()) &&
                            isValidLongitude(longitudeInputEditText.text.toString().toDouble()) &&
                            isValidTimeZone)

            if (setCoordinates.isClickable) {
                setCoordinates.setTextColor(requireContext().resolveAttrColor(R.attr.textPrimary))
            } else {
                setCoordinates.setTextColor(requireContext().resolveAttrColor(R.attr.indicator))
            }
        } catch (e: NumberFormatException) {
            setCoordinates.setTextColor(requireContext().resolveAttrColor(R.attr.indicator))
            setCoordinates.isClickable = false
        } catch (e: NullPointerException) {
            setCoordinates.setTextColor(requireContext().resolveAttrColor(R.attr.indicator))
            setCoordinates.isClickable = false
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        MainPreferences().setCustomCoordinates(requireContext(), isCoordinateSet)
        if (isCoordinateSet && isValidTimeZone) {
            MainPreferences().setTimeZone(requireContext(), timezoneInputEditText.text.toString())
        }
        coordinatesCallback?.isCoordinatesSet(isCoordinateSet)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(geoCoderRunnable)
    }

    override fun onTimeZoneSelected(p0: String) {
        timezoneInputEditText.setText(p0)
    }
}