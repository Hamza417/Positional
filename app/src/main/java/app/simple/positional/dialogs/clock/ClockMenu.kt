package app.simple.positional.dialogs.clock

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.dialogs.settings.HtmlViewer
import app.simple.positional.preference.ClockPreferences
import app.simple.positional.ui.Clock
import app.simple.positional.views.CustomBottomSheetDialog
import java.lang.ref.WeakReference

class ClockMenu(private val clock: WeakReference<Clock>) : CustomBottomSheetDialog() {

    private lateinit var defaultTimeFormatContainer: LinearLayout
    private lateinit var defaultTimeFormatSwitch: SwitchCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
    }

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

        view.findViewById<LinearLayout>(R.id.clock_needle_theme).setOnClickListener {
            if (BuildConfig.FLAVOR == "lite") {
                HtmlViewer().newInstance("Buy").show(childFragmentManager, "buy")
            } else {
                val clockNeedle = WeakReference(ClockNeedle(clock))
                clockNeedle.get()?.show(parentFragmentManager, "null")
            }
        }

        view.findViewById<LinearLayout>(R.id.clock_motion_type).setOnClickListener {
            val clockMotionType = WeakReference(ClockMotionType(clock))
            clockMotionType.get()?.show(parentFragmentManager, "null")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clock.clear()
    }
}