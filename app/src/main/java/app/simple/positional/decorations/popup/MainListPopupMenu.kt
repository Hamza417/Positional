package app.simple.positional.decorations.popup

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import app.simple.positional.R
import app.simple.positional.decorations.ripple.DynamicRippleTextView

/**
 * A customised version of popup menu that uses [PopupWindow]
 * created to replace ugly material popup menu which does not
 * provide any customizable flexibility. This on the other hand
 * uses custom layout, background, animations and also dims entire
 * window when appears. It is highly recommended to use this
 * and ditch popup menu entirely.
 */
class MainListPopupMenu(contentView: View,
        viewGroup: ViewGroup,
        xOff: Float,
        yOff: Float) : PopupWindow() {

    lateinit var popupMenuCallback: PopupMenuCallback

    init {
        setContentView(contentView)
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        width = contentView.measuredWidth
        height = contentView.measuredHeight
        animationStyle = R.style.DialogAnimation
        isClippingEnabled = false
        isFocusable = true
        elevation = 100F

        contentView.findViewById<DynamicRippleTextView>(R.id.menu_disclaimer).onClick()
        contentView.findViewById<DynamicRippleTextView>(R.id.menu_privacy_policy).onClick()
        contentView.findViewById<DynamicRippleTextView>(R.id.menu_terms_of_use).onClick()
        contentView.findViewById<DynamicRippleTextView>(R.id.menu_permissions).onClick()
        contentView.findViewById<DynamicRippleTextView>(R.id.menu_credits).onClick()
        contentView.findViewById<DynamicRippleTextView>(R.id.menu_internet_uses).onClick()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            overlapAnchor = false
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            setIsClippedToScreen(false)
            setIsLaidOutInScreen(true)
        }

        showAsDropDown(viewGroup, xOff.toInt() - width / 2, yOff.toInt() - height / 2, Gravity.NO_GRAVITY)
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int, gravity: Int) {
        super.showAsDropDown(anchor, xoff, yoff, gravity)
        dimBehind()
    }

    private fun DynamicRippleTextView.onClick() {
        this.setOnClickListener {
            popupMenuCallback.onMenuItemClicked(this.text.toString())
            dismiss()
        }
    }

    /**
     * Dim the background when PopupWindow shows
     * Should be called from [showAsDropDown] function
     * because this is when container's parent is
     * initialized
     */
    private fun dimBehind() {
        val container = contentView.rootView
        val windowManager = contentView.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = container.layoutParams as WindowManager.LayoutParams
        layoutParams.flags = layoutParams.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        layoutParams.dimAmount = 0.3f
        windowManager.updateViewLayout(container, layoutParams)
    }
}
