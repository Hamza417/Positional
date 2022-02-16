package app.simple.positional.decorations.maps

import android.animation.LayoutTransition
import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import app.simple.positional.R
import app.simple.positional.constants.LocationPins.locationsPins
import app.simple.positional.decorations.corners.DynamicCornerLinearLayout
import app.simple.positional.decorations.ripple.DynamicRippleImageButton
import app.simple.positional.preferences.GPSPreferences
import app.simple.positional.preferences.GPSPreferences.getPinSkin
import app.simple.positional.preferences.MainPreferences.getCornerRadius
import app.simple.positional.preferences.MainPreferences.isCustomCoordinate
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import app.simple.positional.util.StatusBarHeight
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel

class MapToolbar : DynamicCornerLinearLayout, OnSharedPreferenceChangeListener {

    private lateinit var mapToolbarCallbacks: MapToolbarCallbacks
    private lateinit var menu: DynamicRippleImageButton
    private lateinit var customLocationButton: DynamicRippleImageButton

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setProperties()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setProperties()
    }

    private fun setProperties() {
        initViews()

        val shapeAppearanceModel = ShapeAppearanceModel()
            .toBuilder()
            .setBottomLeftCorner(CornerFamily.ROUNDED, getCornerRadius().toFloat())
            .setBottomRightCorner(CornerFamily.ROUNDED, getCornerRadius().toFloat())
            .build()

        background = MaterialShapeDrawable(shapeAppearanceModel)

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

        menu = view.findViewById(R.id.gps_menu)
        customLocationButton = view.findViewById(R.id.gps_custom_location)

        with(view.findViewById<TextView>(R.id.map_toolbar_heading)) {
            isSelected = true
        }

        if (isCustomCoordinate()) {
            customLocationButton.setImageResource(R.drawable.ic_place_custom)
        } else {
            customLocationButton.setImageResource(locationsPins[getPinSkin()])
        }

        menu.setOnClickListener { mapToolbarCallbacks.onMenuClicked(it) }

        customLocationButton.setOnClickListener {
            mapToolbarCallbacks.onCustomLocationClicked(it)
        }

        customLocationButton.setOnLongClickListener {
            mapToolbarCallbacks.onCustomLocationLongPressed(it)
            true
        }
    }

    fun hide() {
        animate().translationY((height * -1).toFloat()).alpha(0f).setInterpolator(DecelerateInterpolator(1.5f)).start()
        menu.isClickable = false
        isClickable = false
    }

    fun show() {
        animate().translationY(0f).alpha(1f).setInterpolator(DecelerateInterpolator(1.5f)).start()
        menu.isClickable = true
        isClickable = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(this)
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
        fun onMenuClicked(view: View)
        fun onCustomLocationClicked(view: View)
        fun onCustomLocationLongPressed(view: View)
    }
}