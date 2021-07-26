package app.simple.positional.popups.miscellaneous

import android.view.Gravity
import android.view.View
import app.simple.positional.R
import app.simple.positional.decorations.popup.BasePopupWindow
import app.simple.positional.decorations.ripple.DynamicRippleTextView

class DeletePopupMenu(contentView: View, anchor: View) : BasePopupWindow() {

    private lateinit var popupDeleteCallbacks: PopupDeleteCallbacks

    init {
        init(contentView, anchor, Gravity.END or Gravity.CENTER_VERTICAL, 2)

        contentView.findViewById<DynamicRippleTextView>(R.id.menu_sure).setOnClickListener {
            popupDeleteCallbacks.delete().also {
                dismiss()
            }
        }

        contentView.findViewById<DynamicRippleTextView>(R.id.menu_cancel).setOnClickListener {
            dismiss()
        }
    }

    fun setOnPopupCallbacksListener(popupDeleteCallbacks: PopupDeleteCallbacks) {
        this.popupDeleteCallbacks = popupDeleteCallbacks
    }

    companion object {
        interface PopupDeleteCallbacks {
            fun delete()
        }
    }
}