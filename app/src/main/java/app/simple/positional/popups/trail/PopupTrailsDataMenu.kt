package app.simple.positional.popups.trail

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import app.simple.positional.R
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout
import app.simple.positional.decorations.popup.BasePopupWindow
import app.simple.positional.decorations.ripple.DynamicRippleTextView

class PopupTrailsDataMenu(anchor: View) : BasePopupWindow() {

    private lateinit var popupTrailsCallbacks: PopupTrailsCallbacks

    init {
        val contentView = LayoutInflater.from(anchor.context).inflate(R.layout.popup_trails_data, DynamicCornerLinearLayout(anchor.context))
        init(contentView, anchor, Gravity.END or Gravity.CENTER_VERTICAL, 2)

        contentView.findViewById<DynamicRippleTextView>(R.id.menu_delete).setOnClickListener {
            popupTrailsCallbacks.onDelete().also {
                dismiss()
            }
        }

        contentView.findViewById<DynamicRippleTextView>(R.id.menu_copy).setOnClickListener {
            popupTrailsCallbacks.onCopy().also {
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
            fun onCopy()
            fun onShare()
        }
    }
}