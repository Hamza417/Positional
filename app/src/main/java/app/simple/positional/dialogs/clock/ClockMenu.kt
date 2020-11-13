package app.simple.positional.dialogs.clock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.ui.Clock
import app.simple.positional.views.CustomBottomSheetDialog
import kotlinx.android.synthetic.main.dialog_clock_menu.*
import java.lang.ref.WeakReference

class ClockMenu(private val clock: WeakReference<Clock>) : CustomBottomSheetDialog() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_clock_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (BuildConfig.FLAVOR == "lite") {
            clock_appearance_text.text = "${clock_appearance_text.text} (requires full version)"
        }

        clock_needle_theme.setOnClickListener {
            if (BuildConfig.FLAVOR == "lite") {
                Toast.makeText(requireContext(), "This feature is only available in full version", Toast.LENGTH_LONG).show()
            } else {
                val clockNeedle = WeakReference(ClockNeedle(clock))
                clockNeedle.get()?.show(parentFragmentManager, "null")
            }
        }

        clock_motion_type.setOnClickListener {
            val clockMotionType = WeakReference(ClockMotionType(clock))
            clockMotionType.get()?.show(parentFragmentManager, "null")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        clock.clear()
    }
}