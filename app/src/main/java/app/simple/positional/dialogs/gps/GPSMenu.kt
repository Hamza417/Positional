package app.simple.positional.dialogs.gps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.SwitchCompat
import app.simple.positional.R
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.preference.GPSPreferences

class GPSMenu : CustomBottomSheetDialogFragment() {

    private lateinit var toggleLabel: SwitchCompat
    private lateinit var toggleSatellite: SwitchCompat
    private lateinit var toggleHighContrast: SwitchCompat
    private lateinit var toggleBuilding: SwitchCompat
    private lateinit var toggleAutoCenter: SwitchCompat

    private lateinit var toggleLabelContainer: LinearLayout
    private lateinit var toggleSatelliteContainer: LinearLayout
    private lateinit var toggleHighContrastContainer: LinearLayout
    private lateinit var toggleBuildingContainer: LinearLayout
    private lateinit var toggleAutoCenterContainer: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_gps_menu, container, false)

        toggleLabel = view.findViewById(R.id.toggle_label)
        toggleSatellite = view.findViewById(R.id.toggle_satellite)
        toggleHighContrast = view.findViewById(R.id.toggle_high_contrast)
        toggleBuilding = view.findViewById(R.id.toggle_buildings)
        toggleAutoCenter = view.findViewById(R.id.toggle_auto_center)

        toggleLabelContainer = view.findViewById(R.id.gps_menu_show_label_container)
        toggleSatelliteContainer = view.findViewById(R.id.gps_menu_satellite_mode_container)
        toggleHighContrastContainer = view.findViewById(R.id.gps_menu_high_contrast_container)
        toggleBuildingContainer = view.findViewById(R.id.gps_menu_show_building_container)
        toggleAutoCenterContainer = view.findViewById(R.id.gps_menu_auto_center_container)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toggleLabel.isChecked = GPSPreferences.isLabelOn()
        toggleSatellite.isChecked = GPSPreferences.isSatelliteOn()
        toggleHighContrast.isChecked = GPSPreferences.getHighContrastMap()
        toggleBuilding.isChecked = GPSPreferences.getShowBuildingsOnMap()
        toggleAutoCenter.isChecked = GPSPreferences.getMapAutoCenter()

        if (toggleSatellite.isChecked) {
            satelliteOverrides(0.4F, isChecked = true)
        }

        toggleLabel.setOnCheckedChangeListener { _, isChecked ->
            GPSPreferences.setLabelMode(isChecked)
        }

        toggleSatellite.setOnCheckedChangeListener { _, isChecked ->
            GPSPreferences.setSatelliteMode(isChecked)
            if (isChecked) {
                satelliteOverrides(0.4F, isChecked)
            } else {
                satelliteOverrides(1.0F, isChecked)
            }
        }

        toggleHighContrast.setOnCheckedChangeListener { _, isChecked ->
            GPSPreferences.setHighContrastMap(isChecked)
        }

        toggleBuilding.setOnCheckedChangeListener { _, isChecked ->
            GPSPreferences.setShowBuildingsOnMap(isChecked)
        }

        toggleAutoCenter.setOnCheckedChangeListener { _, isChecked ->
            GPSPreferences.setMapAutoCenter(isChecked)
        }

        toggleLabelContainer.setOnClickListener {
            toggleLabel.isChecked = !toggleLabel.isChecked
        }

        toggleSatelliteContainer.setOnClickListener {
            toggleSatellite.isChecked = !toggleSatellite.isChecked
        }

        toggleHighContrastContainer.setOnClickListener {
            toggleHighContrast.isChecked = !toggleHighContrast.isChecked
        }

        toggleBuildingContainer.setOnClickListener {
            toggleBuilding.isChecked = !toggleBuilding.isChecked
        }

        toggleAutoCenterContainer.setOnClickListener {
            toggleAutoCenter.isChecked = !toggleAutoCenter.isChecked
        }
    }

    private fun satelliteOverrides(alpha: Float, isChecked: Boolean) {

        toggleLabel.isClickable = !isChecked
        toggleBuilding.isClickable = !isChecked
        toggleHighContrast.isClickable = !isChecked

        toggleBuildingContainer.animate().alpha(alpha).start()
        toggleBuildingContainer.isClickable = !isChecked

        toggleLabelContainer.animate().alpha(alpha).start()
        toggleLabelContainer.isClickable = !isChecked

        toggleHighContrastContainer.animate().alpha(alpha).start()
        toggleHighContrastContainer.isClickable = !isChecked
    }
}
