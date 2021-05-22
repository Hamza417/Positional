package app.simple.positional.dialogs.gps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleLinearLayout
import app.simple.positional.decorations.ripple.DynamicRippleTextView
import app.simple.positional.decorations.switchview.SwitchView
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.dialogs.settings.HtmlViewer
import app.simple.positional.preferences.GPSPreferences

class OSMMenu : CustomBottomSheetDialogFragment() {

    private lateinit var toggleAutoCenter: SwitchView
    private lateinit var toggleVolumeKeys: SwitchView
    private lateinit var toggleUseBearing: SwitchView
    private lateinit var pinCustomization: DynamicRippleTextView
    private lateinit var tileSource: DynamicRippleTextView

    private lateinit var toggleAutoCenterContainer: DynamicRippleLinearLayout
    private lateinit var toggleVolumeKeysContainer: DynamicRippleLinearLayout
    private lateinit var toggleUseBearingContainer: DynamicRippleLinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.dialog_osm_menu, container, false)

        toggleAutoCenter = view.findViewById(R.id.toggle_auto_center)
        toggleVolumeKeys = view.findViewById(R.id.toggle_use_volume_keys)
        toggleUseBearing = view.findViewById(R.id.toggle_bearing_rotation)
        pinCustomization = view.findViewById(R.id.gps_pin_customization)
        tileSource = view.findViewById(R.id.gps_tile_source)

        toggleAutoCenterContainer = view.findViewById(R.id.gps_menu_auto_center_container)
        toggleVolumeKeysContainer = view.findViewById(R.id.gps_menu_volume_keys_container)
        toggleUseBearingContainer = view.findViewById(R.id.gps_menu_bearing_rotation)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toggleAutoCenter.isChecked = GPSPreferences.getMapAutoCenter()
        toggleVolumeKeys.isChecked = GPSPreferences.isUsingVolumeKeys()
        toggleUseBearing.isChecked = GPSPreferences.isBearingRotationOn()

        toggleAutoCenter.setOnCheckedChangeListener {
            GPSPreferences.setMapAutoCenter(it)
        }

        toggleVolumeKeys.setOnCheckedChangeListener {
            GPSPreferences.setUseVolumeKeys(it)
        }

        toggleUseBearing.setOnCheckedChangeListener {
            GPSPreferences.setUseBearingRotation(it)
        }

        pinCustomization.setOnClickListener {
            PinCustomization.newInstance()
                    .show(requireActivity().supportFragmentManager, "pin_customization")
            dismiss()
        }

        tileSource.setOnClickListener {
            MapTiles.newInstance()
                    .show(parentFragmentManager, "map_tiles")
            dismiss()
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

        toggleUseBearingContainer.setOnClickListener {
            toggleUseBearing.invertCheckedStatus()
        }
    }

    companion object {
        fun newInstance(): OSMMenu {
            val args = Bundle()
            val fragment = OSMMenu()
            fragment.arguments = args
            return fragment
        }
    }
}
