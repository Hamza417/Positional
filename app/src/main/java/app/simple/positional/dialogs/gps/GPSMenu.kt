package app.simple.positional.dialogs.gps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.positional.R
import app.simple.positional.preference.GPSPreferences
import app.simple.positional.ui.GPS
import app.simple.positional.views.CustomBottomSheetDialog
import kotlinx.android.synthetic.main.dialog_gps_menu.*
import java.lang.ref.WeakReference

class GPSMenu(private val weakReference: WeakReference<GPS>) : CustomBottomSheetDialog() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_gps_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toggle_label.isChecked = GPSPreferences().isLabelOn(requireContext())
        toggle_satellite.isChecked = GPSPreferences().isSatelliteOn(requireContext())

        toggle_label.setOnCheckedChangeListener { _, isChecked ->
            weakReference.get()?.showLabel(isChecked)
        }

        toggle_satellite.setOnCheckedChangeListener { _, isChecked ->
            GPSPreferences().setSatelliteMode(requireContext(), isChecked)
            weakReference.get()?.setSatellite(isChecked)
            toggle_label.isClickable = !isChecked
        }
    }
}
