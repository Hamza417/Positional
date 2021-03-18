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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_clock_motion_type, container, false)

        smooth = view.findViewById(R.id.motion_smooth)
        tick = view.findViewById(R.id.motion_tick)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setButton(ClockPreferences.getMovementType())

        smooth.setOnClickListener {
            setButton(true)
        }

        tick.setOnClickListener {
            setButton(false)
        }
    }

    private fun setButton(value: Boolean) {
        smooth.isChecked = value
        tick.isChecked = !value
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