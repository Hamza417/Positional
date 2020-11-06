package app.simple.positional.dialogs.compass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.positional.R
import app.simple.positional.preference.CompassPreference
import app.simple.positional.ui.Compass
import app.simple.positional.views.CustomBottomSheetDialog
import kotlinx.android.synthetic.main.dialog_compass_menu.*
import java.lang.ref.WeakReference

class CompassMenu(private val weakReference: WeakReference<Compass>) : CustomBottomSheetDialog() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_compass_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toggle_parallax.isChecked = CompassPreference().getParallax(requireContext())
        calibrate_parallax.isClickable = CompassPreference().getParallax(requireContext())

        compass_dial_theme.setOnClickListener {
            val compassDial = WeakReference(CompassDial(weakReference))
            compassDial.get()?.show(parentFragmentManager, "null")
        }

        compass_needle_theme.setOnClickListener {
            val compassNeedle = WeakReference(CompassNeedle(weakReference))
            compassNeedle.get()?.show(parentFragmentManager, "null")
        }

        toggle_parallax.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                weakReference.get()?.toggleParallax(false)
                calibrate_parallax.isClickable = false
            } else {
                weakReference.get()?.toggleParallax(true)
                calibrate_parallax.isClickable = true
            }
        }

        calibrate_parallax.setOnClickListener {
            weakReference.get()?.calibrate()
        }
    }
}
