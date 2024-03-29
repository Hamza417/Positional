package app.simple.positional.dialogs.gps

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import app.simple.positional.R
import app.simple.positional.activities.subactivity.MapSearchActivity
import app.simple.positional.decorations.ripple.DynamicRippleButton
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.decorations.views.CustomDialogFragment
import app.simple.positional.preferences.GPSPreferences
import app.simple.positional.util.LocationExtension
import app.simple.positional.util.ViewUtils.gone
import app.simple.positional.util.ViewUtils.visible

class TargetCoordinates : CustomDialogFragment() {

    private lateinit var latitude: EditText
    private lateinit var longitude: EditText
    private lateinit var search: DynamicRippleImageButton
    private lateinit var save: DynamicRippleButton
    private lateinit var cancel: DynamicRippleButton

    private var isValidLatitude = false
    private var isValidLongitude = false

    private val registerForActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                result.data?.let { intent ->
                    intent.getStringExtra("address")?.let { _ ->
                        intent.getDoubleExtra("latitude", 0.0).let { latitude ->
                            intent.getDoubleExtra("longitude", 0.0).let { longitude ->
                                this.latitude.setText(latitude.toString())
                                this.longitude.setText(longitude.toString())
                            }
                        }
                    }
                }
            }

            Activity.RESULT_CANCELED -> {
                Log.d("TargetCoordinates", "Search cancelled")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_target_location, container, false)

        latitude = view.findViewById(R.id.target_latitude)
        longitude = view.findViewById(R.id.target_longitude)
        save = view.findViewById(R.id.save)
        search = view.findViewById(R.id.search)
        cancel = view.findViewById(R.id.cancel)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        search.setOnClickListener {
            registerForActivityResult.launch(Intent(requireContext(), MapSearchActivity::class.java))
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

        with(GPSPreferences.getTargetMarkerCoordinates()) {
            latitude.setText(this[0].toString())
            longitude.setText(this[1].toString())
        }

        save.setOnClickListener {
            GPSPreferences.setTargetMarkerLatitude(latitude.text.toString().toFloat())
            GPSPreferences.setTargetMarkerLongitude(longitude.text.toString().toFloat())
            GPSPreferences.setTargetMarker(true)
            dismiss()
        }

        cancel.setOnClickListener {
            dismiss()
        }
    }

    private fun saveButtonState() {
        if (isValidLatitude && isValidLongitude) {
            save.visible(false)
        } else {
            save.gone()
        }
    }

    companion object {
        fun newInstance(): TargetCoordinates {
            val args = Bundle()
            val fragment = TargetCoordinates()
            fragment.arguments = args
            return fragment
        }
    }
}