package app.simple.positional.dialogs.compass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import app.simple.positional.R
import app.simple.positional.preference.CompassPreference
import app.simple.positional.preference.LevelPreferences
import app.simple.positional.views.CustomBottomSheetDialog

class NoSensorAlert : CustomBottomSheetDialog() {

    fun newInstance(string: String): NoSensorAlert {
        val args = Bundle()
        val fragment = NoSensorAlert()
        args.putString("which", string)
        fragment.arguments = args
        return fragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_compass_no_sensor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<CheckBox>(R.id.compass_no_sensor_no_show).isChecked = when (this.arguments?.getString("which")) {
            "compass" -> {
                CompassPreference.isNoSensorAlertON()
            }
            "level" -> {
                LevelPreferences.isNoSensorAlertON()
            }
            else -> {
                true
            }
        }

        view.findViewById<CheckBox>(R.id.compass_no_sensor_no_show).setOnCheckedChangeListener { button, isChecked ->
            if (button.isPressed) {
                if (this.arguments?.getString("which") == "compass") {
                    CompassPreference.setNoSensorAlert(value = isChecked)
                } else if (this.arguments?.getString("which") == "level") {
                    LevelPreferences.setNoSensorAlert(value = isChecked)
                }
            }
        }

        view.findViewById<Button>(R.id.compass_no_sensor_ok).setOnClickListener {
            this.dialog?.dismiss()
        }
    }
}