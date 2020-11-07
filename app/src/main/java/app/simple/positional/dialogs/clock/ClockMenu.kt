package app.simple.positional.dialogs.clock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        clock_face_theme.setOnClickListener {
            val clockFaceTheme = WeakReference(ClockFace(clock = clock))
            clockFaceTheme.get()?.show(parentFragmentManager, "null")
        }

        clock_needle_theme.setOnClickListener {
            val clockNeedleSkins = WeakReference(ClockNeedle(clock))
            clockNeedleSkins.get()?.show(parentFragmentManager, "null")
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