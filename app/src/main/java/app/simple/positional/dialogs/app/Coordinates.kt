package app.simple.positional.dialogs.app

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import app.simple.positional.R
import app.simple.positional.callbacks.CoordinatesCallback
import app.simple.positional.preference.MainPreferences
import app.simple.positional.util.resolveAttrColor
import app.simple.positional.views.CustomDialogFragment
import kotlinx.android.synthetic.main.dialog_input_coordinates.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class Coordinates : CustomDialogFragment() {
    fun newInstance(): Coordinates {
        return Coordinates()
    }

    var coordinatesCallback: CoordinatesCallback? = null
    private var isCoordinateSet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_input_coordinates, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        address_indicator.hide()
        setButton()

        address_input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                getCoordinatesFromAddress(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        latitude_input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                /* no-op */
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    if (isValidLatitude(s.toString().toDouble())) {
                        latitude_container.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.valid_input)
                        latitude_container.hintTextColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.valid_input))
                    } else {
                        throw NumberFormatException()
                    }
                } catch (e: NumberFormatException) {
                    latitude_container.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.invalid_input)
                    latitude_container.hintTextColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.invalid_input))
                }

                setButton()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        longitude_input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                /* no-op */
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    if (isValidLongitude(s.toString().toDouble())) {
                        longitude_container.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.valid_input)
                        longitude_container.hintTextColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.valid_input))
                    } else {
                        throw NumberFormatException()
                    }
                } catch (e: NumberFormatException) {
                    longitude_container.boxStrokeColor = ContextCompat.getColor(requireContext(), R.color.invalid_input)
                    longitude_container.hintTextColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.invalid_input))
                }

                setButton()
            }

            override fun afterTextChanged(s: Editable?) {

            }

        })

        set_coordinates.setOnClickListener {
            try {
                MainPreferences().setLatitude(requireContext(), latitude_input.text.toString().toFloat())
                MainPreferences().setLongitude(requireContext(), longitude_input.text.toString().toFloat())
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

        cancel_coordinate_input.setOnClickListener {
            isCoordinateSet = MainPreferences().isCustomCoordinate(requireContext())
            this.dialog?.dismiss()
        }
    }

    private fun isValidLongitude(longitude: Double): Boolean {
        return (longitude >= -180 && longitude <= 180)
    }

    private fun isValidLatitude(latitude: Double): Boolean {
        return (latitude >= -90 && latitude <= 90)
    }

    private fun getCoordinatesFromAddress(address: String) {
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                address_indicator.show()
            }

            val geocoder = Geocoder(requireContext())
            var addresses: MutableList<Address>? = null
            var latitude: Double? = null
            var longitude: Double? = null

            try {
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
                        latitude_input.setText(latitude.toString())
                        longitude_input.setText(longitude.toString())
                    } else {
                        latitude_input.text?.clear()
                        longitude_input.text?.clear()
                    }
                    address_indicator.hide()
                } catch (e: NullPointerException) {
                } catch (e: UninitializedPropertyAccessException) {
                }
            }
        }
    }

    private fun setButton() {
        try {
            set_coordinates.isClickable =
                    (isValidLatitude(latitude_input.text.toString().toDouble()) &&
                            isValidLongitude(longitude_input.text.toString().toDouble()))

            if (set_coordinates.isClickable) {
                set_coordinates.setTextColor(requireContext().resolveAttrColor(R.attr.textPrimary))
            } else {
                set_coordinates.setTextColor(requireContext().resolveAttrColor(R.attr.indicator))
            }
        } catch (e: NumberFormatException) {
            set_coordinates.setTextColor(requireContext().resolveAttrColor(R.attr.indicator))
            set_coordinates.isClickable = false
        } catch (e: NullPointerException) {
            set_coordinates.setTextColor(requireContext().resolveAttrColor(R.attr.indicator))
            set_coordinates.isClickable = false
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        MainPreferences().setCustomCoordinates(requireContext(), isCoordinateSet)
        coordinatesCallback?.isCoordinatesSet(isCoordinateSet)
    }
}