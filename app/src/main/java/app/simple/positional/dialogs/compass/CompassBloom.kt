package app.simple.positional.dialogs.compass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RadioButton
import app.simple.positional.R
import app.simple.positional.preference.CompassPreference
import app.simple.positional.ui.Compass
import app.simple.positional.views.CustomBottomSheetDialog
import java.lang.ref.WeakReference

class CompassBloom(private val weakReference: WeakReference<Compass>) : CustomBottomSheetDialog() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        retainInstance = true
    }

    private lateinit var orange: RadioButton
    private lateinit var purple: RadioButton
    private lateinit var flower: RadioButton
    private lateinit var petals: RadioButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_compass_bloom, container, false)

        orange = view.findViewById(R.id.bloom_orange)
        purple = view.findViewById(R.id.bloom_purple)
        flower = view.findViewById(R.id.bloom_flower)
        petals = view.findViewById(R.id.bloom_petals)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // This will prevent the underlying dialog from dimming preventing a flashy animation that can cause some issues to some users
        this.dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

        setButtons(CompassPreference().getFlowerBloomTheme(requireContext()))

        orange.setOnClickListener {
            setValue(0)
        }

        purple.setOnClickListener {
            setValue(1)
        }

        flower.setOnClickListener {
            setValue(2)
        }

        petals.setOnClickListener {
            setValue(3)
        }
    }

    private fun setValue(value: Int) {
        weakReference.get()?.setFlowerTheme(value)
        setButtons(value)
    }

    private fun setButtons(value: Int) {
        CompassPreference().setFlowerBloom(value, requireContext())

        orange.isChecked = value == 0
        purple.isChecked = value == 1
        flower.isChecked = value == 2
        petals.isChecked = value == 3
    }
}