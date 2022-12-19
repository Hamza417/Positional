package app.simple.positional.popups.directions

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import app.simple.positional.R
import app.simple.positional.decorations.popup.BasePopupWindow
import app.simple.positional.decorations.popup.PopupLinearLayout
import app.simple.positional.decorations.ripple.DynamicRippleTextView

class PopupDirectionAddMenu(view: View): BasePopupWindow() {

    private var popupDirectionsAddCallbacks: PopupDirectionsAddCallbacks? = null

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_directions_add_menu, PopupLinearLayout(view.context))
        init(contentView, view, Gravity.END or Gravity.CENTER_VERTICAL, 2)

        contentView.findViewById<DynamicRippleTextView>(R.id.menu_new).setOnClickListener {
            popupDirectionsAddCallbacks?.onNew().also {
                dismiss()
            }
        }

        contentView.findViewById<DynamicRippleTextView>(R.id.menu_save_from_target).setOnClickListener {
            popupDirectionsAddCallbacks?.onSaveFromTarget().also {
                dismiss()
            }
        }
    }

    fun setOnPopupCallbacksListener(popupDirectionsCallbacks: PopupDirectionsAddCallbacks) {
        this.popupDirectionsAddCallbacks = popupDirectionsCallbacks
    }

    companion object {
        interface PopupDirectionsAddCallbacks {
            fun onNew()
            fun onSaveFromTarget()
        }
    }
}