package app.simple.positional.dialogs.direction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleButton
import app.simple.positional.decorations.views.CustomDialogFragment
import app.simple.positional.preferences.DirectionPreferences
import app.simple.positional.util.LocationExtension
import app.simple.positional.util.ViewUtils.gone
import app.simple.positional.util.ViewUtils.visible

class DirectionTarget : CustomDialogFragment() {

    private lateinit var label: EditText
    private lateinit var latitude: EditText
    private lateinit var longitude: EditText
    private lateinit var save: DynamicRippleButton
    private lateinit var cancel: DynamicRippleButton

    private var isValidLatitude = false
    private var isValidLongitude = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_direction_target, container, false)

        label = view.findViewById(R.id.target_label)
        latitude = view.findViewById(R.id.target_latitude)
        longitude = view.findViewById(R.id.target_longitude)
        save = view.findViewById(R.id.save)
        cancel = view.findViewById(R.id.cancel)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        label.setText(DirectionPreferences.getTargetLabel())

        label.doOnTextChanged { _, _, _, _ ->
            saveButtonState()
        }

        latitude.doOnTextChanged { text, _, _, _ ->
            isValidLatitude = kotlin.runCatching {
                LocationExtension.isValidLatitude(text.toString().toDouble())
            }.getOrElse {
                false
            }

            saveButtonState()
        }

        longitude.doOnTextChanged { text, _, _, _ ->
            isValidLongitude = kotlin.runCatching {
                LocationExtension.isValidLongitude(text.toString().toDouble())
            }.getOrElse {
                false
            }

            saveButtonState()
        }

        with(DirectionPreferences.getTargetCoordinates()) {
            latitude.setText(this[0].toString())
            longitude.setText(this[1].toString())
        }

        save.setOnClickListener {
            DirectionPreferences.setTargetLatitude(latitude.text.toString().toFloat())
            DirectionPreferences.setTargetLongitude(longitude.text.toString().toFloat())
            DirectionPreferences.setTargetLabel(label.text.toString())
            dismiss()
        }

        cancel.setOnClickListener {
            dismiss()
        }
    }

    private fun saveButtonState() {
        if (isValidLatitude && isValidLongitude && label.text.isNotEmpty()) {
            save.visible(false)
        } else {
            save.gone()
        }
    }

    companion object {
        fun newInstance(): DirectionTarget {
            val args = Bundle()
            val fragment = DirectionTarget()
            fragment.arguments = args
            return fragment
        }
    }
}