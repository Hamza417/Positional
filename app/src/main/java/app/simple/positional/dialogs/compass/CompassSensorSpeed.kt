package app.simple.positional.dialogs.compass

import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.positional.R
import app.simple.positional.preference.CompassPreference
import app.simple.positional.ui.Compass
import app.simple.positional.views.CustomBottomSheetDialog
import kotlinx.android.synthetic.main.dialog_compass_sensor_speed.*
import java.lang.ref.WeakReference

class CompassSensorSpeed(private val weakReference: WeakReference<Compass>) : CustomBottomSheetDialog() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_compass_sensor_speed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setButtons(CompassPreference().getDelay(requireContext()))

        speed_smooth.setOnClickListener {
            setButtons(SensorManager.SENSOR_DELAY_GAME)
            weakReference.get()?.setSpeed(SensorManager.SENSOR_DELAY_GAME)
        }

        speed_quick.setOnClickListener {
            setButtons(SensorManager.SENSOR_DELAY_FASTEST)
            weakReference.get()?.setSpeed(SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    private fun setButtons(value: Int) {
        CompassPreference().setDelay(value, requireContext())
        speed_smooth.isChecked = value == SensorManager.SENSOR_DELAY_GAME
        speed_quick.isChecked = value == SensorManager.SENSOR_DELAY_FASTEST
    }
}