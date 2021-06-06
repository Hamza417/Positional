package app.simple.positional.popups

import android.view.View
import android.view.ViewGroup
import app.simple.positional.R
import app.simple.positional.decorations.popup.BasePopupWindow
import app.simple.positional.decorations.popup.PopupMenuCallback
import app.simple.positional.decorations.ripple.DynamicRippleTextView

class CustomLocationPopupMenu(contentView: View, viewGroup: ViewGroup) : BasePopupWindow() {

    private lateinit var popupMenuCallback: PopupMenuCallback

    init {
        init(contentView, viewGroup)

        contentView.findViewById<DynamicRippleTextView>(R.id.menu_custom_locations_save).onClick()
        contentView.findViewById<DynamicRippleTextView>(R.id.menu_custom_locations_set_and_save).onClick()
        contentView.findViewById<DynamicRippleTextView>(R.id.menu_custom_locations_help).onClick()
    }

    private fun DynamicRippleTextView.onClick() {
        this.setOnClickListener {
            popupMenuCallback.onMenuItemClicked(this.text.toString())
            dismiss()
        }
    }

    fun setOnPopupCallbackListener(popupMenuCallback: PopupMenuCallback) {
        this.popupMenuCallback = popupMenuCallback
    }
}