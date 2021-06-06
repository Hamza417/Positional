package app.simple.positional.popups

import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import app.simple.positional.R
import app.simple.positional.decorations.popup.BasePopupWindow
import app.simple.positional.decorations.popup.PopupMenuCallback
import app.simple.positional.decorations.ripple.DynamicRippleTextView

/**
 * A customised version of popup menu that uses [PopupWindow]
 * created to replace ugly material popup menu which does not
 * provide any customizable flexibility. This on the other hand
 * uses custom layout, background, animations and also dims entire
 * window when appears. It is highly recommended to use this
 * and ditch popup menu entirely.
 */
class LegalNotesPopupMenu(contentView: View,
        viewGroup: ViewGroup,
        xOff: Float,
        yOff: Float) : BasePopupWindow() {

    lateinit var popupMenuCallback: PopupMenuCallback

    init {
        init(contentView, viewGroup, xOff, yOff)

        contentView.findViewById<DynamicRippleTextView>(R.id.menu_disclaimer).onClick()
        contentView.findViewById<DynamicRippleTextView>(R.id.menu_privacy_policy).onClick()
        contentView.findViewById<DynamicRippleTextView>(R.id.menu_terms_of_use).onClick()
        contentView.findViewById<DynamicRippleTextView>(R.id.menu_permissions).onClick()
        contentView.findViewById<DynamicRippleTextView>(R.id.menu_credits).onClick()
        contentView.findViewById<DynamicRippleTextView>(R.id.menu_internet_uses).onClick()
    }

    private fun DynamicRippleTextView.onClick() {
        this.setOnClickListener {
            popupMenuCallback.onMenuItemClicked(this.text.toString())
            dismiss()
        }
    }
}
