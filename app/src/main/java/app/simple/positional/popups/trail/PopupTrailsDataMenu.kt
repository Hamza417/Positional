package app.simple.positional.popups.trail

import android.view.Gravity
import android.view.View
import app.simple.positional.R
import app.simple.positional.decorations.popup.BasePopupWindow
import app.simple.positional.decorations.ripple.DynamicRippleTextView

class PopupTrailsDataMenu(contentView: View, anchor: View) : BasePopupWindow() {

    private lateinit var popupTrailsCallbacks: PopupTrailsCallbacks

    init {
        init(contentView, anchor, Gravity.END or Gravity.CENTER_VERTICAL)

        contentView.findViewById<DynamicRippleTextView>(R.id.menu_delete).setOnClickListener {
            popupTrailsCallbacks.onDelete().also {
                dismiss()
            }
        }
    }

    fun setOnPopupCallbacksListener(popupDeleteCallbacks: PopupTrailsCallbacks) {
        this.popupTrailsCallbacks = popupDeleteCallbacks
    }

    companion object {
        interface PopupTrailsCallbacks {
            fun onDelete()
        }
    }
}