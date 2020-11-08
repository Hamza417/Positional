package app.simple.positional.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import app.simple.positional.R

@Suppress("deprecation")
fun loadImageResources(resourceValue: Int, imageView: ImageView, context: Context) {
    class GetData : AsyncTask<Void, Void, Drawable>() {
        override fun doInBackground(vararg params: Void?): Drawable? {
            return if (resourceValue != 0) context.resources?.let { ResourcesCompat.getDrawable(it, resourceValue, null) }!! else null
        }

        override fun onPostExecute(result: Drawable?) {
            super.onPostExecute(result)
            val animOut: Animation = AnimationUtils.loadAnimation(context, R.anim.image_out)
            val animIn: Animation = AnimationUtils.loadAnimation(context, R.anim.image_in)
            animOut.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    imageView.setImageDrawable(result)
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

    val getData = GetData()

    if (getData.status == AsyncTask.Status.RUNNING) {
        if (getData.cancel(true)) {
            getData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    } else {
        getData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }
}

@Suppress("deprecation")
fun loadImageResourcesWithoutAnimation(resourceValue: Int, imageView: ImageView, context: Context) {
    class GetData : AsyncTask<Void, Void, Drawable>() {
        override fun doInBackground(vararg params: Void?): Drawable? {
            return if (resourceValue != 0) context.resources?.let { ResourcesCompat.getDrawable(it, resourceValue, null) }!! else null
        }

        override fun onPostExecute(result: Drawable?) {
            super.onPostExecute(result)
            imageView.setImageDrawable(result)
        }
    }

    val getData = GetData()

    if (getData.status == AsyncTask.Status.RUNNING) {
        if (getData.cancel(true)) {
            getData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    } else {
        getData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
    }
}