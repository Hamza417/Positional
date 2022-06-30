package app.simple.positional.dialogs.direction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.positional.R
import app.simple.positional.decorations.switchview.SwitchView
import app.simple.positional.decorations.views.CustomBottomSheetDialogFragment
import app.simple.positional.preferences.DirectionPreferences

class DirectionMenu : CustomBottomSheetDialogFragment() {

    private lateinit var gimbalLock: SwitchView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_direction_menu, container, false)

        gimbalLock = view.findViewById(R.id.toggle_gimbal_lock)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gimbalLock.isChecked = DirectionPreferences.isGimbalLock()

        gimbalLock.setOnCheckedChangeListener {
            DirectionPreferences.setGimbalLock(it)
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