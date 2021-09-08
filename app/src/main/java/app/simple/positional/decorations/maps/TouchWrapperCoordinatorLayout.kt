package app.simple.positional.decorations.maps

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.coordinatorlayout.widget.CoordinatorLayout

/**
 * Workaround to intercept touch events on the [Maps] while the compass
 * is running to allow gestures support.
 */
class TouchWrapperCoordinatorLayout : CoordinatorLayout {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var onTouch: ((event: MotionEvent, b: Boolean) -> Unit)? = null

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        with(super.dispatchTouchEvent(event)) {
            onTouch?.invoke(event, this)
            return this
        }
    }
}
