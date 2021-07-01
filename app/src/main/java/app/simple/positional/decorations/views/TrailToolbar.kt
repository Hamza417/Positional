package app.simple.positional.decorations.views

import android.animation.LayoutTransition
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.DecelerateInterpolator
import app.simple.positional.R
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.util.StatusBarHeight

class TrailToolbar : DynamicCornerLinearLayout {

    private lateinit var flag: DynamicRippleImageButton
    private lateinit var menu: DynamicRippleImageButton

    var onFlagClicked: () -> Unit = {}
    var onMenuClicked: () -> Unit = {}

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setProperties()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setProperties()
    }

    private fun setProperties() {
        initViews()
        layoutTransition = LayoutTransition()
    }

    private fun initViews() {
        val view = LayoutInflater.from(context).inflate(R.layout.toolbar_trail_maps, this, true)
        setPadding(resources.getDimensionPixelOffset(R.dimen.toolbar_padding),
                   resources.getDimensionPixelOffset(R.dimen.toolbar_padding) + StatusBarHeight.getStatusBarHeight(resources),
                   resources.getDimensionPixelOffset(R.dimen.toolbar_padding),
                   resources.getDimensionPixelOffset(R.dimen.toolbar_padding))

        flag = view.findViewById(R.id.trail_flag)
        menu = view.findViewById(R.id.trail_menu)

        flag.setOnClickListener {
            onFlagClicked.invoke()
        }

        menu.setOnClickListener {
            onMenuClicked.invoke()
        }
    }

    fun hide() {
        animate().translationY((height * -1).toFloat()).alpha(0f).setInterpolator(DecelerateInterpolator(1.5f)).start()
        flag.isClickable = false
        menu.isClickable = false
        isClickable = false
    }

    fun show() {
        animate().translationY(0f).alpha(1f).setInterpolator(DecelerateInterpolator(1.5f)).start()
        flag.isClickable = true
        menu.isClickable = true
        isClickable = true
    }
}