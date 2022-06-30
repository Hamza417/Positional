package app.simple.positional.popups.directions

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import app.simple.positional.R
import app.simple.positional.decorations.popup.BasePopupWindow
import app.simple.positional.decorations.popup.PopupLinearLayout
import app.simple.positional.decorations.ripple.DynamicRippleTextView

class PopupDirectionsMenu(anchor: View) : BasePopupWindow() {

    private var popupDirectionsCallbacks: PopupDirectionsCallbacks? = null

    init {
        val contentView = LayoutInflater.from(anchor.context).inflate(R.layout.popup_directions, PopupLinearLayout(anchor.context))
        init(contentView, anchor, Gravity.END or Gravity.CENTER_VERTICAL, 2)

        contentView.findViewById<DynamicRippleTextView>(R.id.menu_set).setOnClickListener {
            popupDirectionsCallbacks?.onSet().also {
                dismiss()
            }
        }

        contentView.findViewById<DynamicRippleTextView>(R.id.menu_delete).setOnClickListener {
            popupDirectionsCallbacks?.onDelete().also {
                dismiss()
            }
        }
    }

    fun setOnPopupCallbacksListener(popupDirectionsCallbacks: PopupDirectionsCallbacks) {
        this.popupDirectionsCallbacks = popupDirectionsCallbacks
    }

    companion object {
        interface PopupDirectionsCallbacks {
            fun onSet()
            fun onDelete()
        }
    }
}