package app.simple.positional.decorations.switchview

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.setPadding
import app.simple.positional.R
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

open class SwitchFrameLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {
    init {

        layoutParams = LayoutParams(context.resources.getDimensionPixelSize(R.dimen.switch_width), ViewGroup.LayoutParams.WRAP_CONTENT)
        setPadding(context.resources.getDimensionPixelSize(R.dimen.switch_padding))

        backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)

        val shapeAppearanceModel = ShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, 100F)
                .build()

        background = MaterialShapeDrawable(shapeAppearanceModel)
    }
}