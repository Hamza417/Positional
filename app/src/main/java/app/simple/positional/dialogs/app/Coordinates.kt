package app.simple.positional.dialogs.app

import android.content.res.ColorStateList
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

class Coordinates : CustomDialogFragment() {
    fun newInstance(): Coordinates {
        return Coordinates()
    }

    var coordinatesCallback: CoordinatesCallback? = null

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

        setButton()

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
                MainPreferences().setCustomCoordinates(requireContext(), true)
            } catch (e: java.lang.NumberFormatException) {
                coordinatesCallback?.onCoordinatesSet(false)
                this.dialog?.dismiss()
            } catch (e: java.lang.NullPointerException) {
                coordinatesCallback?.onCoordinatesSet(false)
                this.dialog?.dismiss()
            }
            coordinatesCallback?.onCoordinatesSet(true)
            this.dialog?.dismiss()
        }

        cancel_coordinate_input.setOnClickListener {
            coordinatesCallback?.onCancel()
            this.dialog?.dismiss()
        }
    }

    private fun isValidLongitude(longitude: Double): Boolean {
        return (longitude >= -180 && longitude <= 180)
    }

    private fun isValidLatitude(latitude: Double): Boolean {
        return (latitude >= -90 && latitude <= 90)
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
}