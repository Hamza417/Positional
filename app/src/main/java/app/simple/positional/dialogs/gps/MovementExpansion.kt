package app.simple.positional.dialogs.gps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import app.simple.positional.R
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.decorations.views.Speedometer
import app.simple.positional.math.MathExtensions
import app.simple.positional.math.UnitConverter.toKiloMetersPerHour
import app.simple.positional.math.UnitConverter.toMilesPerHour
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.HtmlHelper
import app.simple.positional.viewmodels.viewmodel.LocationViewModel

class MovementExpansion : CustomBottomSheetDialogFragment() {

    private lateinit var speedometer: Speedometer
    private lateinit var speed: TextView

    private lateinit var locationViewModel: LocationViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.expansion_dialog_movement, container, false)

        speedometer = view.findViewById(R.id.speedometer)
        speed = view.findViewById(R.id.movement_expansion_speed_text)

        locationViewModel = ViewModelProvider(requireActivity()).get(LocationViewModel::class.java)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationViewModel.getLocation().observe(viewLifecycleOwner) {
            speedometer.setSpeedValue(
                    if (MainPreferences.isMetric()) {
                        it.speed.toKiloMetersPerHour()
                    } else {
                        it.speed.toMilesPerHour()
                    })

            speed.text = if (MainPreferences.isMetric()) {
                HtmlHelper.fromHtml("<b>${getString(R.string.gps_speed)}</b> " +
                        "${MathExtensions.round(it.speed.toDouble().toKiloMetersPerHour(), 2)} " +
                        getString(R.string.kilometer_hour))
            } else {
                HtmlHelper.fromHtml("<b>${getString(R.string.gps_speed)}</b> " +
                        "${MathExtensions.round(it.speed.toDouble().toKiloMetersPerHour().toMilesPerHour(), 2)} " +
                        getString(R.string.miles_hour))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speedometer.clear()
    }

    companion object {
        fun newInstance(): MovementExpansion {
            val args = Bundle()
            val fragment = MovementExpansion()
            fragment.arguments = args
            return fragment
        }
    }
}
