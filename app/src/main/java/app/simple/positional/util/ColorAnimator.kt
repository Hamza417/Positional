package app.simple.positional.util

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.animation.ArgbEvaluatorCompat

@Suppress("unused")
object ColorAnimator {
    fun TextView.animateColorChange(endColor: Int) {
        val colorAnim = ValueAnimator.ofObject(ArgbEvaluatorCompat(), this.currentTextColor, endColor)
        colorAnim.duration = 1000
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
}
