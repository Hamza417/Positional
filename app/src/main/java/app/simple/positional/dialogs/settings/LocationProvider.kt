package app.simple.positional.dialogs.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.positional.R
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.decorations.views.CustomRadioButton
import app.simple.positional.preference.MainPreferences

class LocationProvider : CustomBottomSheetDialogFragment() {

    private lateinit var androidRadioButton: CustomRadioButton
    private lateinit var fusedRadioButton: CustomRadioButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_location_provider, container, false)

        androidRadioButton = view.findViewById(R.id.radio_button_android)
        fusedRadioButton = view.findViewById(R.id.radio_button_fused)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setButton(MainPreferences.getLocationProvider())

        androidRadioButton.setOnClickListener {
            setButton("android")
        }

        fusedRadioButton.setOnClickListener {
            setButton("fused")
        }

        super.onViewCreated(view, savedInstanceState)
    }

    private fun setButton(value: String) {
        androidRadioButton.isChecked = value == "android"
        fusedRadioButton.isChecked = value == "fused"
        MainPreferences.setLocationProvider(value)
    }

    companion object {
        fun newInstance(): LocationProvider {
            val args = Bundle()
            val fragment = LocationProvider()
            fragment.arguments = args
            return fragment
        }
    }
}