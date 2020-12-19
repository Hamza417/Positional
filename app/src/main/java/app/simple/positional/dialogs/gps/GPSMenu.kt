package app.simple.positional.dialogs.gps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import app.simple.positional.R
import app.simple.positional.preference.GPSPreferences
import app.simple.positional.ui.GPS
import app.simple.positional.views.CustomBottomSheetDialog
import java.lang.ref.WeakReference

class GPSMenu(private val weakReference: WeakReference<GPS>) : CustomBottomSheetDialog() {

    private lateinit var toggleLabel: SwitchCompat
    private lateinit var toggleSatellite: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_gps_menu, container, false)

        toggleLabel = view.findViewById(R.id.toggle_label)
        toggleSatellite = view.findViewById(R.id.toggle_satellite)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toggleLabel.isChecked = GPSPreferences().isLabelOn(requireContext())
        toggleSatellite.isChecked = GPSPreferences().isSatelliteOn(requireContext())

        toggleLabel.setOnCheckedChangeListener { _, isChecked ->
            weakReference.get()?.showLabel(isChecked)
        }

        toggleSatellite.setOnCheckedChangeListener { _, isChecked ->
            GPSPreferences().setSatelliteMode(requireContext(), isChecked)
            weakReference.get()?.setSatellite(isChecked)
            toggleLabel.isClickable = !isChecked
        }
    }
}
