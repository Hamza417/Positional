package app.simple.positional.dialogs.gps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SwitchCompat
import app.simple.positional.R
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.preference.GPSPreferences

class GPSMenu : CustomBottomSheetDialogFragment() {

    private lateinit var toggleLabel: SwitchCompat
    private lateinit var toggleSatellite: SwitchCompat
    private lateinit var toggleHighContrast: SwitchCompat
    private lateinit var toggleBuilding: SwitchCompat

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_gps_menu, container, false)

        toggleLabel = view.findViewById(R.id.toggle_label)
        toggleSatellite = view.findViewById(R.id.toggle_satellite)
        toggleHighContrast = view.findViewById(R.id.toggle_high_contrast)
        toggleBuilding = view.findViewById(R.id.toggle_buildings)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toggleLabel.isChecked = GPSPreferences.isLabelOn()
        toggleSatellite.isChecked = GPSPreferences.isSatelliteOn()
        toggleHighContrast.isChecked = GPSPreferences.getHighContrastMap()
        toggleBuilding.isChecked = GPSPreferences.getShowBuildingsOnMap()

        toggleLabel.setOnCheckedChangeListener { _, isChecked ->
            GPSPreferences.setLabelMode(isChecked)
        }

        toggleSatellite.setOnCheckedChangeListener { _, isChecked ->
            GPSPreferences.setSatelliteMode(isChecked)
            toggleLabel.isClickable = !isChecked
        }

        toggleHighContrast.setOnCheckedChangeListener { _, isChecked ->
            GPSPreferences.setHighContrastMap(isChecked)
        }

        toggleBuilding.setOnCheckedChangeListener { _, isChecked ->
            GPSPreferences.setShowBuildingsOnMap(isChecked)
        }
    }
}
