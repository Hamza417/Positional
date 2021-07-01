package app.simple.positional.decorations.views

import android.animation.LayoutTransition
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.DecelerateInterpolator
import app.simple.positional.R
import app.simple.positional.constants.LocationPins.locationsPins
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.preferences.GPSPreferences
import app.simple.positional.preferences.GPSPreferences.getPinSkin
import app.simple.positional.preferences.MainPreferences.isCustomCoordinate
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import app.simple.positional.util.LocationExtension.getLocationStatus
import app.simple.positional.util.StatusBarHeight

class MapToolbar : DynamicCornerLinearLayout, OnSharedPreferenceChangeListener {
    private lateinit var mapToolbarCallbacks: MapToolbarCallbacks
    private lateinit var location: DynamicRippleImageButton
    private lateinit var menu: DynamicRippleImageButton
    private lateinit var customLocationButton: DynamicRippleImageButton
    private var isFixed = false

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setProperties()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setProperties()
    }

    private fun setProperties() {
        initViews()
        layoutTransition = LayoutTransition()
        getSharedPreferences()
            .registerOnSharedPreferenceChangeListener(this)
    }

    private fun initViews() {
        val view = LayoutInflater.from(context).inflate(R.layout.toolbar_map_panel, this, true)
        setPadding(resources.getDimensionPixelOffset(R.dimen.toolbar_padding),
                   resources.getDimensionPixelOffset(R.dimen.toolbar_padding) + StatusBarHeight.getStatusBarHeight(resources),
                   resources.getDimensionPixelOffset(R.dimen.toolbar_padding),
                   resources.getDimensionPixelOffset(R.dimen.toolbar_padding))

        location = view.findViewById(R.id.gps_location_indicator)
        menu = view.findViewById(R.id.gps_menu)
        customLocationButton = view.findViewById(R.id.gps_custom_location)

        if (isCustomCoordinate()) {
            customLocationButton.setImageResource(R.drawable.ic_place_custom)
        } else {
            customLocationButton.setImageResource(locationsPins[getPinSkin()])
        }

        location.setOnClickListener { mapToolbarCallbacks.onLocationReset(it) }
        location.setOnLongClickListener {
            mapToolbarCallbacks.onLocationLongPressed()
            true
        }

        menu.setOnClickListener { mapToolbarCallbacks.onMenuClicked(it) }
        customLocationButton.setOnClickListener { mapToolbarCallbacks.onCustomLocationClicked(it) }
    }

    fun hide() {
        animate().translationY((height * -1).toFloat()).alpha(0f).setInterpolator(DecelerateInterpolator(1.5f)).start()
        location.isClickable = false
        menu.isClickable = false
        isClickable = false
    }

    fun show() {
        animate().translationY(0f).alpha(1f).setInterpolator(DecelerateInterpolator(1.5f)).start()
        location.isClickable = isFixed
        menu.isClickable = true
        isClickable = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        println("Called")
        getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(this)
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
        if (getLocationStatus(context)) {
            location.setImageResource(R.drawable.ic_gps_not_fixed)
        } else {
            location.setImageResource(R.drawable.ic_gps_off)
        }
    }

    fun setOnMapToolbarCallbacks(mapToolbarCallbacks: MapToolbarCallbacks) {
        this.mapToolbarCallbacks = mapToolbarCallbacks
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (GPSPreferences.pinSkin == key) {
            if (!isCustomCoordinate()) {
                customLocationButton.setImageResource(locationsPins[getPinSkin()])
            }
        }
    }

    interface MapToolbarCallbacks {
        fun onLocationReset(view: View)
        fun onLocationLongPressed()
        fun onMenuClicked(view: View)
        fun onCustomLocationClicked(view: View)
    }
}