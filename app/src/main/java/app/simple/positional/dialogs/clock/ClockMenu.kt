package app.simple.positional.dialogs.clock

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.ui.Clock
import app.simple.positional.views.CustomBottomSheetDialog
import java.lang.ref.WeakReference

class ClockMenu(private val clock: WeakReference<Clock>) : CustomBottomSheetDialog() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_clock_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (BuildConfig.FLAVOR == "lite") {
            view.findViewById<TextView>(R.id.clock_needle_theme_text).setTextColor(Color.GRAY)
        }

        view.findViewById<LinearLayout>(R.id.clock_needle_theme).setOnClickListener {
            if (BuildConfig.FLAVOR == "lite") {
                Toast.makeText(requireContext(), "This feature is only available in full version", Toast.LENGTH_LONG).show()
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