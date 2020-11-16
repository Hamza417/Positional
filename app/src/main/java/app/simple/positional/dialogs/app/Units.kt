package app.simple.positional.dialogs.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.positional.R
import app.simple.positional.preference.MainPreferences
import app.simple.positional.ui.AppSettings
import app.simple.positional.views.CustomBottomSheetDialog
import kotlinx.android.synthetic.main.dialog_unit_menu.*
import java.lang.ref.WeakReference

class Units(private val weakReference: WeakReference<AppSettings>) : CustomBottomSheetDialog() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_unit_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setButtons(MainPreferences().getUnit(requireContext()))

        unit_metric.setOnCheckedChangeListener { _, isChecked ->
            setButtons(isChecked)
            weakReference.get()?.setCurrentUnit(isChecked)
        }

        unit_imperial.setOnCheckedChangeListener { _, isChecked ->
            setButtons(!isChecked)
            weakReference.get()?.setCurrentUnit(!isChecked)
        }
    }

    private fun setButtons(value: Boolean) {
        MainPreferences().setUnit(requireContext(), value)

        unit_metric.isChecked = value
        unit_imperial.isChecked = !value
    }
}