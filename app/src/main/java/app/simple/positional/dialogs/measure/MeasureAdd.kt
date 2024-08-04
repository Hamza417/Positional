package app.simple.positional.dialogs.measure

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleButton
import app.simple.positional.decorations.views.CustomDialogFragment
import app.simple.positional.util.ViewUtils.setVisibility

class MeasureAdd : CustomDialogFragment() {

    private lateinit var name: EditText
    private lateinit var note: EditText
    private lateinit var save: DynamicRippleButton
    private lateinit var cancel: DynamicRippleButton

    private lateinit var callbacks: MeasureAddCallbacks

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_measure_add, container, false)

        name = view.findViewById(R.id.name_edit_text)
        note = view.findViewById(R.id.note)
        save = view.findViewById(R.id.save)
        cancel = view.findViewById(R.id.cancel)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        name.doOnTextChanged { text, _, _, _ ->
            save.setVisibility(text.toString().isNotEmpty())
            save.isEnabled = text.toString().isNotEmpty()
        }

        save.setOnClickListener {
            val name = name.text.toString()
            val note = note.text.toString()
            callbacks.onSave(name, note)
            dismiss()
        }

        cancel.setOnClickListener {
            dismiss()
        }
    }

    fun setOnMeasureAddCallbacks(callbacks: MeasureAddCallbacks) {
        this.callbacks = callbacks
    }

    companion object {
        fun newInstance(): MeasureAdd {
            val args = Bundle()
            val fragment = MeasureAdd()
            fragment.arguments = args
            return fragment
        }

        fun Fragment.showMeasureAdd(): MeasureAdd {
            val dialog = newInstance()
            dialog.show(childFragmentManager, TAG)
            return dialog
        }

        interface MeasureAddCallbacks {
            fun onSave(name: String, note: String)
        }

        const val TAG = "MeasureAdd"
    }
}
