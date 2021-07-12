package app.simple.positional.util

import android.animation.Animator
import android.content.Context
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import app.simple.positional.R
import app.simple.positional.util.ColorUtils.resolveAttrColor
import app.simple.positional.util.ViewUtils.makeInvisible

object ViewUtils {
    /**
     * Dim the background when PopupWindow shows
     * Should be called from showAsDropDown function
     * because this is when container's parent is
     * initialized
     */
    fun dimBehind(contentView: View) {
        val container = contentView.rootView
        val windowManager = contentView.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = container.layoutParams as WindowManager.LayoutParams
        layoutParams.flags = layoutParams.flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
        layoutParams.dimAmount = 0.3f
        windowManager.updateViewLayout(container, layoutParams)
    }

    // @RequiresApi(28)
    /**
     * Adds outline shadows to the view using the accent color
     * of the app
     *
     * @param contentView [View] that needs to be elevated with colored
     *                    shadow
     */
    fun addShadow(contentView: View) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            contentView.outlineAmbientShadowColor = contentView.context.resolveAttrColor(R.attr.colorAppAccent)
            contentView.outlineSpotShadowColor = contentView.context.resolveAttrColor(R.attr.colorAppAccent)
        }
    }

    /**
     * Makes the view go away
     */
    fun View.makeGoAway() {
        this.visibility = View.GONE
    }

    /**
     * Makes the view go away
     *
     * @param animate adds animation to the process
     */
    fun View.makeInvisible(animate: Boolean) {
        clearAnimation()

        if (animate) {
            this.animate()
                .scaleY(0F)
                .scaleX(0F)
                .alpha(0F)
                .setInterpolator(AccelerateInterpolator())
                .setDuration(400L)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {
                            /* no-op */
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            this@makeInvisible.visibility = View.INVISIBLE
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                            /* no-op */
                        }

                        override fun onAnimationRepeat(animation: Animator?) {
                            /* no-op */
                        }
                    })
                    .start()
        } else {
            this.visibility = View.INVISIBLE
        }
    }

    /**
     * Makes the view come back
     *
     * @param animate adds animation to the process
     */
    fun View.makeVisible(animate: Boolean) {
        clearAnimation()

        if (animate) {
            this.animate()
                .scaleY(1F)
                .scaleX(1F)
                .alpha(1F)
                .setInterpolator(LinearOutSlowInInterpolator())
                .setDuration(400L)
                .setListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {
                            this@makeVisible.visibility = View.VISIBLE
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            /* no-op */
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                            /* no-op */
                        }

                        override fun onAnimationRepeat(animation: Animator?) {
                            /* no-op */
                        }
                    })
                    .start()
        } else {
            this.visibility = View.VISIBLE
        }
    }
}