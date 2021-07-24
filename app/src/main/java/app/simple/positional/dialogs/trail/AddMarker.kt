package app.simple.positional.dialogs.trail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleButton
import app.simple.positional.decorations.views.CustomDialogFragment
import app.simple.positional.model.TrailData
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddMarker : CustomDialogFragment() {

    private lateinit var nameInputLayout: TextInputLayout
    private lateinit var nameInputEditText: TextInputEditText
    private lateinit var noteInputLayout: TextInputLayout
    private lateinit var noteInputEditText: TextInputEditText
    private lateinit var save: DynamicRippleButton
    private lateinit var cancel: DynamicRippleButton

    var onNewTrailAddedSuccessfully: (trailData: TrailData) -> Unit = {}

    private var latLng: LatLng? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_on_trail_add, container, false)

        nameInputEditText = view.findViewById(R.id.trail_name_edit_text)
        noteInputEditText = view.findViewById(R.id.trail_note_edit_text)
        nameInputLayout = view.findViewById(R.id.trail_name_edit_text_layout)
        noteInputLayout = view.findViewById(R.id.trail_note_edit_text_layout)
        save = view.findViewById(R.id.save)
        cancel = view.findViewById(R.id.cancel)

        latLng = requireArguments().getParcelable("latlng")

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        save.setOnClickListener {
            val trails = TrailData(
                    latLng!!.latitude,
                    latLng!!.longitude,
                    requireArguments().getLong("time"),
                    requireArguments().getInt("icon_position"),
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

            dismiss()

            onNewTrailAddedSuccessfully.invoke(trails)
        }

        cancel.setOnClickListener {
            dismiss()
        }
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
