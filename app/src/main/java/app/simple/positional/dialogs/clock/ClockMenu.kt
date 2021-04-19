package app.simple.positional.dialogs.clock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import app.simple.positional.R
import app.simple.positional.decorations.switchview.SwitchView
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.preference.ClockPreferences

class ClockMenu : CustomBottomSheetDialogFragment() {

    private lateinit var defaultTimeFormatContainer: LinearLayout
    private lateinit var defaultTimeFormatSwitch: SwitchView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_clock_menu, container, false)

        defaultTimeFormatContainer = view.findViewById(R.id.clock_menu_default_time_format)
        defaultTimeFormatSwitch = view.findViewById(R.id.toggle_default_time_format)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        defaultTimeFormatSwitch.isChecked = ClockPreferences.getDefaultClockTime()

        defaultTimeFormatContainer.setOnClickListener {
            defaultTimeFormatSwitch.isChecked = !defaultTimeFormatSwitch.isChecked
        }

        defaultTimeFormatSwitch.setOnCheckedChangeListener { isChecked ->
            ClockPreferences.setDefaultClockTime(isChecked)
        }

        view.findViewById<TextView>(R.id.clock_needle_theme_text).setOnClickListener {
            ClockNeedle.newInstance().show(parentFragmentManager, "null")
        }

        view.findViewById<TextView>(R.id.clock_motion_type_text).setOnClickListener {
            ClockMotionType.newInstance().show(parentFragmentManager, "null")
        }
    }

    companion object {
        fun newInstance(): ClockMenu {
            val args = Bundle()
            val fragment = ClockMenu()
            fragment.arguments = args
            return fragment
        }
    }
}
