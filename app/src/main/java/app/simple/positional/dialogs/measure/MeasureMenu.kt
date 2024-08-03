package app.simple.positional.dialogs.measure

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleLinearLayout
import app.simple.positional.decorations.switchview.SwitchView
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.preferences.MeasurePreferences

class MeasureMenu : CustomBottomSheetDialogFragment() {

    private lateinit var label: SwitchView
    private lateinit var labelContainer: DynamicRippleLinearLayout
    private lateinit var building: SwitchView
    private lateinit var buildingContainer: DynamicRippleLinearLayout
    private lateinit var satellite: SwitchView
    private lateinit var satelliteContainer: DynamicRippleLinearLayout
    private lateinit var highContrast: SwitchView
    private lateinit var highContrastContainer: DynamicRippleLinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_measure_menu, container, false)

        label = view.findViewById(R.id.toggle_label)
        labelContainer = view.findViewById(R.id.show_label_container)
        building = view.findViewById(R.id.toggle_buildings)
        buildingContainer = view.findViewById(R.id.show_building_container)
        satellite = view.findViewById(R.id.toggle_satellite)
        satelliteContainer = view.findViewById(R.id.satellite_mode_container)
        highContrast = view.findViewById(R.id.toggle_high_contrast)
        highContrastContainer = view.findViewById(R.id.high_contrast_container)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        label.isChecked = MeasurePreferences.isLabelOn()
        building.isChecked = MeasurePreferences.getShowBuildingsOnMap()
        satellite.isChecked = MeasurePreferences.isSatelliteOn()
        highContrast.isChecked = MeasurePreferences.getHighContrastMap()

        labelContainer.setOnClickListener {
            label.invertCheckedStatus()
        }

        buildingContainer.setOnClickListener {
            building.invertCheckedStatus()
        }

        satelliteContainer.setOnClickListener {
            satellite.invertCheckedStatus()
        }

        highContrastContainer.setOnClickListener {
            highContrast.invertCheckedStatus()
        }

        label.setOnCheckedChangeListener { isChecked ->
            MeasurePreferences.setLabelMode(isChecked)
        }

        building.setOnCheckedChangeListener { isChecked ->
            MeasurePreferences.setShowBuildingsOnMap(isChecked)
        }

        satellite.setOnCheckedChangeListener { isChecked ->
            MeasurePreferences.setSatelliteMode(isChecked)
        }

        highContrast.setOnCheckedChangeListener { isChecked ->
            MeasurePreferences.setHighContrastMap(isChecked)
        }
    }

    companion object {
        fun newInstance(): MeasureMenu {
            val args = Bundle()
            val fragment = MeasureMenu()
            fragment.arguments = args
            return fragment
        }

        fun Fragment.showMeasureMenu(): MeasureMenu {
            val measureMenu = newInstance()
            measureMenu.show(childFragmentManager, TAG)
            return measureMenu
        }

        const val TAG = "MeasureMenu"
    }
}
