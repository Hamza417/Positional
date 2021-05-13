package app.simple.positional.util

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.animation.ArgbEvaluatorCompat

@Suppress("unused")
object ColorUtils {
    fun ViewGroup.animateColorChange(endColor: Int) {
        val colorAnim = ValueAnimator.ofObject(ArgbEvaluatorCompat(), this.backgroundTintList?.defaultColor, endColor)
        colorAnim.duration = 1000
        colorAnim.interpolator = DecelerateInterpolator(1.5F)
        colorAnim.addUpdateListener { animation -> this.backgroundTintList = ColorStateList.valueOf(animation.animatedValue as Int) }
        colorAnim.start()
    }

    fun TextView.animateColorChange(endColor: Int) {
        val colorAnim = ValueAnimator.ofObject(ArgbEvaluatorCompat(), this.currentTextColor, endColor)
        colorAnim.duration = 1000L
        colorAnim.interpolator = DecelerateInterpolator(1.5F)
        colorAnim.addUpdateListener { animation -> this.setTextColor(animation.animatedValue as Int) }
        colorAnim.start()
    }

    fun AppCompatButton.animateColorChange(endColor: Int) {
        val colorAnim = ValueAnimator.ofObject(ArgbEvaluatorCompat(), this.backgroundTintList!!.defaultColor, endColor)
        colorAnim.duration = 500L
        colorAnim.interpolator = DecelerateInterpolator(1.5F)
        colorAnim.addUpdateListener { animation -> this.backgroundTintList = ColorStateList.valueOf(animation.animatedValue as Int) }
        colorAnim.start()
    }

    @ColorInt
    fun Context.resolveAttrColor(@AttrRes attr: Int): Int {
        val a = theme.obtainStyledAttributes(intArrayOf(attr))
        val color: Int
        try {
            color = a.getColor(0, 0)
        } finally {
            a.recycle()
        }
        return color
    }

    fun changeAlpha(origColor: Int, userInputAlpha: Int): Int {
        return origColor and 0x00ffffff or (userInputAlpha shl 24)
    }
}
