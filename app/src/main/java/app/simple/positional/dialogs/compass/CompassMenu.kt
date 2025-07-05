package app.simple.positional.dialogs.compass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleLinearLayout
import app.simple.positional.decorations.ripple.DynamicRippleTextView
import app.simple.positional.decorations.switchview.SwitchView
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.preferences.CompassPreferences
import app.simple.positional.preferences.CompassPreferences.isFlowerBloomOn
import app.simple.positional.preferences.CompassPreferences.setDirectionCode
import app.simple.positional.preferences.CompassPreferences.setFlowerBloom

class CompassMenu : CustomBottomSheetDialogFragment() {

    private lateinit var toggleFlower: SwitchView
    private lateinit var toggleCode: SwitchView
    private lateinit var toggleGimbalLock: SwitchView
    private lateinit var bloomSkins: DynamicRippleTextView
    private lateinit var bloomSwitchContainer: DynamicRippleLinearLayout
    private lateinit var codeSwitchContainer: DynamicRippleLinearLayout
    private lateinit var gimbalLockSwitchContainer: DynamicRippleLinearLayout
    private lateinit var physicalProperties: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_compass_menu, container, false)

        toggleFlower = view.findViewById(R.id.toggle_flower)
        toggleCode = view.findViewById(R.id.toggle_code)
        bloomSkins = view.findViewById(R.id.compass_bloom_skins_text)
        toggleGimbalLock = view.findViewById(R.id.toggle_gimbal_lock)
        bloomSwitchContainer = view.findViewById(R.id.bloom_switch_container)
        codeSwitchContainer = view.findViewById(R.id.compass_menu_show_code)
        gimbalLockSwitchContainer = view.findViewById(R.id.compass_menu_gimbal_lock)
        physicalProperties = view.findViewById(R.id.compass_speed)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toggleCode.isChecked = CompassPreferences.getDirectionCode()
        toggleFlower.isChecked = isFlowerBloomOn()
        toggleGimbalLock.isChecked = CompassPreferences.isUsingGimbalLock()

        toggleFlower.setOnCheckedChangeListener { isChecked ->
            setFlowerBloom(isChecked)
        }

        bloomSkins.setOnClickListener {
            CompassBloom().show(parentFragmentManager, "compass_bloom")
            dismiss()
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

        toggleGimbalLock.setOnCheckedChangeListener {
            CompassPreferences.setUseGimbalLock(it)
        }

        gimbalLockSwitchContainer.setOnClickListener {
            toggleGimbalLock.isChecked = !toggleGimbalLock.isChecked
        }

        physicalProperties.setOnClickListener {
            CompassPhysicalProperties().show(parentFragmentManager, "compass_properties")
            dismiss()
        }
    }
}
