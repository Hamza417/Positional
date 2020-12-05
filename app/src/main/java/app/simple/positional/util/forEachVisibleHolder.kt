package app.simple.positional.util

import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.RecyclerView

const val flingTranslationMagnitude = 0.5f
const val overscrollRotationMagnitude = 0.5f
const val overscrollTranslationMagnitude = 0.5f

const val bouncyValue = SpringForce.DAMPING_RATIO_NO_BOUNCY
const val stiffnessValue = SpringForce.STIFFNESS_LOW

inline fun <reified T : RecyclerView.ViewHolder> RecyclerView.forEachVisibleHolder(action: (T) -> Unit) {
    for (i in 0 until childCount) {
        action(getChildViewHolder(getChildAt(i)) as T)
    }
}