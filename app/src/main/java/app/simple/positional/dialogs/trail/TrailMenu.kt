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
import app.simple.positional.preferences.TrailPreferences

class TrailMenu : CustomBottomSheetDialogFragment() {

    private lateinit var toggleLabel: SwitchView
    private lateinit var toggleSatellite: SwitchView
    private lateinit var toggleHighContrast: SwitchView
    private lateinit var toggleBuilding: SwitchView
    private lateinit var toggleGeodesic: SwitchView

    private lateinit var toggleLabelContainer: DynamicRippleLinearLayout
    private lateinit var toggleSatelliteContainer: DynamicRippleLinearLayout
    private lateinit var toggleHighContrastContainer: DynamicRippleLinearLayout
    private lateinit var toggleBuildingContainer: DynamicRippleLinearLayout
    private lateinit var toggleGeodesicContainer: DynamicRippleLinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_trail_menu, container, false)

        toggleLabel = view.findViewById(R.id.toggle_label)
        toggleSatellite = view.findViewById(R.id.toggle_satellite)
        toggleHighContrast = view.findViewById(R.id.toggle_high_contrast)
        toggleBuilding = view.findViewById(R.id.toggle_buildings)
        toggleGeodesic = view.findViewById(R.id.toggle_auto_center)

        toggleLabelContainer = view.findViewById(R.id.gps_menu_show_label_container)
        toggleSatelliteContainer = view.findViewById(R.id.gps_menu_satellite_mode_container)
        toggleHighContrastContainer = view.findViewById(R.id.gps_menu_high_contrast_container)
        toggleBuildingContainer = view.findViewById(R.id.gps_menu_show_building_container)
        toggleGeodesicContainer = view.findViewById(R.id.gps_menu_auto_center_container)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toggleLabel.isChecked = TrailPreferences.isLabelOn()
        toggleSatellite.isChecked = TrailPreferences.isSatelliteOn()
        toggleHighContrast.isChecked = TrailPreferences.getHighContrastMap()
        toggleBuilding.isChecked = TrailPreferences.getShowBuildingsOnMap()
        toggleGeodesic.isChecked = TrailPreferences.isTrailGeodesic()

        toggleLabel.setOnCheckedChangeListener { isChecked ->
            TrailPreferences.setLabelMode(isChecked)
        }

        toggleSatellite.setOnCheckedChangeListener { isChecked ->
            TrailPreferences.setSatelliteMode(isChecked)
            if (isChecked) {
                satelliteOverrides(0.4F, isChecked)
            } else {
                satelliteOverrides(1.0F, isChecked)
            }
        }

        toggleHighContrast.setOnCheckedChangeListener {
            TrailPreferences.setHighContrastMap(it)
        }

        toggleBuilding.setOnCheckedChangeListener {
            TrailPreferences.setShowBuildingsOnMap(it)
        }

        toggleGeodesic.setOnCheckedChangeListener {
            TrailPreferences.setGeodesic(it)
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

        toggleGeodesicContainer.setOnClickListener {
            toggleGeodesic.invertCheckedStatus()
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

    companion object {
        fun newInstance(): TrailMenu {
            val args = Bundle()
            val fragment = TrailMenu()
            fragment.arguments = args
            return fragment
        }
    }
}
