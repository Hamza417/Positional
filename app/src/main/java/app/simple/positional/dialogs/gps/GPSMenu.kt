package app.simple.positional.dialogs.gps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleLinearLayout
import app.simple.positional.decorations.switchview.SwitchView
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.dialogs.settings.HtmlViewer
import app.simple.positional.preference.GPSPreferences

class GPSMenu : CustomBottomSheetDialogFragment() {

    private lateinit var toggleLabel: SwitchView
    private lateinit var toggleSatellite: SwitchView
    private lateinit var toggleHighContrast: SwitchView
    private lateinit var toggleBuilding: SwitchView
    private lateinit var toggleAutoCenter: SwitchView
    private lateinit var toggleVolumeKeys: SwitchView
    private lateinit var toggleUseSmallIcon: SwitchView
    private lateinit var toggleUseBearing: SwitchView

    private lateinit var toggleLabelContainer: DynamicRippleLinearLayout
    private lateinit var toggleSatelliteContainer: DynamicRippleLinearLayout
    private lateinit var toggleHighContrastContainer: DynamicRippleLinearLayout
    private lateinit var toggleBuildingContainer: DynamicRippleLinearLayout
    private lateinit var toggleAutoCenterContainer: DynamicRippleLinearLayout
    private lateinit var toggleVolumeKeysContainer: DynamicRippleLinearLayout
    private lateinit var toggleUseSmallIconContainer: DynamicRippleLinearLayout
    private lateinit var toggleUseBearingContainer: DynamicRippleLinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_gps_menu, container, false)

        toggleLabel = view.findViewById(R.id.toggle_label)
        toggleSatellite = view.findViewById(R.id.toggle_satellite)
        toggleHighContrast = view.findViewById(R.id.toggle_high_contrast)
        toggleBuilding = view.findViewById(R.id.toggle_buildings)
        toggleAutoCenter = view.findViewById(R.id.toggle_auto_center)
        toggleVolumeKeys = view.findViewById(R.id.toggle_use_volume_keys)
        toggleUseSmallIcon = view.findViewById(R.id.toggle_use_smaller_icon)
        toggleUseBearing = view.findViewById(R.id.toggle_bearing_rotation)

        toggleLabelContainer = view.findViewById(R.id.gps_menu_show_label_container)
        toggleSatelliteContainer = view.findViewById(R.id.gps_menu_satellite_mode_container)
        toggleHighContrastContainer = view.findViewById(R.id.gps_menu_high_contrast_container)
        toggleBuildingContainer = view.findViewById(R.id.gps_menu_show_building_container)
        toggleAutoCenterContainer = view.findViewById(R.id.gps_menu_auto_center_container)
        toggleVolumeKeysContainer = view.findViewById(R.id.gps_menu_volume_keys_container)
        toggleUseSmallIconContainer = view.findViewById(R.id.gps_menu_us_smaller_icon)
        toggleUseBearingContainer = view.findViewById(R.id.gps_menu_bearing_rotation)

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
        toggleUseBearing.isChecked = GPSPreferences.isBearingRotationOn()

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

        toggleHighContrast.setOnCheckedChangeListener {
            GPSPreferences.setHighContrastMap(it)
        }

        toggleBuilding.setOnCheckedChangeListener {
            GPSPreferences.setShowBuildingsOnMap(it)
        }

        toggleAutoCenter.setOnCheckedChangeListener {
            GPSPreferences.setMapAutoCenter(it)
        }

        toggleVolumeKeys.setOnCheckedChangeListener {
            GPSPreferences.setUseVolumeKeys(it)
        }

        toggleUseSmallIcon.setOnCheckedChangeListener {
            GPSPreferences.setUseSmallerIcon(it)
        }

        toggleUseBearing.setOnCheckedChangeListener {
            GPSPreferences.setUseBearingRotation(it)
        }

        toggleLabelContainer.setOnClickListener {
            toggleLabel.invertCheckedStatus()
        }

        toggleSatelliteContainer.setOnClickListener {
            toggleSatellite.invertCheckedStatus()
        }

        toggleHighContrastContainer.setOnClickListener {
            toggleHighContrast.invertCheckedStatus()
        }

        toggleBuildingContainer.setOnClickListener {
            toggleBuilding.invertCheckedStatus()
        }

        toggleAutoCenterContainer.setOnClickListener {
            toggleAutoCenter.invertCheckedStatus()
        }

        toggleVolumeKeysContainer.setOnClickListener {
            toggleVolumeKeys.invertCheckedStatus()
        }

        toggleVolumeKeysContainer.setOnLongClickListener {
            HtmlViewer.newInstance("Media Keys").show(childFragmentManager, "html_viewer")
            true
        }

        toggleUseSmallIconContainer.setOnClickListener {
            toggleUseSmallIcon.invertCheckedStatus()
        }

        toggleUseBearingContainer.setOnClickListener {
            toggleUseBearing.invertCheckedStatus()
        }

        /**
         * Calling this here because adding click event listeners
         * resets the [View.isClickable] state to true
         */
        if (toggleSatellite.isChecked) {
            satelliteOverrides(0.4F, isChecked = true)
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
