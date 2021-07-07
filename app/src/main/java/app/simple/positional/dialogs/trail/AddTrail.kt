package app.simple.positional.dialogs.trail

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleButton
import app.simple.positional.decorations.views.CustomDialogFragment
import app.simple.positional.model.Trails
import app.simple.positional.viewmodels.viewmodel.TrailsViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddTrail : CustomDialogFragment() {

    private lateinit var textInputLayout: TextInputLayout
    private lateinit var textInputEditText: TextInputEditText
    private lateinit var save: DynamicRippleButton

    private val trailsViewModel: TrailsViewModel by viewModels()
    private val list = arrayListOf<Trails>()

    var onNewTrailAddedSuccessfully: () -> Unit = {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_trail_name, container, false)

        textInputEditText = view.findViewById(R.id.trail_name_edit_text)
        textInputLayout = view.findViewById(R.id.trail_name_edit_text_layout)
        save = view.findViewById(R.id.save)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        trailsViewModel.getTrails().observe(viewLifecycleOwner, {
            list.addAll(it)
        })

        textInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (list.isNotEmpty()) {
                    for (trail in list) {
                        if (trail.trailName == s.toString()) {
                            textInputLayout.isErrorEnabled = true
                            save.isClickable = false
                        } else {
                            textInputLayout.isErrorEnabled = false
                            save.isClickable = true
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        save.setOnClickListener {
            val trails = Trails(
                    System.currentTimeMillis(),
                    textInputEditText.text.toString(),
                    getString(R.string.not_available)
            )

            trailsViewModel.addTrail(trails)

            onNewTrailAddedSuccessfully.invoke()
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
