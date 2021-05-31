package app.simple.positional.dialogs.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.positional.R
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.decorations.views.CustomRadioButton
import app.simple.positional.preferences.MainPreferences

class MapProvider : CustomBottomSheetDialogFragment() {

    private lateinit var osm: CustomRadioButton
    private lateinit var google: CustomRadioButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_maps_provider, container, false)

        osm = view.findViewById(R.id.radio_button_osm)
        google = view.findViewById(R.id.radio_button_google)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        osm.isChecked = MainPreferences.getMapPanelType()
        google.isChecked = !MainPreferences.getMapPanelType()

        osm.setOnCheckedChangeListener { _, isChecked ->
            setButtons(isChecked)
        }

        google.setOnCheckedChangeListener { _, isChecked ->
            setButtons(!isChecked)
        }
    }

    private fun setButtons(boolean: Boolean) {
        MainPreferences.setMapPanelType(boolean)

        osm.isChecked = boolean
        google.isChecked = !boolean
    }

    companion object {
        fun newInstance(): MapProvider {
            val args = Bundle()
            val fragment = MapProvider()
            fragment.arguments = args
            return fragment
        }
    }
}