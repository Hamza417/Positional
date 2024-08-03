package app.simple.positional.dialogs.trail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleButton
import app.simple.positional.decorations.views.CustomDialogFragment
import app.simple.positional.model.TrailEntry
import app.simple.positional.preferences.TrailPreferences
import app.simple.positional.util.ConditionUtils.isZero
import app.simple.positional.util.ViewUtils.gone
import app.simple.positional.util.ViewUtils.visible
import app.simple.positional.viewmodels.viewmodel.TrailsViewModel

class AddTrail : CustomDialogFragment() {

    private lateinit var nameInputEditText: EditText
    private lateinit var noteInputEditText: EditText
    private lateinit var save: DynamicRippleButton
    private lateinit var cancel: DynamicRippleButton

    private val trailsViewModel: TrailsViewModel by viewModels()
    private var list = arrayListOf<TrailEntry>()

    var onNewTrailAddedSuccessfully: (trailEntry: TrailEntry) -> Unit = {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_trail_name, container, false)

        nameInputEditText = view.findViewById(R.id.trail_name_edit_text)
        noteInputEditText = view.findViewById(R.id.note)
        save = view.findViewById(R.id.save)
        cancel = view.findViewById(R.id.cancel)

        nameInputEditText.setText(TrailPreferences.getLastTrailName())
        noteInputEditText.setText(TrailPreferences.getLastTrailNote())

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

        trailsViewModel.trails.observe(viewLifecycleOwner, {
            list.addAll(it)
        })

        if (TrailPreferences.getLastTrailName().isNotBlank()) {
            saveButtonState(TrailPreferences.getLastTrailName())
        }

        nameInputEditText.doOnTextChanged { text, _, _, _ ->
            kotlin.runCatching {
                if (list.isEmpty()) {
                    saveButtonState(text!!.toString())
                } else {
                    for (i in list) {
                        if (i.trailName.lowercase() == text.toString().lowercase() || text.toString().lowercase() == "%%_trails" || text?.length!!.isZero()) {
                            save.gone()
                            break
                        } else {
                            save.visible(true)
                        }
                    }
                }
            }

            TrailPreferences.setLastTrailName(text.toString())
        }

        noteInputEditText.doOnTextChanged { text, _, _, _ ->
            TrailPreferences.setLastTrailNote(text.toString())
        }

        save.setOnClickListener {
            val trails = TrailEntry(
                System.currentTimeMillis(),
                nameInputEditText.text.toString(),
                if (noteInputEditText.text.toString().isNotEmpty()) {
                    noteInputEditText.text.toString()
                } else {
                    getString(R.string.not_available)
                }
            )

            TrailPreferences.setLastTrailNote("")
            TrailPreferences.setLastTrailName("")

            dismiss()

            onNewTrailAddedSuccessfully.invoke(trails)
        }

        cancel.setOnClickListener {
            dismiss()
        }
    }

    private fun saveButtonState(text: String) {
        if (text.lowercase() == "%%_trails" || text.length.isZero()) {
            save.gone()
        } else {
            save.visible(true)
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
