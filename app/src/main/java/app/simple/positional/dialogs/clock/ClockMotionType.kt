package app.simple.positional.dialogs.clock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.positional.R
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.decorations.views.CustomRadioButton
import app.simple.positional.preferences.ClockPreferences

class ClockMotionType : CustomBottomSheetDialogFragment() {

    private lateinit var smooth: CustomRadioButton
    private lateinit var tick: CustomRadioButton
    private lateinit var oscillate: CustomRadioButton
    private lateinit var tickSmooth: CustomRadioButton
    private lateinit var mechanical: CustomRadioButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_clock_motion_type, container, false)

        smooth = view.findViewById(R.id.motion_smooth)
        tick = view.findViewById(R.id.motion_tick)
        oscillate = view.findViewById(R.id.motion_tick_oscillate)
        tickSmooth = view.findViewById(R.id.motion_tick_smooth)
        mechanical = view.findViewById(R.id.motion_mechanical)

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

        mechanical.setOnClickListener {
            setButton("mechanical")
        }
    }

    private fun setButton(value: String) {
        smooth.isChecked = value == "smooth"
        tick.isChecked = value == "tick"
        oscillate.isChecked = value == "oscillate"
        tickSmooth.isChecked = value == "tick_smooth"
        mechanical.isChecked = value == "mechanical"

        ClockPreferences.setMovementType(value)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!requireActivity().isDestroyed) {
            ClockMenu.newInstance().show(parentFragmentManager, "clock_menu")
        }
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