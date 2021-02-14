package app.simple.positional.views

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.widget.ImageViewCompat
import app.simple.positional.R
import app.simple.positional.constants.SpeedometerConstants
import app.simple.positional.util.AsyncImageLoader
import app.simple.positional.util.BitmapHelper
import app.simple.positional.util.BitmapHelper.toBitmap
import com.google.android.material.animation.ArgbEvaluatorCompat

class Speedometer constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    /**
     * Make these values public before setting them programmatically
     */
    private var lastColor = Color.parseColor("#363636")
    private var isGradientNeedle = false
    private var firstGradientColor = 0
    private var colorfulNeedle = false
    private var whichImage = 0

    /**
     * Views
     */
    private val needle: ImageView
    private val dial: ImageView
    private var objectAnimator: ObjectAnimator? = null
    private var colorAnimation: ValueAnimator? = null

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.speedometer, this, true)
        needle = view.findViewById(R.id.speedometer_needle)
        dial = view.findViewById(R.id.speedometer_dial)

        context.theme.obtainStyledAttributes(attrs, R.styleable.Speedometer, 0, 0).apply {
            try {
                isGradientNeedle = getBoolean(R.styleable.Speedometer_gradient_needle, false)
                colorfulNeedle = getBoolean(R.styleable.Speedometer_colorful_needle, false)
                firstGradientColor = getColor(R.styleable.Speedometer_first_gradient_color, Color.parseColor("#009bce"))
            } finally {
                recycle()
            }
        }
    }

    fun setSpeedValue(value: Float) {
        if (value < 300F) {
            if (whichImage == 0) {
                AsyncImageLoader.loadImageResources(R.drawable.speedometer_dial_normal_range, dial, context, 0)
                whichImage = 1
            }
        } else if (value > 300F) {
            if (whichImage == 1) {
                AsyncImageLoader.loadImageResources(R.drawable.speedometer_dial_medium_range, dial, context, 0)
                whichImage = 0
            }
        }
        println(value / 2)
        rotateNeedle(value)
    }

    private fun rotateNeedle(value: Float) {
        objectAnimator = ObjectAnimator.ofFloat(needle, "rotation", needle.rotation, if (value > 300F) value + 60F else value)
        objectAnimator!!.duration = getAnimationDuration(value)
        objectAnimator!!.interpolator = DecelerateInterpolator(1.5F)
        objectAnimator!!.setAutoCancel(true)
        objectAnimator!!.start()

        if (colorfulNeedle) {
            animateColorChange(value = value)
        }
    }

    private fun animateColorChange(value: Float) {
        colorAnimation = ValueAnimator.ofObject(ArgbEvaluatorCompat(), lastColor, SpeedometerConstants.getSpeedometerColor(value / 2))
        colorAnimation!!.duration = getAnimationDuration(value)
        colorAnimation!!.interpolator = AccelerateDecelerateInterpolator()
        colorAnimation!!.addUpdateListener { animation ->
            lastColor = animation.animatedValue as Int
            if (isGradientNeedle) {
                needle.setImageBitmap(BitmapHelper.addLinearGradient(
                        R.drawable.speedometer_needle.toBitmap(context, 500),
                        intArrayOf(animation.animatedValue as Int, firstGradientColor),
                        needle.height / 4F
                ))
            } else {
                ImageViewCompat.setImageTintList(
                        needle,
                        ColorStateList.valueOf(animation.animatedValue as Int))
            }
        }
        colorAnimation!!.start()
    }

    private fun getAnimationDuration(value: Float): Long {
        return if (value == 0f) {
            3000L
        } else {
            500L
        }
    }

    /**
     * Clears the references and animation of the objects.
     * It is recommended to call this in onDestroy or before
     * a view is being recycled
     */
    fun clear() {
        objectAnimator?.cancel()
        colorAnimation?.cancel()
        needle.clearAnimation()
    }
}
