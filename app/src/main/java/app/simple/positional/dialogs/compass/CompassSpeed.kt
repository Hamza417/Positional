package app.simple.positional.dialogs.compass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import app.simple.positional.R
import app.simple.positional.preference.CompassPreference
import app.simple.positional.ui.Compass
import app.simple.positional.views.CustomBottomSheetDialogFragment
import java.lang.ref.WeakReference

class CompassSpeed(private val weakReference: WeakReference<Compass>) : CustomBottomSheetDialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
    }

    private lateinit var smooth: RadioButton
    private lateinit var normal: RadioButton
    private lateinit var fast: RadioButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_compass_speed, container, false)

        smooth = view.findViewById(R.id.speed_smooth)
        normal = view.findViewById(R.id.speed_normal)
        fast = view.findViewById(R.id.speed_fast)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setButtons(CompassPreference.getCompassSpeed())

        smooth.setOnClickListener {
            setButtons(0.03f)
            weakReference.get()?.setSpeed(0.03f)
        }

        normal.setOnClickListener {
            setButtons(0.06f)
            weakReference.get()?.setSpeed(0.06f)
        }

        fast.setOnClickListener {
            setButtons(0.15f)
            weakReference.get()?.setSpeed(0.15f)
        }
    }

    private fun setButtons(value: Float) {
        CompassPreference.setCompassSpeed(value)

        smooth.isChecked = value == 0.03f
        normal.isChecked = value == 0.06f
        fast.isChecked = value == 0.15f
    }
}