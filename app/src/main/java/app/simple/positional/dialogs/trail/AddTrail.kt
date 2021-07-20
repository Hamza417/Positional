package app.simple.positional.dialogs.trail

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleButton
import app.simple.positional.decorations.views.CustomDialogFragment
import app.simple.positional.model.TrailModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddTrail : CustomDialogFragment() {

    private lateinit var nameInputLayout: TextInputLayout
    private lateinit var nameInputEditText: TextInputEditText
    private lateinit var noteInputLayout: TextInputLayout
    private lateinit var noteInputEditText: TextInputEditText
    private lateinit var save: DynamicRippleButton
    private lateinit var cancel: DynamicRippleButton

    var onNewTrailAddedSuccessfully: (trailModel: TrailModel) -> Unit = {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_trail_name, container, false)

        nameInputEditText = view.findViewById(R.id.trail_name_edit_text)
        noteInputEditText = view.findViewById(R.id.trail_note_edit_text)
        nameInputLayout = view.findViewById(R.id.trail_name_edit_text_layout)
        noteInputLayout = view.findViewById(R.id.trail_note_edit_text_layout)
        save = view.findViewById(R.id.save)
        cancel = view.findViewById(R.id.cancel)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                save.isClickable = s.isNotEmpty()
                save.isEnabled = s.isNotEmpty()
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        save.setOnClickListener {
            val trails = TrailModel(
                    System.currentTimeMillis(),
                    nameInputEditText.text.toString(),
                    if (noteInputEditText.text.toString().isNotEmpty()) {
                        noteInputEditText.text.toString()
                    } else {
                        getString(R.string.not_available)
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
        fun newInstance(): AddTrail {
            val args = Bundle()
            val fragment = AddTrail()
            fragment.arguments = args
            return fragment
        }
    }
}
