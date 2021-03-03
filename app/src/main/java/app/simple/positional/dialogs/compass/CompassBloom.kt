package app.simple.positional.dialogs.compass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import app.simple.positional.R
import app.simple.positional.preference.CompassPreference
import app.simple.positional.ui.Compass
import app.simple.positional.views.CustomBottomSheetDialogFragment
import java.lang.ref.WeakReference

class CompassBloom(private val weakReference: WeakReference<Compass>) : CustomBottomSheetDialogFragment() {

    private lateinit var orange: RadioButton
    private lateinit var purple: RadioButton
    private lateinit var flower: RadioButton
    private lateinit var petals: RadioButton
    private lateinit var octagon: RadioButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_compass_bloom, container, false)

        orange = view.findViewById(R.id.bloom_orange)
        purple = view.findViewById(R.id.bloom_purple)
        flower = view.findViewById(R.id.bloom_flower)
        petals = view.findViewById(R.id.bloom_petals)
        octagon = view.findViewById(R.id.bloom_octagon)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setButtons(CompassPreference.getFlowerBloomTheme())

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

        octagon.setOnClickListener {
            setValue(4)
        }
    }

    private fun setValue(value: Int) {
        weakReference.get()?.setFlowerTheme(value)
        setButtons(value)
    }

    private fun setButtons(value: Int) {
        CompassPreference.setFlowerBloom(value)

        orange.isChecked = value == 0
        purple.isChecked = value == 1
        flower.isChecked = value == 2
        petals.isChecked = value == 3
        octagon.isChecked = value == 4
    }
}