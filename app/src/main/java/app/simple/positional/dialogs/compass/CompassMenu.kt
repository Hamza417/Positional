package app.simple.positional.dialogs.compass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import app.simple.positional.BuildConfig
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

        if (BuildConfig.FLAVOR != "lite") {
            toggle_flower.isChecked = CompassPreference().isFlowerBloom(requireContext())
        } else {
            compass_appearance_text_view.text = "${compass_appearance_text_view.text} (requires full version)"
        }

        toggle_flower.setOnCheckedChangeListener { _, isChecked ->
            if (BuildConfig.FLAVOR != "lite") {
                weakReference.get()?.setFlower(isChecked)
                CompassPreference().setFlowerBloom(isChecked, requireContext())
            } else {
                Toast.makeText(requireContext(), "This feature is only available in full version", Toast.LENGTH_LONG).show()
                toggle_flower.isChecked = false
            }
        }

        compass_bloom_theme.setOnClickListener {
            if (BuildConfig.FLAVOR != "lite") {
                val compassBloom = CompassBloom(weakReference)
                compassBloom.show(parentFragmentManager, "null")
            } else {
                Toast.makeText(requireContext(), "This feature is only available in full version", Toast.LENGTH_LONG).show()
                toggle_flower.isChecked = false
            }
        }

        compass_speed.setOnClickListener {
            val compassSpeed = WeakReference(CompassSpeed(weakReference))
            compassSpeed.get()?.show(parentFragmentManager, "null")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        weakReference.clear()
    }
}
