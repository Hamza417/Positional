package app.simple.positional.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

class OverScrollBehavior(context: Context, attributeSet: AttributeSet) : CoordinatorLayout.Behavior<View>() {

    companion object {
        private const val OVER_SCROLL_AREA = 4
    }

    private var overScrollY = 0

    override fun onStartNestedScroll(
            coordinatorLayout: CoordinatorLayout,
            child: View,
            directTargetChild: View,
            target: View,
            axes: Int,
            type: Int
    ): Boolean {
        overScrollY = 0
        return true
    }

    override fun onNestedScroll(
            coordinatorLayout: CoordinatorLayout,
            child: View,
            target: View,
            dxConsumed: Int,
            dyConsumed: Int,
            dxUnconsumed: Int,
            dyUnconsumed: Int,
            type: Int,
            consumed: IntArray
    ) {
        if (dyUnconsumed == 0) {
            return
        }

        overScrollY -= (dyUnconsumed / OVER_SCROLL_AREA)
        val group = target as ViewGroup
        val count = group.childCount
        for (i in 0 until count) {
            val view = group.getChildAt(i)
            view.translationY = overScrollY.toFloat()
        }
    }

    override fun onStopNestedScroll(
            coordinatorLayout: CoordinatorLayout,
            child: View,
            target: View,
            type: Int
    ) {
        // Smooth animate to 0 when the user stops scrolling
        moveToDefPosition(target)
    }

    override fun onNestedPreFling(
            coordinatorLayout: CoordinatorLayout,
            child: View,
            target: View,
            velocityX: Float,
            velocityY: Float
    ): Boolean {
        // Scroll view by inertia when current position equals to 0
        if (overScrollY == 0) {
            return false
        }
        // Smooth animate to 0 when user fling view
        moveToDefPosition(target)
        return true
    }

    private fun moveToDefPosition(target: View) {
        val group = target as ViewGroup
        val count = group.childCount
        for (i in 0 until count) {
            val view = group.getChildAt(i)
            val springAnimation = SpringAnimation(view, DynamicAnimation.TRANSLATION_Y)
            springAnimation.spring = SpringForce()
                    .setFinalPosition(0f)
                    .setDampingRatio(SpringForce.DAMPING_RATIO_NO_BOUNCY)
                    .setStiffness(SpringForce.STIFFNESS_LOW)
            springAnimation.start()
        }
    }
}