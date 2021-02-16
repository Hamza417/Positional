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
import app.simple.positional.util.AsyncImageLoader.loadImage
import app.simple.positional.util.BitmapHelper
import app.simple.positional.util.BitmapHelper.toBitmap
import com.google.android.material.animation.ArgbEvaluatorCompat

/**
 * Shows a gauge/dial for the speed
 */
class Speedometer constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    /**
     * Make these values public before setting them programmatically
     */
    private var lastColor = Color.parseColor("#363636")
    private var isGradientNeedle = false
    private var firstGradientColor = 0
    private var colorfulNeedle = false
    private var whichImage = -1

    /**
     * [needleAngleCompensator] compensates the missing portion of
     * the speedometer gauge and the assigned value should be
     * strictly 0, 60, 120 and 180
     */
    private var needleAngleCompensator = 0F

    /**
     * Views
     */
    private val needle: ImageView
    private val dial: ImageView

    /**
     * Animation objects
     */
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
                whichImage = R.drawable.speedometer_dial_normal_range
            } finally {
                recycle()
            }
        }
    }

    fun setSpeedValue(value: Float) {
        println(value)
        setDial(value * 2)
        rotateNeedle(value * 2)
    }

    private fun setDial(value: Float) {
        when {
            value <= 300F -> {
                if (whichImage != R.drawable.speedometer_dial_normal_range) {
                    loadImage(R.drawable.speedometer_dial_normal_range, dial, context, 0)
                    whichImage = R.drawable.speedometer_dial_normal_range
                    needleAngleCompensator = 0F
                }
            }
            value in 300.01F..600F -> {
                if (whichImage != R.drawable.speedometer_dial_medium_range) {
                    loadImage(R.drawable.speedometer_dial_medium_range, dial, context, 0)
                    whichImage = R.drawable.speedometer_dial_medium_range
                    needleAngleCompensator = 60F
                }
            }
            value in 600.01F..900F -> {
                if (whichImage != R.drawable.speedometer_dial_high_range) {
                    loadImage(R.drawable.speedometer_dial_high_range, dial, context, 0)
                    whichImage = R.drawable.speedometer_dial_high_range
                    needleAngleCompensator = 120F
                }
            }
            value >= 900.01F -> {
                if (whichImage != R.drawable.speedometer_dial_max_range) {
                    loadImage(R.drawable.speedometer_dial_max_range, dial, context, 0)
                    whichImage = R.drawable.speedometer_dial_max_range
                    needleAngleCompensator = 180F
                }
            }
        }
    }

    private fun rotateNeedle(value: Float) {
        objectAnimator = ObjectAnimator.ofFloat(needle, "rotation", needle.rotation, value.reifyNeedleAngle())
        objectAnimator!!.duration = 1000L
        objectAnimator!!.interpolator = DecelerateInterpolator(1.5F)
        objectAnimator!!.setAutoCancel(true)
        objectAnimator!!.start()

        if (colorfulNeedle) {
            animateColorChange(value = value)
        }
    }

    /**
     * Status: Not in use
     *
     * Animates the needle color as the speed increases or decreases
     */
    private fun animateColorChange(value: Float) {
        colorAnimation = ValueAnimator.ofObject(ArgbEvaluatorCompat(), lastColor, SpeedometerConstants.getSpeedometerColor(value / 2.0))
        colorAnimation!!.duration = 1000L
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

    /**
     * This function will lock the value if it exceeds 600 km/h or 1200°
     * or compensates the values according to the gauge's nature using
     * the [needleAngleCompensator] variable
     *
     * the multiplication of the two is only for converting the speed
     * into angle for the needle
     *
     * Check this vector graphic to see the gauge's structure and why
     * the multiplication is necessary
     *
     * @see R.drawable.speedometer_dial_normal_range
     * @return [Float]
     */
    private fun Float.reifyNeedleAngle(): Float {
        return if (this >= 1200F) {
            1200F
        } else {
            if (this > 300F) {
                this + needleAngleCompensator
            } else {
                this
            }
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
        dial.clearAnimation()
        needle.clearAnimation()
        invalidate()
    }
}
