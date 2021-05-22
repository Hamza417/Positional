package app.simple.positional.util

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
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

    fun getMapColorFilter(): ColorMatrixColorFilter {
        //taking cue from https://medium.com/square-corner-blog/welcome-to-the-color-matrix-64d112e3f43d
        val inverseMatrix = ColorMatrix(floatArrayOf(
                -1.0f, 0.0f, 0.0f, 0.0f, 255f,
                0.0f, -1.0f, 0.0f, 0.0f, 255f,
                0.0f, 0.0f, -1.0f, 0.0f, 255f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
        ))

        val destinationColor: Int = Color.parseColor("#FF2A2A2A")
        val lr: Float = (255.0f - Color.red(destinationColor)) / 150.0f
        val lg: Float = (255.0f - Color.green(destinationColor)) / 255.0f
        val lb: Float = (255.0f - Color.blue(destinationColor)) / 255.0f
        val grayscaleMatrix = ColorMatrix(floatArrayOf(
                lr, lg, lb, 0f, 0f,  //
                lr, lg, lb, 0f, 0f,  //
                lr, lg, lb, 0f, 0f, 0f, 0f, 0f, 0f, 255f))
        grayscaleMatrix.preConcat(inverseMatrix)
        val dr: Int = Color.red(destinationColor)
        val dg: Int = Color.green(destinationColor)
        val db: Int = Color.blue(destinationColor)
        val drf = dr / 255f
        val dgf = dg / 255f
        val dbf = db / 255f
        val tintMatrix = ColorMatrix(floatArrayOf(
                drf, 0f, 0f, 0f, 0f, 0f, dgf, 0f, 0f, 0f, 0f, 0f, dbf, 0f, 0f, 0f, 0f, 0f, 1f, 0f))
        tintMatrix.preConcat(grayscaleMatrix)

        /**
         * Change contrast here, increasing scale value
         * will increase the contrast and decreasing will
         * decrease the contrast
         */
        val lDestination = drf * lr + dgf * lg + dbf * lb
        val scale = 1f + lDestination
        val translate = 1.5F - scale * 0.9f
        val scaleMatrix = ColorMatrix(floatArrayOf(
                scale, 0f, 0f, 0f, dr * translate, 0f, scale, 0f, 0f, dg * translate, 0f, 0f, scale, 0f, db * translate, 0f, 0f, 0f, 1f, 0f))
        scaleMatrix.preConcat(tintMatrix)
        return ColorMatrixColorFilter(scaleMatrix)
    }

    fun createContrast(contrast: Float): ColorMatrixColorFilter {
        val scale: Float = contrast + 1f
        val translate = (-.5f * scale + .5f) * 255f
        val array = floatArrayOf(
                scale, 0f, 0f, 0f, translate, 0f, scale, 0f, 0f, translate, 0f, 0f, scale, 0f, translate, 0f, 0f, 0f, 1f, 0f)
        val matrix = ColorMatrix(array)
        return ColorMatrixColorFilter(matrix)
    }
}
