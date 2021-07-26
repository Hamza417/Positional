package app.simple.positional.dialogs.trail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import app.simple.positional.R
import app.simple.positional.constants.TrailIcons
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout
import app.simple.positional.decorations.ripple.DynamicRippleButton
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.decorations.views.CustomDialogFragment
import app.simple.positional.model.TrailData
import app.simple.positional.popups.trail.PopupMarkers
import app.simple.positional.preferences.TrailPreferences
import com.google.android.gms.maps.model.LatLng

class AddMarker : CustomDialogFragment() {

    private lateinit var nameInputEditText: EditText
    private lateinit var noteInputEditText: EditText
    private lateinit var icon: DynamicRippleImageButton
    private lateinit var save: DynamicRippleButton
    private lateinit var cancel: DynamicRippleButton

    var onNewTrailAddedSuccessfully: (trailData: TrailData) -> Unit = {}

    private var latLng: LatLng? = null
    private var iconPosition = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_on_trail_add, container, false)

        nameInputEditText = view.findViewById(R.id.name)
        noteInputEditText = view.findViewById(R.id.note)
        icon = view.findViewById(R.id.icon)
        save = view.findViewById(R.id.save)
        cancel = view.findViewById(R.id.cancel)

        latLng = requireArguments().getParcelable("latlng")
        setMarkerIcon(requireArguments().getInt("icon_position"))

        nameInputEditText.setText(TrailPreferences.getLastMarkerName())
        noteInputEditText.setText(TrailPreferences.getLastMarkerNote())

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameInputEditText.doOnTextChanged { text, _, _, _ ->
            TrailPreferences.setLastMarkerName(text.toString())
        }

        noteInputEditText.doOnTextChanged { text, _, _, _ ->
            TrailPreferences.setLastMarkerNote(text.toString())
        }

        icon.setOnClickListener {
            val popup = PopupMarkers(
                    layoutInflater.inflate(R.layout.popup_trail_markers,
                                           DynamicCornerLinearLayout(requireContext())), save)

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
            val trails = TrailData(
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
                    }
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
        fun newInstance(position: Int, latLng: LatLng): AddMarker {
            val args = Bundle()
            args.putInt("icon_position", position)
            args.putParcelable("latlng", latLng)
            args.putLong("time", System.currentTimeMillis())
            val fragment = AddMarker()
            fragment.arguments = args
            return fragment
        }
    }
}
