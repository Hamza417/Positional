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
import app.simple.positional.preferences.TrailPreferences
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import app.simple.positional.util.ViewUtils.makeGoAway
import app.simple.positional.util.ViewUtils.makeVisible

class TrailTools : DynamicCornerLinearLayout, SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var location: DynamicRippleImageButton
    private lateinit var remove: DynamicRippleImageButton
    private lateinit var wrap: DynamicRippleImageButton
    private lateinit var align: DynamicRippleImageButton
    private lateinit var icons: DynamicRippleImageButton

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

        icons.setOnClickListener {
            trailCallbacks.onAdd(it)
        }

        setWrapUnwrapButtonState()
        setAlignButtonState()

        location.setOnClickListener {
            trailCallbacks.onLocation()
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
    }

    private fun setWrapUnwrapButtonState() {
        if (TrailPreferences.arePolylinesWrapped()) {
            wrap.setImageResource(R.drawable.ic_close_fullscreen)
        } else {
            wrap.setImageResource(R.drawable.ic_full_screen)
        }
    }

    private fun setAlignButtonState() {
        if (TrailPreferences.isToolsGravityToLeft()) {
            align.setImageResource(R.drawable.ic_arrow_right)
        } else {
            align.setImageResource(R.drawable.ic_arrow_left)
        }
    }

    fun setTrailCallbacksListener(trailCallbacks: TrailCallbacks) {
        this.trailCallbacks = trailCallbacks
    }

    fun changeButtonState(hide: Boolean) {
        if (hide) {
            wrap.makeGoAway()
            remove.makeGoAway()
        } else {
            wrap.makeVisible(false)
            remove.makeVisible(false)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key!!) {
            TrailPreferences.wrapped -> {
                setWrapUnwrapButtonState()
            }
            TrailPreferences.toolsMenuGravity -> {
                setAlignButtonState()
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
    }

    companion object {
        interface TrailCallbacks {
            fun onLocation()
            fun onAdd(view: View)
            fun onRemove(remove: View)
            fun onWrapUnwrap()
        }
    }
}