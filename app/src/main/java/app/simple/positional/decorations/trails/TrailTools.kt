package app.simple.positional.decorations.trails

import android.animation.LayoutTransition
import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import app.simple.positional.R
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.preferences.TrailPreferences
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import app.simple.positional.util.ImageLoader
import app.simple.positional.util.ViewUtils.gone
import app.simple.positional.util.ViewUtils.visible

class TrailTools : DynamicCornerLinearLayout, SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var location: DynamicRippleImageButton
    private lateinit var remove: DynamicRippleImageButton
    private lateinit var wrap: DynamicRippleImageButton
    private lateinit var align: DynamicRippleImageButton
    private lateinit var icons: DynamicRippleImageButton
    private lateinit var compass: DynamicRippleImageButton

    private lateinit var trailCallbacks: TrailCallbacks

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setProperties()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setProperties()
    }

    private fun setProperties() {
        initViews()
        layoutTransition = LayoutTransition()
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
    }

    private fun initViews() {
        val view = LayoutInflater.from(context).inflate(R.layout.trail_tools, this, true)

        location = view.findViewById(R.id.current_location)
        remove = view.findViewById(R.id.remove_flag)
        wrap = view.findViewById(R.id.wrap_unwrap_flags)
        icons = view.findViewById(R.id.add_flag)
        align = view.findViewById(R.id.tools_align_btn)
        compass = view.findViewById(R.id.compass)

        icons.setOnClickListener {
            trailCallbacks.onAdd(it)
        }

        setWrapUnwrapButtonState(false)
        setAlignButtonState(false)
        setCompassButtonState(false)

        location.setOnClickListener {
            trailCallbacks.onLocation(false)
        }

        location.setOnLongClickListener {
            trailCallbacks.onLocation(true)
            true
        }

        remove.setOnClickListener {
            trailCallbacks.onRemove(remove)
        }

        wrap.setOnClickListener {
            trailCallbacks.onWrapUnwrap()
        }

        align.setOnClickListener {
            TrailPreferences.setToolsGravityToLeft(
                    !TrailPreferences.isToolsGravityToLeft()
            )
        }

        compass.setOnClickListener {
            TrailPreferences.setCompassRotation(!TrailPreferences.isCompassRotation())
        }
    }

    private fun setWrapUnwrapButtonState(animate: Boolean) {
        if (TrailPreferences.arePolylinesWrapped()) {
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
        if (TrailPreferences.isCompassRotation()) {
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
        if (TrailPreferences.isToolsGravityToLeft()) {
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

    fun setTrailCallbacksListener(trailCallbacks: TrailCallbacks) {
        this.trailCallbacks = trailCallbacks
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key!!) {
            TrailPreferences.wrapped -> {
                setWrapUnwrapButtonState(true)
            }
            TrailPreferences.toolsMenuGravity -> {
                setAlignButtonState(true)
            }
            TrailPreferences.compass -> {
                setCompassButtonState(true)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
    }

    companion object {
        interface TrailCallbacks {
            fun onLocation(reset: Boolean)
            fun onAdd(view: View)
            fun onRemove(remove: View)
            fun onWrapUnwrap()
        }
    }
}