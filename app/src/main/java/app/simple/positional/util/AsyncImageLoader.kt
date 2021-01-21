package app.simple.positional.util

import android.content.Context
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import app.simple.positional.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AsyncImageLoader {
    fun loadImageResources(resourceValue: Int, imageView: ImageView, context: Context, delay: Int) {
        CoroutineScope(Dispatchers.Default).launch {
            val drawable = if (resourceValue != 0) context.resources?.let {
                ResourcesCompat.getDrawable(it, resourceValue, null)
            }!! else null

            withContext(Dispatchers.Main) {
                val animOut: Animation = AnimationUtils.loadAnimation(context, R.anim.image_out)
                val animIn: Animation = AnimationUtils.loadAnimation(context, R.anim.image_in)

                animIn.startOffset = delay.toLong()
                animOut.startOffset = delay.toLong()

                animOut.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        imageView.setImageDrawable(drawable)
                        animIn.setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationStart(animation: Animation?) {

                            }

                            override fun onAnimationEnd(animation: Animation?) {

                            }

                            override fun onAnimationRepeat(animation: Animation?) {

                            }

                        })
                        imageView.startAnimation(animIn)
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                })
                imageView.startAnimation(animOut)

            }
        }
    }


    fun loadImageResourcesWithoutAnimation(resourceValue: Int, imageView: ImageView, context: Context) {
        CoroutineScope(Dispatchers.Default).launch {
            val drawable = if (resourceValue != 0) context.resources?.let {
                ResourcesCompat.getDrawable(it, resourceValue, null)
            }!! else null

            withContext(Dispatchers.Main) {
                try {
                    imageView.setImageDrawable(drawable)
                } catch (e: NullPointerException) {
                }
            }
        }
    }
}
