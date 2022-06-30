package app.simple.positional.dialogs.direction

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.positional.R
import app.simple.positional.activities.subactivity.DirectionsActivity
import app.simple.positional.decorations.ripple.DynamicRippleTextView
import app.simple.positional.decorations.switchview.SwitchView
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.preferences.DirectionPreferences

class DirectionMenu : CustomBottomSheetDialogFragment() {

    private lateinit var gimbalLock: SwitchView
    private lateinit var mapsTarget: SwitchView
    private lateinit var directionTargets: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_direction_menu, container, false)

        gimbalLock = view.findViewById(R.id.toggle_gimbal_lock)
        mapsTarget = view.findViewById(R.id.toggle_use_target)
        directionTargets = view.findViewById(R.id.direction_target_list)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gimbalLock.isChecked = DirectionPreferences.isGimbalLock()
        mapsTarget.isChecked = DirectionPreferences.isUsingMapsTarget()

        gimbalLock.setOnCheckedChangeListener {
            DirectionPreferences.setGimbalLock(it)
        }

        mapsTarget.setOnCheckedChangeListener {
            DirectionPreferences.setUseMapsTarget(it)
        }

        directionTargets.setOnClickListener {
            startActivity(Intent(requireActivity(), DirectionsActivity::class.java))
        }
    }

    companion object {
        fun newInstance(): DirectionMenu {
            val args = Bundle()
            val fragment = DirectionMenu()
            fragment.arguments = args
            return fragment
        }
    }
}