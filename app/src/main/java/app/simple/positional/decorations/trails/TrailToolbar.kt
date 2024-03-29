package app.simple.positional.decorations.trails

import android.animation.LayoutTransition
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.DecelerateInterpolator
import androidx.core.view.setPadding
import app.simple.positional.R
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.preferences.MainPreferences.getCornerRadius
import app.simple.positional.util.ConditionUtils.invert
import app.simple.positional.util.StatusBarHeight
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

class TrailToolbar : DynamicCornerLinearLayout {

    private lateinit var trails: DynamicRippleImageButton
    private lateinit var menu: DynamicRippleImageButton
    private lateinit var add: DynamicRippleImageButton
    private var trailToolsCallbacks: TrailToolsCallbacks? = null

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setProperties()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setProperties()
    }

    private fun setProperties() {
        initViews()
        layoutTransition = LayoutTransition()

        val shapeAppearanceModel = ShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, getCornerRadius().toFloat())
                .build()

        if (StatusBarHeight.isLandscape(context).invert()) {
            background = MaterialShapeDrawable(shapeAppearanceModel)
        }
    }

    private fun initViews() {
        val view = LayoutInflater.from(context).inflate(R.layout.toolbar_trail_maps, this, true)

        setPadding(resources.getDimensionPixelSize(R.dimen.toolbar_padding).div(2))

        // Set margin
        post {
            val params = layoutParams as MarginLayoutParams
            params.setMargins(paddingLeft, StatusBarHeight.getStatusBarHeight(resources) + paddingTop, paddingRight, paddingBottom)
            layoutParams = params

            // set wrap content width
            layoutParams.width = LayoutParams.MATCH_PARENT
        }

        trails = view.findViewById(R.id.trail_flag)
        menu = view.findViewById(R.id.trail_menu)
        add = view.findViewById(R.id.trail_add)

        trails.setOnClickListener {
            trailToolsCallbacks?.onTrailsClicked()
        }

        menu.setOnClickListener {
            trailToolsCallbacks?.onMenuClicked()
        }

        add.setOnClickListener {
            trailToolsCallbacks?.onAddClicked()
        }
    }

    fun hide() {
        animate().translationY((height * -1).toFloat()).alpha(0f).setInterpolator(DecelerateInterpolator(1.5f)).start()
        trails.isClickable = false
        menu.isClickable = false
        isClickable = false
    }

    fun show() {
        animate().translationY(0f).alpha(1f).setInterpolator(DecelerateInterpolator(1.5f)).start()
        trails.isClickable = true
        menu.isClickable = true
        isClickable = true
    }

    fun setOnTrailToolbarEventListener(trailToolsCallbacks: TrailToolsCallbacks) {
        this.trailToolsCallbacks = trailToolsCallbacks
    }

    companion object {
        interface TrailToolsCallbacks {
            fun onTrailsClicked()
            fun onAddClicked()
            fun onMenuClicked()
        }
    }
}