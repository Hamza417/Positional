package app.simple.positional.dialogs.clock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import app.simple.positional.R
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.preference.ClockPreferences

class ClockMotionType : CustomBottomSheetDialogFragment() {

    private lateinit var smooth: RadioButton
    private lateinit var tick: RadioButton
    private lateinit var oscillate: RadioButton
    private lateinit var tickSmooth: RadioButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_clock_motion_type, container, false)

        smooth = view.findViewById(R.id.motion_smooth)
        tick = view.findViewById(R.id.motion_tick)
        oscillate = view.findViewById(R.id.motion_tick_oscillate)
        tickSmooth = view.findViewById(R.id.motion_tick_smooth)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setButton(ClockPreferences.getMovementType())

        smooth.setOnClickListener {
            setButton("smooth")
        }

        tick.setOnClickListener {
            setButton("tick")
        }

        oscillate.setOnClickListener {
            setButton("oscillate")
        }

        tickSmooth.setOnClickListener {
            setButton("tick_smooth")
        }
    }

    private fun setButton(value: String) {
        smooth.isChecked = value == "smooth"
        tick.isChecked = value == "tick"
        oscillate.isChecked = value == "oscillate"
        tickSmooth.isChecked = value == "tick_smooth"

        ClockPreferences.setMovementType(value)
    }

    companion object {
        fun newInstance(): ClockMotionType {
            val args = Bundle()
            val fragment = ClockMotionType()
            fragment.arguments = args
            return fragment
        }
    }
}