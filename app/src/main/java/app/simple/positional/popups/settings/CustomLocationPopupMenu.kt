package app.simple.positional.popups.settings

import android.view.View
import app.simple.positional.R
import app.simple.positional.decorations.popup.BasePopupWindow
import app.simple.positional.decorations.popup.PopupMenuCallback
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.decorations.ripple.DynamicRippleTextView

class CustomLocationPopupMenu(contentView: View, view: DynamicRippleImageButton) : BasePopupWindow() {

    private lateinit var popupMenuCallback: PopupMenuCallback

    init {
        init(contentView, view)

        contentView.findViewById<DynamicRippleTextView>(R.id.menu_custom_locations_save).onClick()
        contentView.findViewById<DynamicRippleTextView>(R.id.menu_custom_locations_set_and_save).onClick()
        contentView.findViewById<DynamicRippleTextView>(R.id.menu_custom_locations_help).onClick()
        contentView.findViewById<DynamicRippleTextView>(R.id.menu_custom_locations_clear).onClick()
        contentView.findViewById<DynamicRippleTextView>(R.id.menu_custom_locations_set_only).onClick()
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