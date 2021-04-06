package app.simple.positional.dialogs.gps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import app.simple.positional.R
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.dialogs.settings.HtmlViewer
import app.simple.positional.preference.GPSPreferences
import app.simple.switchview.views.SwitchView

class GPSMenu : CustomBottomSheetDialogFragment() {

    private lateinit var toggleLabel: SwitchView
    private lateinit var toggleSatellite: SwitchView
    private lateinit var toggleHighContrast: SwitchView
    private lateinit var toggleBuilding: SwitchView
    private lateinit var toggleAutoCenter: SwitchView
    private lateinit var toggleVolumeKeys: SwitchView
    private lateinit var toggleUseSmallIcon: SwitchView

    private lateinit var toggleLabelContainer: LinearLayout
    private lateinit var toggleSatelliteContainer: LinearLayout
    private lateinit var toggleHighContrastContainer: LinearLayout
    private lateinit var toggleBuildingContainer: LinearLayout
    private lateinit var toggleAutoCenterContainer: LinearLayout
    private lateinit var toggleVolumeKeysContainer: LinearLayout
    private lateinit var toggleUseSmallIconContainer: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_gps_menu, container, false)

        toggleLabel = view.findViewById(R.id.toggle_label)
        toggleSatellite = view.findViewById(R.id.toggle_satellite)
        toggleHighContrast = view.findViewById(R.id.toggle_high_contrast)
        toggleBuilding = view.findViewById(R.id.toggle_buildings)
        toggleAutoCenter = view.findViewById(R.id.toggle_auto_center)
        toggleVolumeKeys = view.findViewById(R.id.toggle_use_volume_keys)
        toggleUseSmallIcon = view.findViewById(R.id.toggle_use_smaller_icon)

        toggleLabelContainer = view.findViewById(R.id.gps_menu_show_label_container)
        toggleSatelliteContainer = view.findViewById(R.id.gps_menu_satellite_mode_container)
        toggleHighContrastContainer = view.findViewById(R.id.gps_menu_high_contrast_container)
        toggleBuildingContainer = view.findViewById(R.id.gps_menu_show_building_container)
        toggleAutoCenterContainer = view.findViewById(R.id.gps_menu_auto_center_container)
        toggleVolumeKeysContainer = view.findViewById(R.id.gps_menu_volume_keys_container)
        toggleUseSmallIconContainer = view.findViewById(R.id.gps_menu_us_smaller_icon)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toggleLabel.isChecked = GPSPreferences.isLabelOn()
        toggleSatellite.isChecked = GPSPreferences.isSatelliteOn()
        toggleHighContrast.isChecked = GPSPreferences.getHighContrastMap()
        toggleBuilding.isChecked = GPSPreferences.getShowBuildingsOnMap()
        toggleAutoCenter.isChecked = GPSPreferences.getMapAutoCenter()
        toggleVolumeKeys.isChecked = GPSPreferences.isUsingVolumeKeys()
        toggleUseSmallIcon.isChecked = GPSPreferences.isUsingSmallerIcon()

        if (toggleSatellite.isChecked) {
            satelliteOverrides(0.4F, isChecked = true)
        }

        toggleLabel.setOnCheckedChangeListener { isChecked ->
            GPSPreferences.setLabelMode(isChecked)
        }

        toggleSatellite.setOnCheckedChangeListener { isChecked ->
            GPSPreferences.setSatelliteMode(isChecked)
            if (isChecked) {
                satelliteOverrides(0.4F, isChecked)
            } else {
                satelliteOverrides(1.0F, isChecked)
            }
        }

        toggleHighContrast.setOnCheckedChangeListener { isChecked ->
            GPSPreferences.setHighContrastMap(isChecked)
        }

        toggleBuilding.setOnCheckedChangeListener { isChecked ->
            GPSPreferences.setShowBuildingsOnMap(isChecked)
        }

        toggleAutoCenter.setOnCheckedChangeListener { isChecked ->
            GPSPreferences.setMapAutoCenter(isChecked)
        }

        toggleVolumeKeys.setOnCheckedChangeListener { isChecked ->
            GPSPreferences.setUseVolumeKeys(isChecked)
        }

        toggleUseSmallIcon.setOnCheckedChangeListener { isChecked ->
            GPSPreferences.setUseSmallerIcon(isChecked)
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

        toggleVolumeKeysContainer.setOnClickListener {
            toggleVolumeKeys.isChecked = !toggleVolumeKeys.isChecked
        }

        toggleVolumeKeysContainer.setOnLongClickListener {
            HtmlViewer.newInstance("Media Keys").show(childFragmentManager, "html_viewer")
            true
        }

        toggleUseSmallIconContainer.setOnClickListener {
            toggleUseSmallIcon.isChecked = !toggleUseSmallIcon.isChecked
        }
    }

    private fun satelliteOverrides(alpha: Float, isChecked: Boolean) {

        toggleBuilding.isClickable = !isChecked
        toggleHighContrast.isClickable = !isChecked

        toggleBuildingContainer.animate().alpha(alpha).setInterpolator(DecelerateInterpolator(1.5F)).start()
        toggleBuildingContainer.isClickable = !isChecked

        toggleHighContrastContainer.animate().alpha(alpha).setInterpolator(DecelerateInterpolator(1.5F)).start()
        toggleHighContrastContainer.isClickable = !isChecked
    }
}
