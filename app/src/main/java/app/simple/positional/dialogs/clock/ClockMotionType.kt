package app.simple.positional.dialogs.clock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RadioButton
import app.simple.positional.R
import app.simple.positional.preference.ClockPreferences
import app.simple.positional.ui.Clock
import app.simple.positional.views.CustomBottomSheetDialog
import java.lang.ref.WeakReference

class ClockMotionType(private val clock: WeakReference<Clock>) : CustomBottomSheetDialog() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
    }

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

        // This will prevent the underlying dialog from dimming preventing a flashy animation that can cause some issues to some users
        this.dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        setButton(ClockPreferences().getMovementType(requireContext()))

        smooth.setOnClickListener {
            setButton(true)
            clock.get()?.setMotionDelay(true)
        }

        tick.setOnClickListener {
            setButton(false)
            clock.get()?.setMotionDelay(false)
        }
    }

    private fun setButton(value: Boolean) {
        smooth.isChecked = value
        tick.isChecked = !value
        ClockPreferences().setMovementType(value, requireContext())
    }
}