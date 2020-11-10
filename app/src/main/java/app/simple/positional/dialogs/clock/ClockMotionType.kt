package app.simple.positional.dialogs.clock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.positional.R
import app.simple.positional.preference.ClockPreferences
import app.simple.positional.ui.Clock
import app.simple.positional.views.CustomBottomSheetDialog
import kotlinx.android.synthetic.main.dialog_clock_motion_type.*
import java.lang.ref.WeakReference

class ClockMotionType(private val clock: WeakReference<Clock>) : CustomBottomSheetDialog() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_clock_motion_type, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setButton(ClockPreferences().getMovementType(requireContext()))

        motion_smooth.setOnClickListener {
            setButton(true)
            clock.get()?.setMotionDelay(true)
        }

        motion_tick.setOnClickListener {
            setButton(false)
            clock.get()?.setMotionDelay(false)
        }
    }

    private fun setButton(value: Boolean) {
        motion_smooth.isChecked = value
        motion_tick.isChecked = !value
        ClockPreferences().setMovementType(value, requireContext())
    }
}