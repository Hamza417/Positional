package app.simple.positional.popups.location

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import app.simple.positional.R
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout
import app.simple.positional.decorations.popup.BasePopupWindow
import app.simple.positional.decorations.ripple.DynamicRippleTextView

class PopupCoordinateBox(view: View) : BasePopupWindow() {
    private lateinit var popupCoordinateBoxCallbacks: PopupCoordinateBoxCallbacks

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_coordinates_box,
                DynamicCornerLinearLayout(view.context))

        init(contentView, view, Gravity.END or Gravity.CENTER_VERTICAL, 2)

        contentView.findViewById<DynamicRippleTextView>(R.id.share).setOnClickListener {
            popupCoordinateBoxCallbacks.send().also {
                dismiss()
            }
        }
    }

    fun setPopupCoordinateBoxCallbacks(popupCoordinateBoxCallbacks: PopupCoordinateBoxCallbacks) {
        this.popupCoordinateBoxCallbacks = popupCoordinateBoxCallbacks
    }

    companion object {
        interface PopupCoordinateBoxCallbacks {
            fun send()
        }
    }
}