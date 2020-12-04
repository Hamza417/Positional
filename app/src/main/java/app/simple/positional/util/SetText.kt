package app.simple.positional.util

import android.view.View
import android.widget.TextView

fun TextView.setTextAnimation(text: String, duration: Long = 300, completion: (() -> Unit)? = null) {
    fadeOutAnimation(duration) {
        this.text = text
        fadeInAnimation(duration) {
            completion?.let {
                it()
            }
        }
    }
}

fun View.fadeOutAnimation(duration: Long = 300, visibility: Int = View.INVISIBLE, completion: (() -> Unit)? = null) {
    animate()
            .alpha(0f)
            .setDuration(duration)
            .withEndAction {
                this.visibility = visibility
                completion?.let {
                    it()
                }
            }
}

fun View.fadeInAnimation(duration: Long = 300, completion: (() -> Unit)? = null) {
    alpha = 0f
    visibility = View.VISIBLE
    animate()
            .alpha(1f)
            .setDuration(duration)
            .withEndAction {
                completion?.let {
                    it()
                }
            }
}