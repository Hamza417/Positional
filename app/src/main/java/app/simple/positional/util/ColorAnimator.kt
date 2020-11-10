package app.simple.positional.util

import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import com.google.android.material.animation.ArgbEvaluatorCompat


fun animateColorChange(view: TextView, startColor: Int, endColor: Int) {
    val colorAnim = ValueAnimator.ofObject(ArgbEvaluatorCompat(), startColor, endColor)
    colorAnim.duration = 1000
    colorAnim.interpolator = AccelerateDecelerateInterpolator()
    colorAnim.addUpdateListener { animation -> view.setTextColor(animation.animatedValue as Int) }
    colorAnim.start()
}