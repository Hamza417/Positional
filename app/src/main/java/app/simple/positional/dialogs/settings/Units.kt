package app.simple.positional.dialogs.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import app.simple.positional.R
import app.simple.positional.preference.MainPreferences
import app.simple.positional.ui.AppSettings
import app.simple.positional.views.CustomBottomSheetDialogFragment
import java.lang.ref.WeakReference

class Units(private val weakReference: WeakReference<AppSettings>) : CustomBottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
    }

    private lateinit var metric: RadioButton
    private lateinit var imperial: RadioButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_unit_menu, container, false)

        metric = view.findViewById(R.id.unit_metric)
        imperial = view.findViewById(R.id.unit_imperial)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setButtons(MainPreferences.getUnit())

        metric.setOnCheckedChangeListener { _, isChecked ->
            setButtons(isChecked)
            weakReference.get()?.setCurrentUnit(isChecked)
        }

        imperial.setOnCheckedChangeListener { _, isChecked ->
            setButtons(!isChecked)
            weakReference.get()?.setCurrentUnit(!isChecked)
        }
    }

    private fun setButtons(value: Boolean) {
        MainPreferences.setUnit(value)

        metric.isChecked = value
        imperial.isChecked = !value
    }
}