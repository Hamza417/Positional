package app.simple.positional.decorations.switchview

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import androidx.core.content.ContextCompat
import app.simple.positional.R
import app.simple.positional.util.ColorUtils.animateColorChange
import app.simple.positional.util.ViewUtils

@SuppressLint("ClickableViewAccessibility")
class SwitchView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : SwitchFrameLayout(context, attrs, defStyleAttr) {

    private var thumb: ImageView
    private var track: SwitchFrameLayout
    private var switchCallbacks: SwitchCallbacks? = null

    var isCheckable = true

    var isChecked: Boolean = false
        set(value) {
            if (value) {
                animateChecked()
            } else {
                animateUnchecked()
            }
            field = value
        }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.switch_view, this, true)

        thumb = view.findViewById(R.id.switch_thumb)
        track = view.findViewById(R.id.switch_track)

        ViewUtils.addShadow(track)

        view.setOnClickListener {
            isChecked = !isChecked
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                thumb.animate()
                        .scaleY(1.5F)
                        .scaleX(1.5F)
                        .setInterpolator(DecelerateInterpolator(1.5F))
                        .setDuration(500L)
                        .start()
            }
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_UP,
            -> {
                thumb.animate()
                        .scaleY(1.0F)
                        .scaleX(1.0F)
                        .setInterpolator(DecelerateInterpolator(1.5F))
                        .setDuration(500L)
                        .start()
            }
        }

        return super.onTouchEvent(event)
    }

    private fun animateUnchecked() {
        thumb.animate()
                .translationX(0F)
                .setInterpolator(OvershootInterpolator(3F))
                .setDuration(500)
                .start()

        track.animateColorChange(ContextCompat.getColor(context, R.color.switch_off))
        switchCallbacks?.onCheckedChanged(false)
        animateElevation(0F)
    }

    private fun animateChecked() {

        if (!isCheckable) return

        val w = context.resources.getDimensionPixelOffset(R.dimen.switch_width)
        val p = context.resources.getDimensionPixelOffset(R.dimen.switch_padding)
        val thumbWidth = context.resources.getDimensionPixelOffset(R.dimen.switch_thumb_dimensions)

        thumb.animate()
                .translationX((w - p * 2 - thumbWidth).toFloat())
                .setInterpolator(OvershootInterpolator(3F))
                .setDuration(500)
                .start()

        track.animateColorChange(ContextCompat.getColor(context, R.color.switch_on))
        switchCallbacks?.onCheckedChanged(true)
        animateElevation(25F)
    }

    private fun animateElevation(elevation: Float) {
        val valueAnimator = ValueAnimator.ofFloat(track.elevation, elevation)
        valueAnimator.duration = 500L
        valueAnimator.interpolator = DecelerateInterpolator(1.5F)
        valueAnimator.addUpdateListener {
            track.elevation = it.animatedValue as Float
        }
        valueAnimator.start()
    }

    fun setOnCheckedChangeListener(switchCallbacks: SwitchCallbacks) {
        this.switchCallbacks = switchCallbacks
    }

    fun invertIsChecked() {
        isChecked = !isChecked
    }
}
