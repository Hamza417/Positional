package app.simple.positional.dialogs.clock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleLinearLayout
import app.simple.positional.decorations.ripple.DynamicRippleTextView
import app.simple.positional.decorations.switchview.SwitchView
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.preferences.ClockPreferences

class ClockMenu : CustomBottomSheetDialogFragment() {

    private lateinit var motionType: DynamicRippleTextView
    private lateinit var defaultTimeFormatSwitch: SwitchView
    private lateinit var secondsPrecisionSwitchView: SwitchView
    private lateinit var clock24HourFace: SwitchView
    private lateinit var defaultTimeFormatContainer: DynamicRippleLinearLayout
    private lateinit var clock24HourFaceContainer: DynamicRippleLinearLayout
    private lateinit var secondsPrecisionContainer: DynamicRippleLinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_clock_menu, container, false)

        motionType = view.findViewById(R.id.clock_motion_type_text)
        defaultTimeFormatContainer = view.findViewById(R.id.clock_menu_default_time_format)
        defaultTimeFormatSwitch = view.findViewById(R.id.toggle_default_time_format)
        clock24HourFace = view.findViewById(R.id.toggle_24_hours_clock)
        secondsPrecisionSwitchView = view.findViewById(R.id.toggle_remove_seconds_precision)
        secondsPrecisionContainer = view.findViewById(R.id.clock_menu_remove_seconds_container)
        clock24HourFaceContainer = view.findViewById(R.id.clock_menu_24_hours_clock)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        defaultTimeFormatSwitch.isChecked = ClockPreferences.getDefaultClockTimeFormat()
        secondsPrecisionSwitchView.isChecked = ClockPreferences.isUsingSecondsPrecision()
        clock24HourFace.isChecked = ClockPreferences.isClockFace24Hour()

        defaultTimeFormatContainer.setOnClickListener {
            defaultTimeFormatSwitch.invertCheckedStatus()
        }

        defaultTimeFormatSwitch.setOnCheckedChangeListener { isChecked ->
            ClockPreferences.setDefaultClockTime(isChecked)
        }

        clock24HourFace.setOnCheckedChangeListener {
            ClockPreferences.setClockFaceType(it)
        }

        secondsPrecisionContainer.setOnClickListener {
            secondsPrecisionSwitchView.invertCheckedStatus()
        }

        secondsPrecisionSwitchView.setOnCheckedChangeListener {
            ClockPreferences.setUseSecondsPrecision(it)
        }

        clock24HourFaceContainer.setOnClickListener {
            clock24HourFace.isChecked = !clock24HourFace.isChecked
        }

        view.findViewById<TextView>(R.id.clock_needle_theme_text).setOnClickListener {
            ClockNeedle.newInstance().show(parentFragmentManager, "clock_menu")
            dismiss()
        }

        motionType.setOnClickListener {
            ClockMotionType.newInstance().show(parentFragmentManager, "clock_menu")
            dismiss()
        }
    }

    companion object {
        fun newInstance(): ClockMenu {
            val args = Bundle()
            val fragment = ClockMenu()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "clock_menu"
    }
}
