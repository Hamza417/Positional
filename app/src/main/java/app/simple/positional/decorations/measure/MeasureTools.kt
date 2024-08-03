package app.simple.positional.decorations.measure

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.view.LayoutInflater
import app.simple.positional.R
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.singleton.SharedPreferences as PositionalSingletonSharedPreferences

class MeasureTools : DynamicCornerLinearLayout, SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var align: DynamicRippleImageButton
    private lateinit var location: DynamicRippleImageButton
    private lateinit var wrap: DynamicRippleImageButton
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
    }

    private fun initViews() {
        val view = LayoutInflater.from(context).inflate(R.layout.tools_measure, this, true)

        align = view.findViewById(R.id.align)
        location = view.findViewById(R.id.location)
        wrap = view.findViewById(R.id.wrap)
        add = view.findViewById(R.id.add)
        remove = view.findViewById(R.id.remove)

        setWrapUnwrapButtonState(false)
        setAlignButtonState(false)
        setCompassButtonState(false)

        location.setOnClickListener { v ->
            measureToolsCallbacks?.onLocation(v)
        }

        wrap.setOnClickListener { v ->
            measureToolsCallbacks?.onWrap(v)
        }

        add.setOnClickListener { v ->
            measureToolsCallbacks?.onNewAdd(v)
        }

        remove.setOnClickListener { v ->
            measureToolsCallbacks?.onClearRecentMarker(v)
        }
    }

    private fun setWrapUnwrapButtonState(animate: Boolean) {

    }

    private fun setCompassButtonState(animate: Boolean) {

    }

    private fun setAlignButtonState(animate: Boolean) {

    }

    fun setMeasureToolsCallbacks(callbacks: MeasureToolsCallbacks) {
        measureToolsCallbacks = callbacks
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {

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
