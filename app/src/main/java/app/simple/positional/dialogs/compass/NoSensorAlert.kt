package app.simple.positional.dialogs.compass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.positional.R
import app.simple.positional.preference.CompassPreference
import app.simple.positional.views.CustomBottomSheetDialog
import kotlinx.android.synthetic.main.dialog_compass_no_sensor.*

class NoSensorAlert : CustomBottomSheetDialog() {

    fun newInstance(): NoSensorAlert {
        return NoSensorAlert()
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

        compass_no_sensor_no_show.setOnCheckedChangeListener { _, isChecked ->
            CompassPreference().setNoSensorAlert(value = isChecked, context = requireContext())
        }

        compass_no_sensor_ok.setOnClickListener {
            this.dialog?.dismiss()
        }
    }
}