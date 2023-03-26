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
import app.simple.positional.model.DirectionModel
import app.simple.positional.preferences.DirectionPreferences
import app.simple.positional.util.ConditionUtils.isNull
import app.simple.positional.util.LocationExtension
import app.simple.positional.util.ParcelUtils.parcelable
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

    private var directionModel: DirectionModel? = null
    private var directionTargetCallbacks: DirectionTargetCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_direction_target, container, false)

        label = view.findViewById(R.id.target_label)
        latitude = view.findViewById(R.id.target_latitude)
        longitude = view.findViewById(R.id.target_longitude)
        save = view.findViewById(R.id.save)
        cancel = view.findViewById(R.id.cancel)

        directionModel = requireArguments().parcelable("DirectionModel")

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        save.setOnClickListener {
            if (directionModel.isNull()) {
                directionModel = DirectionModel()
                directionModel?.dateAdded = System.currentTimeMillis()
            }

            directionModel?.latitude = DirectionPreferences.setTargetLatitude(latitude.text.toString().toFloat()).toDouble()
            directionModel?.longitude = DirectionPreferences.setTargetLongitude(longitude.text.toString().toFloat()).toDouble()
            directionModel?.name = DirectionPreferences.setTargetLabel(label.text.toString())

            directionTargetCallbacks?.onDirectionAdded(directionModel!!).also {
                dismiss()
            }
        }

        cancel.setOnClickListener {
            dismiss()
        }

        if (directionModel.isNull()) {
            with(DirectionPreferences.getTargetCoordinates()) {
                latitude.setText(this[0].toString())
                longitude.setText(this[1].toString())
            }

            label.setText(DirectionPreferences.getTargetLabel())
        } else {
            latitude.setText(directionModel?.latitude?.toString())
            longitude.setText(directionModel?.longitude?.toString())
            label.setText(directionModel?.name)
        }
    }

    private fun saveButtonState() {
        if (isValidLatitude && isValidLongitude && label.text.isNotEmpty()) {
            save.visible(false)
        } else {
            save.gone()
        }
    }

    fun setOnDirectionTargetListener(directionTargetCallbacks: DirectionTargetCallbacks) {
        this.directionTargetCallbacks = directionTargetCallbacks
    }

    companion object {
        fun newInstance(directionModel: DirectionModel? = null): DirectionTarget {
            val args = Bundle()
            args.putParcelable("DirectionModel", directionModel)
            val fragment = DirectionTarget()
            fragment.arguments = args
            return fragment
        }

        interface DirectionTargetCallbacks {
            fun onDirectionAdded(directionModel: DirectionModel)
        }
    }
}