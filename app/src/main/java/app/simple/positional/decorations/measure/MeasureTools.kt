package app.simple.positional.decorations.measure

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.transition.TransitionInflater
import androidx.transition.TransitionManager
import app.simple.positional.R
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.decorations.views.LocationButton
import app.simple.positional.preferences.MeasurePreferences
import app.simple.positional.util.ImageLoader
import app.simple.positional.util.StatusBarHeight
import app.simple.positional.util.ViewUtils.gone
import app.simple.positional.util.ViewUtils.visible
import app.simple.positional.singleton.SharedPreferences as PositionalSingletonSharedPreferences

class MeasureTools : DynamicCornerLinearLayout, SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var align: DynamicRippleImageButton
    private lateinit var location: LocationButton
    private lateinit var wrap: DynamicRippleImageButton
    private lateinit var compass: DynamicRippleImageButton
    private lateinit var add: DynamicRippleImageButton
    private lateinit var remove: DynamicRippleImageButton

    private var measureToolsCallbacks: MeasureToolsCallbacks? = null

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setProperties()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setProperties()
    }

    private fun setProperties() {
        initViews()
        updateToolsGravity()
    }

    private fun initViews() {
        val view = LayoutInflater.from(context).inflate(R.layout.tools_measure, this, true)

        align = view.findViewById(R.id.align)
        location = view.findViewById(R.id.location)
        wrap = view.findViewById(R.id.wrap)
        compass = view.findViewById(R.id.compass)
        add = view.findViewById(R.id.add)
        remove = view.findViewById(R.id.remove)

        setWrapUnwrapButtonState(false)
        setAlignButtonState(false)
        setCompassButtonState(false)

        location.setOnClickListener { v ->
            measureToolsCallbacks?.onLocation(v, false)
        }

        location.setOnLongClickListener { v ->
            measureToolsCallbacks?.onLocation(v, true)
            true
        }

        wrap.setOnClickListener { v ->
            MeasurePreferences.invertPolylinesWrapped()
        }

        compass.setOnClickListener { v ->
            MeasurePreferences.invertCompassRotation()
        }

        add.setOnClickListener { v ->
            measureToolsCallbacks?.onNewAdd(v)
        }

        remove.setOnClickListener { v ->
            measureToolsCallbacks?.onClearRecentMarker(v)
        }

        align.setOnClickListener { v ->
            MeasurePreferences.invertToolsGravity()
        }
    }

    private fun setWrapUnwrapButtonState(animate: Boolean) {
        if (MeasurePreferences.arePolylinesWrapped()) {
            if (animate) {
                ImageLoader.setImage(R.drawable.ic_close_fullscreen, wrap, context, 0)
            } else {
                wrap.setImageResource(R.drawable.ic_close_fullscreen)
            }
        } else {
            if (animate && wrap.visibility != View.GONE) {
                ImageLoader.setImage(R.drawable.ic_full_screen, wrap, context, 0)
            } else {
                wrap.setImageResource(R.drawable.ic_full_screen)
            }
        }
    }

    private fun setCompassButtonState(animate: Boolean) {
        if (MeasurePreferences.isCompassRotation()) {
            if (animate) {
                ImageLoader.setImage(R.drawable.ic_compass_on, compass, context, 0)
            } else {
                compass.setImageResource(R.drawable.ic_compass_on)
            }
        } else {
            if (animate) {
                ImageLoader.setImage(R.drawable.ic_compass_off, compass, context, 0)
            } else {
                compass.setImageResource(R.drawable.ic_compass_off)
            }
        }
    }

    private fun setAlignButtonState(animate: Boolean) {
        if (MeasurePreferences.isToolsGravityToLeft()) {
            if (animate) {
                ImageLoader.setImage(R.drawable.ic_arrow_right, align, context, 0)
            } else {
                align.setImageResource(R.drawable.ic_arrow_right)
            }
        } else {
            if (animate) {
                ImageLoader.setImage(R.drawable.ic_arrow_left, align, context, 0)
            } else {
                align.setImageResource(R.drawable.ic_arrow_left)
            }
        }
    }

    fun changeWrapButtonState(hide: Boolean) {
        if (hide) {
            wrap.gone()
            remove.gone()
        } else {
            wrap.visible(false)
            remove.visible(false)
        }
    }

    internal fun updateToolsGravity() {
        if (StatusBarHeight.isLandscape(context)) return

        TransitionManager.beginDelayedTransition(
            this as ViewGroup,
            TransitionInflater.from(context)
                .inflateTransition(R.transition.tools_transition))

        val params = CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)

        params.apply {
            gravity = if (MeasurePreferences.isToolsGravityToLeft()) {
                Gravity.START or Gravity.CENTER_VERTICAL
            } else {
                Gravity.END or Gravity.CENTER_VERTICAL
            }

            marginStart = resources.getDimensionPixelSize(R.dimen.trail_tools_margin)
            marginEnd = resources.getDimensionPixelSize(R.dimen.trail_tools_margin)
        }

        layoutParams = params
        setAlignButtonState(true)
    }

    fun locationIndicatorUpdate(isFixed: Boolean) = location.locationIndicatorUpdate(isFixed)
    fun locationIconStatusUpdates() = location.locationIconStatusUpdate()

    fun setMeasureToolsCallbacks(callbacks: MeasureToolsCallbacks) {
        measureToolsCallbacks = callbacks
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key!!) {
            MeasurePreferences.POLYLINES_WRAPPED -> {
                setWrapUnwrapButtonState(true)
            }

            MeasurePreferences.TOOLS_GRAVITY -> {
                updateToolsGravity()
            }

            MeasurePreferences.COMPASS_ROTATION -> {
                setCompassButtonState(true)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        PositionalSingletonSharedPreferences.getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        PositionalSingletonSharedPreferences.getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(this)
    }
}
