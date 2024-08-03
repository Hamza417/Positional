package app.simple.positional.dialogs.trail

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import app.simple.positional.R
import app.simple.positional.constants.TrailIcons
import app.simple.positional.decorations.ripple.DynamicRippleButton
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.decorations.views.CustomDialogFragment
import app.simple.positional.model.TrailPoint
import app.simple.positional.popups.trail.PopupMarkers
import app.simple.positional.preferences.TrailPreferences
import app.simple.positional.util.ParcelUtils.parcelable
import com.google.android.gms.maps.model.LatLng

class AddMarker : CustomDialogFragment() {

    private lateinit var nameInputEditText: EditText
    private lateinit var noteInputEditText: EditText
    private lateinit var icon: DynamicRippleImageButton
    private lateinit var save: DynamicRippleButton
    private lateinit var cancel: DynamicRippleButton

    var onNewTrailAddedSuccessfully: (trailPoint: TrailPoint) -> Unit = {}

    private var latLng: LatLng? = null
    private var accuracy: Float = -1F
    private var iconPosition = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_on_trail_add, container, false)

        nameInputEditText = view.findViewById(R.id.name)
        noteInputEditText = view.findViewById(R.id.note)
        icon = view.findViewById(R.id.icon)
        save = view.findViewById(R.id.save)
        cancel = view.findViewById(R.id.cancel)

        latLng = requireArguments().parcelable("latlng")
        accuracy = requireArguments().getFloat("accuracy")
        setMarkerIcon(requireArguments().getInt("icon_position"))

        nameInputEditText.setText(TrailPreferences.getLastMarkerName())
        noteInputEditText.setText(TrailPreferences.getLastMarkerNote())

        kotlin.runCatching {
            nameInputEditText.requestFocus()
            dialog!!.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }.getOrElse {
            nameInputEditText.clearFocus()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager) {
            @Suppress("DEPRECATION")
            showSoftInput(nameInputEditText, InputMethodManager.SHOW_FORCED)
        }

        nameInputEditText.doOnTextChanged { text, _, _, _ ->
            TrailPreferences.setLastMarkerName(text.toString())
        }

        noteInputEditText.doOnTextChanged { text, _, _, _ ->
            TrailPreferences.setLastMarkerNote(text.toString())
        }

        icon.setOnClickListener {
            val popup = PopupMarkers(
                    icon, it.x, it.y)

            popup.setOnPopupMarkersCallbackListener(object : PopupMarkers.Companion.PopupMarkersCallbacks {
                override fun onMarkerClicked(position: Int) {
                    setMarkerIcon(position)
                }

                override fun onMarkerLongClicked(position: Int) {
                    setMarkerIcon(position)
                }
            })
        }

        save.setOnClickListener {
            val trails = TrailPoint(
                latLng!!.latitude,
                latLng!!.longitude,
                requireArguments().getLong("time"),
                iconPosition,
                if (noteInputEditText.text.toString().isNotEmpty()) {
                    noteInputEditText.text.toString()
                } else {
                    null
                },
                if (nameInputEditText.text.toString().isNotEmpty()) {
                    nameInputEditText.text.toString()
                } else {
                    null
                },
                accuracy
            )

            TrailPreferences.setLastMarkerName("")
            TrailPreferences.setLastMarkerNote("")

            dismiss()

            onNewTrailAddedSuccessfully.invoke(trails)
        }

        cancel.setOnClickListener {
            dismiss()
        }
    }

    private fun setMarkerIcon(position: Int) {
        requireArguments().putInt("icon_position", position)
        iconPosition = position
        icon.setImageResource(TrailIcons.icons[position])
    }

    companion object {
        fun newInstance(position: Int, latLng: LatLng, accuracy: Float): AddMarker {
            val args = Bundle()
            args.putInt("icon_position", position)
            args.putParcelable("latlng", latLng)
            args.putFloat("accuracy", accuracy)
            args.putLong("time", System.currentTimeMillis())
            val fragment = AddMarker()
            fragment.arguments = args
            return fragment
        }
    }
}
