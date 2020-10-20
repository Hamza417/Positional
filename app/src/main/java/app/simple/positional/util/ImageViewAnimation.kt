package app.simple.positional.util

import android.content.Context
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import app.simple.positional.R

fun imageViewAnimatedChange(v: ImageView, new_image: Int, context: Context) {
    val animOut: Animation = AnimationUtils.loadAnimation(context, R.anim.image_out)
    val animIn: Animation = AnimationUtils.loadAnimation(context, R.anim.image_in)
    animOut.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) {}

        override fun onAnimationEnd(animation: Animation?) {
            v.setImageResource(new_image)
            animIn.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {

                }

                override fun onAnimationEnd(animation: Animation?) {

                }

                override fun onAnimationRepeat(animation: Animation?) {

                }

            })
            v.startAnimation(animIn)
        }

        override fun onAnimationRepeat(animation: Animation?) {}

    })
    v.startAnimation(animOut)
}