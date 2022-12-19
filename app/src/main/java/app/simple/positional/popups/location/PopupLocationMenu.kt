package app.simple.positional.popups.location

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import app.simple.positional.R
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout
import app.simple.positional.decorations.ripple.DynamicRippleTextView
import app.simple.positional.extensions.maps.MapsCallbacks
import app.simple.positional.util.ViewUtils

class PopupLocationMenu(anchor: View, x: Float, y: Float) : PopupWindow() {

    private var mapsCallbacks: MapsCallbacks? = null
    private val target: DynamicRippleTextView
    private val navigate: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(anchor.context)
            .inflate(R.layout.popup_location_map, DynamicCornerLinearLayout(anchor.context), true)

        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        contentView.clipToOutline = false
        width = contentView.measuredWidth
        height = contentView.measuredHeight
        animationStyle = R.style.PopupAnimation
        isClippingEnabled = false
        isFocusable = true
        elevation = 50F
        overlapAnchor = true

        ViewUtils.addShadow(contentView)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            setIsClippedToScreen(false)
            setIsLaidOutInScreen(true)
        }

        setContentView(contentView)

        target = contentView.findViewById(R.id.map_menu_target)
        navigate = contentView.findViewById(R.id.map_menu_navigate)

        target.setOnClickListener {
            mapsCallbacks?.onTargetAdd().also {
                dismiss()
            }
        }

        navigate.setOnClickListener {
            mapsCallbacks?.onNavigate().also {
                dismiss()
            }
        }

        showAsDropDown(anchor, x.toInt() - width / 2, y.toInt() - height / 2, Gravity.NO_GRAVITY)
        ViewUtils.dimBehind(contentView)
    }

    fun setOnMapsCallBackListener(mapsCallbacks: MapsCallbacks) {
        this.mapsCallbacks = mapsCallbacks
    }
}