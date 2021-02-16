package app.simple.positional.dialogs.gps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.positional.R
import app.simple.positional.math.MathExtensions
import app.simple.positional.math.UnitConverter.toKiloMetersPerHour
import app.simple.positional.math.UnitConverter.toMilesPerHour
import app.simple.positional.preference.MainPreferences
import app.simple.positional.util.HtmlHelper
import app.simple.positional.views.CustomBottomSheetDialog
import app.simple.positional.views.Speedometer

class MovementExpansion : CustomBottomSheetDialog() {

    private var broadcastReceiver: BroadcastReceiver? = null

    private lateinit var speedometer: Speedometer
    private lateinit var speed: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        retainInstance = true
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.expansion_dialog_movement, container, false)

        speedometer = view.findViewById(R.id.speedometer)
        speed = view.findViewById(R.id.movement_expansion_speed_text)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == "location") {
                    val location = intent.getParcelableExtra<Location>("location")!!

                    speedometer.setSpeedValue(if (MainPreferences.getUnit()) location.speed.toKiloMetersPerHour() else location.speed.toMilesPerHour())
                    speed.text = if (MainPreferences.getUnit()) {
                        HtmlHelper.fromHtml("<b>${getString(R.string.gps_speed)}</b> ${MathExtensions.round(location.speed.toDouble().toKiloMetersPerHour(), 2)} ${getString(R.string.kilometer_hour)}")
                    } else {
                        HtmlHelper.fromHtml("<b>${getString(R.string.gps_speed)}</b> ${MathExtensions.round(location.speed.toDouble().toKiloMetersPerHour().toMilesPerHour(), 2)} ${getString(R.string.miles_hour)}")
                    }
                }
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver!!, IntentFilter("location"))
        super.onResume()
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver!!)
        super.onPause()
    }

    override fun onDestroy() {
        speedometer.clear()
        super.onDestroy()
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
