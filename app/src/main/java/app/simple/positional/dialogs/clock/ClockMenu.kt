package app.simple.positional.dialogs.clock

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.preference.ClockPreferences
import app.simple.positional.ui.Clock
import java.lang.ref.WeakReference

class ClockMenu : CustomBottomSheetDialogFragment() {

    private lateinit var defaultTimeFormatContainer: LinearLayout
    private lateinit var defaultTimeFormatSwitch: SwitchCompat
    lateinit var clock: WeakReference<Clock>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_clock_menu, container, false)

        defaultTimeFormatContainer = view.findViewById(R.id.clock_menu_default_time_format)
        defaultTimeFormatSwitch = view.findViewById(R.id.toggle_default_time_format)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        defaultTimeFormatSwitch.isChecked = ClockPreferences.getDefaultClockTime()

        if (BuildConfig.FLAVOR == "lite") {
            view.findViewById<TextView>(R.id.clock_needle_theme_text).setTextColor(Color.GRAY)
        }

        defaultTimeFormatContainer.setOnClickListener {
            defaultTimeFormatSwitch.isChecked = !defaultTimeFormatSwitch.isChecked
        }

        defaultTimeFormatSwitch.setOnCheckedChangeListener { _, isChecked ->
            ClockPreferences.setDefaultClockTime(isChecked)
        }

        view.findViewById<TextView>(R.id.clock_needle_theme_text).setOnClickListener {
            if (BuildConfig.FLAVOR == "lite") {
                Toast.makeText(requireContext(), R.string.only_full_version, Toast.LENGTH_SHORT).show()
            } else {
                val clockNeedle = WeakReference(ClockNeedle(clock))
                clockNeedle.get()?.show(parentFragmentManager, "null")
            }
        }

        view.findViewById<TextView>(R.id.clock_motion_type_text).setOnClickListener {
            val clockMotionType = WeakReference(ClockMotionType(clock))
            clockMotionType.get()?.show(parentFragmentManager, "null")
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
