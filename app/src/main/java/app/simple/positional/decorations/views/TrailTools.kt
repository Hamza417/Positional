package app.simple.positional.decorations.views

import android.animation.LayoutTransition
import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.view.LayoutInflater
import app.simple.positional.R
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.preferences.TrailPreferences
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences

class TrailTools : DynamicCornerLinearLayout, SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var add: DynamicRippleImageButton
    private lateinit var remove: DynamicRippleImageButton
    private lateinit var wrap: DynamicRippleImageButton

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

        add = view.findViewById(R.id.add_flag)
        remove = view.findViewById(R.id.remove_flag)
        wrap = view.findViewById(R.id.wrap_unwrap_flags)

        setWrapUnwrapButtonStatus()

        add.setOnClickListener {
            trailCallbacks.onAdd()
        }

        remove.setOnClickListener {
            trailCallbacks.onRemove()
        }

        wrap.setOnClickListener {
            trailCallbacks.onWrapUnwrap()
        }
    }

    private fun setWrapUnwrapButtonStatus() {
        if (TrailPreferences.arePolylinesWrapped()) {
            wrap.setImageResource(R.drawable.ic_close_fullscreen)
        } else {
            wrap.setImageResource(R.drawable.ic_full_screen)
        }
    }

    fun setTrailCallbacksListener(trailCallbacks: TrailCallbacks) {
        this.trailCallbacks = trailCallbacks
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == TrailPreferences.wrapped) {
            setWrapUnwrapButtonStatus()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
    }

    companion object {
        interface TrailCallbacks {
            fun onAdd()
            fun onRemove()
            fun onWrapUnwrap()
        }
    }
}