package app.simple.positional.dialogs.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.positional.R
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.decorations.views.CustomRadioButton
import app.simple.positional.preferences.MainPreferences

class Units : CustomBottomSheetDialogFragment() {

    private lateinit var metric: CustomRadioButton
    private lateinit var imperial: CustomRadioButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_unit_menu, container, false)

        metric = view.findViewById(R.id.unit_metric)
        imperial = view.findViewById(R.id.unit_imperial)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setButtons(MainPreferences.isMetric())

        metric.setOnCheckedChangeListener { _, isChecked ->
            setButtons(isChecked)
        }

        imperial.setOnCheckedChangeListener { _, isChecked ->
            setButtons(!isChecked)
        }
    }

    private fun setButtons(value: Boolean) {
        MainPreferences.setUnit(value)

        metric.isChecked = value
        imperial.isChecked = !value
    }
}