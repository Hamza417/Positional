package app.simple.positional.decorations.maps

import android.animation.LayoutTransition
import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.DecelerateInterpolator
import app.simple.positional.R
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.preferences.GPSPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.ImageLoader
import app.simple.positional.util.LocationExtension
import app.simple.positional.util.ViewUtils.gone

class MapsTools : DynamicCornerLinearLayout, SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var align: DynamicRippleImageButton
    private lateinit var location: DynamicRippleImageButton
    private lateinit var compass: DynamicRippleImageButton
    private lateinit var bearing: DynamicRippleImageButton
    private lateinit var northOnly: DynamicRippleImageButton

    private var isFixed = false

    private lateinit var mapsToolsCallbacks: MapsToolsCallbacks

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setProperties()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setProperties()
    }

    private fun setProperties() {
        initViews()
        layoutTransition = LayoutTransition()
        app.simple.positional.singleton.SharedPreferences.getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
    }

    private fun initViews() {
        val view = LayoutInflater.from(context).inflate(R.layout.maps_tools, this, true)

        align = view.findViewById(R.id.tools_align_btn)
        location = view.findViewById(R.id.current_location)
        compass = view.findViewById(R.id.compass)
        bearing = view.findViewById(R.id.bearing)
        northOnly = view.findViewById(R.id.north_up)

        setAlignButtonState(animate = false)
        updateNorthOnlyIcon(animate = false)
        updateBearingIcon(animate = false)
        updateCompassIcon(animate = false)

        location.setOnClickListener {
            mapsToolsCallbacks.onLocationClicked(it, false)
        }

        location.setOnLongClickListener {
            mapsToolsCallbacks.onLocationClicked(it, true)
            true
        }

        align.setOnClickListener {
            GPSPreferences.setToolsGravity(
                    !GPSPreferences.isToolsGravityLeft()
            )
        }

        compass.setOnClickListener {
            with(GPSPreferences) {
                setCompassRotation(true)
                setBearingRotation(false)
                setNorthOnly(false)
            }
        }

        bearing.setOnClickListener {
            with(GPSPreferences) {
                setCompassRotation(false)
                setBearingRotation(true)
                setNorthOnly(false)
            }
        }

        northOnly.setOnClickListener {
            with(GPSPreferences) {
                setCompassRotation(false)
                setBearingRotation(false)
                setNorthOnly(true)
            }
        }

        if (MainPreferences.isCustomCoordinate()) {
            bearing.gone()
        }
    }

    private fun setAlignButtonState(animate: Boolean) {
        if (GPSPreferences.isToolsGravityLeft()) {
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

    fun locationIndicatorUpdate(isFixed: Boolean) {
        this.isFixed = isFixed
        if (isFixed) {
            location.setImageResource(R.drawable.ic_gps_fixed)
        } else {
            locationIconStatusUpdates()
        }
    }

    fun locationIconStatusUpdates() {
        if (LocationExtension.getLocationStatus(context)) {
            location.setImageResource(R.drawable.ic_gps_not_fixed)
        } else {
            location.setImageResource(R.drawable.ic_gps_off)
        }
    }

    private fun updateCompassIcon(animate: Boolean) {
        if (GPSPreferences.isCompassRotation()) {
            if (animate) {
                compass.animate().alpha(1F).setInterpolator(DecelerateInterpolator()).start()
            } else {
                compass.alpha = 1F
            }
        } else {
            if (animate) {
                compass.animate().alpha(0.4F).setInterpolator(DecelerateInterpolator()).start()
            } else {
                compass.alpha = 0.4F
            }
        }
    }

    private fun updateBearingIcon(animate: Boolean) {
        if (GPSPreferences.isBearingRotation()) {
            if (animate) {
                bearing.animate().alpha(1F).setInterpolator(DecelerateInterpolator()).start()
            } else {
                bearing.alpha = 1F
            }
        } else {
            if (animate) {
                bearing.animate().alpha(0.4F).setInterpolator(DecelerateInterpolator()).start()
            } else {
                bearing.alpha = 0.4F
            }
        }
    }

    private fun updateNorthOnlyIcon(animate: Boolean) {
        if (GPSPreferences.isNorthOnly()) {
            if (animate) {
                northOnly.animate().alpha(1F).setInterpolator(DecelerateInterpolator()).start()
            } else {
                northOnly.alpha = 1F
            }
        } else {
            if (animate) {
                northOnly.animate().alpha(0.4F).setInterpolator(DecelerateInterpolator()).start()
            } else {
                northOnly.alpha = 0.4F
            }
        }
    }

    fun setOnToolsCallbacksListener(mapsToolsCallbacks: MapsToolsCallbacks) {
        this.mapsToolsCallbacks = mapsToolsCallbacks
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            GPSPreferences.isNorthOnly -> {
                updateNorthOnlyIcon(true)
            }
            GPSPreferences.compassRotation -> {
                updateCompassIcon(true)
            }
            GPSPreferences.useBearingRotation -> {
                updateBearingIcon(true)
            }
            GPSPreferences.toolsGravity -> {
                setAlignButtonState(animate = true)
            }
        }
    }
}