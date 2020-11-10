package app.simple.positional.dialogs.compass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.positional.R
import app.simple.positional.preference.CompassPreference
import app.simple.positional.ui.Compass
import app.simple.positional.views.CustomBottomSheetDialog
import kotlinx.android.synthetic.main.dialog_compass_speed.*
import java.lang.ref.WeakReference

class CompassSpeed(private val weakReference: WeakReference<Compass>) : CustomBottomSheetDialog() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_compass_speed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setButtons(CompassPreference().getCompassSpeed(requireContext()))

        speed_smooth.setOnClickListener {
            setButtons(0.03f)
            weakReference.get()?.setSpeed(0.03f)
        }

        speed_normal.setOnClickListener {
            setButtons(0.06f)
            weakReference.get()?.setSpeed(0.06f)
        }

        speed_fast.setOnClickListener {
            setButtons(0.15f)
            weakReference.get()?.setSpeed(0.15f)
        }
    }

    private fun setButtons(value: Float) {
        CompassPreference().setCompassSpeed(value, requireContext())

        speed_smooth.isChecked = value == 0.03f
        speed_normal.isChecked = value == 0.06f
        speed_fast.isChecked = value == 0.15f
    }
}