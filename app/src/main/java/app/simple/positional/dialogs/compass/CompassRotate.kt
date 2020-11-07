package app.simple.positional.dialogs.compass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.positional.R
import app.simple.positional.preference.CompassPreference
import app.simple.positional.ui.Compass
import app.simple.positional.views.CustomBottomSheetDialog
import kotlinx.android.synthetic.main.dialog_compass_rotate.*
import java.lang.ref.WeakReference

class CompassRotate(private val weakReference: WeakReference<Compass>) : CustomBottomSheetDialog() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_compass_rotate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setButtons(CompassPreference().getRotatePreference(requireContext()))

        rotate_needle.setOnClickListener {
            setButtons(1)
            weakReference.get()?.rotateWhich(1)
        }

        rotate_dial.setOnClickListener {
            setButtons(2)
            weakReference.get()?.rotateWhich(2)
        }

        rotate_both.setOnClickListener {
            setButtons(3)
            weakReference.get()?.rotateWhich(3)
        }
    }

    private fun setButtons(value: Int) {
        rotate_needle.isChecked = value == 1
        rotate_dial.isChecked = value == 2
        rotate_both.isChecked = value == 3
    }
}