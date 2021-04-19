package app.simple.positional.dialogs.compass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import app.simple.positional.R
import app.simple.positional.decorations.switchview.SwitchView
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.preference.CompassPreference
import app.simple.positional.preference.CompassPreference.isFlowerBloomOn
import app.simple.positional.preference.CompassPreference.setDirectionCode
import app.simple.positional.preference.CompassPreference.setFlowerBloom

class CompassMenu : CustomBottomSheetDialogFragment() {

    private lateinit var toggleFlower: SwitchView
    private lateinit var toggleCode: SwitchView
    private lateinit var bloomText: TextView
    private lateinit var blooms: TextView
    private lateinit var bloomSwitchContainer: LinearLayout
    private lateinit var codeSwitchContainer: LinearLayout
    private lateinit var speed: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_compass_menu, container, false)

        toggleFlower = view.findViewById(R.id.toggle_flower)
        toggleCode = view.findViewById(R.id.toggle_code)
        bloomText = view.findViewById(R.id.compass_bloom_text)
        blooms = view.findViewById(R.id.compass_bloom_skins_text)
        bloomSwitchContainer = view.findViewById(R.id.bloom_switch_container)
        codeSwitchContainer = view.findViewById(R.id.compass_menu_show_code)
        speed = view.findViewById(R.id.compass_speed)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toggleCode.isChecked = CompassPreference.getDirectionCode()
        toggleFlower.isChecked = isFlowerBloomOn()

        toggleFlower.setOnCheckedChangeListener { isChecked ->
            setFlowerBloom(isChecked)
        }

        blooms.setOnClickListener {
            CompassBloom().show(parentFragmentManager, "null")
        }

        bloomSwitchContainer.setOnClickListener {
            toggleFlower.isChecked = !toggleFlower.isChecked
        }

        codeSwitchContainer.setOnClickListener {
            toggleCode.isChecked = !toggleCode.isChecked
        }

        toggleCode.setOnCheckedChangeListener { isChecked ->
            setDirectionCode(isChecked)
        }

        speed.setOnClickListener {
            CompassSpeed().show(parentFragmentManager, "null")
        }
    }
}
