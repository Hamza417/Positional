package app.simple.positional.decorations.measure

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

class MeasureToolbar : DynamicCornerLinearLayout {

    private lateinit var add: DynamicRippleImageButton
    private lateinit var measures: DynamicRippleImageButton
    private lateinit var menu: DynamicRippleImageButton

    private var measureToolbarCallbacks: MeasureToolbarCallbacks? = null

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
        val view = LayoutInflater.from(context).inflate(R.layout.toolbar_measure, this, true)

        setPadding(resources.getDimensionPixelSize(R.dimen.toolbar_padding).div(2))

        // Set margin
        post {
            val params = layoutParams as MarginLayoutParams
            params.setMargins(
                paddingLeft, StatusBarHeight.getStatusBarHeight(resources) + paddingTop, paddingRight, paddingBottom)
            layoutParams = params

            // set wrap content width
            layoutParams.width = LayoutParams.MATCH_PARENT
        }

        measures = view.findViewById(R.id.measures)
        menu = view.findViewById(R.id.menu)
        add = view.findViewById(R.id.add)

        measures.setOnClickListener { v ->
            measureToolbarCallbacks?.onMeasures(v)
        }

        menu.setOnClickListener { v ->
            measureToolbarCallbacks?.onMenu(v)
        }

        add.setOnClickListener { v ->
            measureToolbarCallbacks?.onAdd(v)
        }
    }

    fun hide() {
        animate()
            .translationY((height * -1).toFloat())
            .alpha(0f)
            .setInterpolator(DecelerateInterpolator(1.5f))
            .start()

        measures.isClickable = false
        menu.isClickable = false
        isClickable = false
    }

    fun show() {
        animate()
            .translationY(0f)
            .alpha(1f)
            .setInterpolator(DecelerateInterpolator(1.5f))
            .start()

        measures.isClickable = true
        menu.isClickable = true
        isClickable = true
    }

    fun setMeasureToolbarCallbacks(measureToolbarCallbacks: MeasureToolbarCallbacks) {
        this.measureToolbarCallbacks = measureToolbarCallbacks
    }
}
