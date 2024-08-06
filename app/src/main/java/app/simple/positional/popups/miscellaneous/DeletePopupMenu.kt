package app.simple.positional.popups.miscellaneous

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import app.simple.positional.R
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout
import app.simple.positional.decorations.popup.BasePopupWindow
import app.simple.positional.decorations.ripple.DynamicRippleTextView

class DeletePopupMenu(anchor: View, onDeleted: () -> Unit) : BasePopupWindow() {

    init {
        val contentView = LayoutInflater.from(anchor.context).inflate(R.layout.popup_delete_confirmation,
                DynamicCornerLinearLayout(anchor.context))

        init(contentView, anchor, Gravity.END or Gravity.CENTER_VERTICAL, 2)

        contentView.findViewById<DynamicRippleTextView>(R.id.menu_sure).setOnClickListener {
            onDeleted().also {
                dismiss()
            }
        }

        contentView.findViewById<DynamicRippleTextView>(R.id.menu_cancel).setOnClickListener {
            dismiss()
        }
    }
}
