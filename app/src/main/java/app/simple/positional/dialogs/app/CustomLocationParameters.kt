package app.simple.positional.dialogs.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.simple.positional.R
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.preferences.ClockPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.DMSConverter
import app.simple.positional.util.HtmlHelper.fromHtml

class CustomLocationParameters : CustomBottomSheetDialogFragment() {

    private lateinit var latitude: TextView
    private lateinit var longitude: TextView
    private lateinit var timezone: TextView
    private lateinit var address: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_custom_location_shortcut, container, false)

        latitude = view.findViewById(R.id.latitude)
        longitude = view.findViewById(R.id.longitude)
        timezone = view.findViewById(R.id.timezone)
        address = view.findViewById(R.id.address)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        latitude.text = fromHtml("<b>${getString(R.string.gps_latitude)}</b> " +
                DMSConverter.latitudeAsDMS(MainPreferences.getCoordinates()[0].toDouble(), 3, requireContext()))

        longitude.text = fromHtml("<b>${getString(R.string.gps_longitude)}</b> " +
                DMSConverter.longitudeAsDMS(MainPreferences.getCoordinates()[1].toDouble(), 3, requireContext()))

        timezone.text = fromHtml("<b>${getString(R.string.local_timezone)}</b> " +
                ClockPreferences.getTimeZone())

        address.text = fromHtml("<b>${getString(R.string.gps_address)}</b>: " +
                MainPreferences.getAddress())
    }

    companion object {
        fun newInstance(): CustomLocationParameters {
            val args = Bundle()
            val fragment = CustomLocationParameters()
            fragment.arguments = args
            return fragment
        }
    }
}