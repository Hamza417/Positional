package app.simple.positional.extensions.maps

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.WindowManager
import app.simple.positional.R
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import app.simple.positional.util.ConditionUtils.invert
import app.simple.positional.util.ConditionUtils.isNotNull
import app.simple.positional.util.ConditionUtils.isNull
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

open class CustomMaps(context: Context, attrs: AttributeSet) : MapView(context, attrs),
    OnMapReadyCallback,
    SharedPreferences.OnSharedPreferenceChangeListener,
    CoroutineScope {

    protected var windowManager: WindowManager
    val viewHandler = Handler(Looper.getMainLooper())
    open var onTouch: ((event: MotionEvent, b: Boolean) -> Unit)? = null
    open var mapsCallbacks: MapsCallbacks? = null
    open var location: Location? = null
    open var latLng: LatLng? = null

    val cameraSpeed = 500
    val autoCenterDelay = 6000L

    var isMapMovementEnabled = true
    var isAnimating = false

    var googleMap: GoogleMap? = null

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    open var lastLatitude = 0.0
    open var lastLongitude = 0.0

    init {
        if (isInEditMode.invert()) {
            lastLatitude = MainPreferences.getLastCoordinates()[0].toDouble()
            lastLongitude = MainPreferences.getLastCoordinates()[1].toDouble()
        }

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

        viewHandler.postDelayed(fun() {
            // This prevents the lag when fragment is switched
            this.alpha = 0F
            getMapAsync(this)
        }, 500)
    }

    // -------------------------------------------------------------------------------------------------------- //

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0

        /**
         * Workaround for flashing of view when map is
         * Initialized
         */
        this.animate().alpha(1F).setDuration(500).start()

        googleMap?.uiSettings?.isCompassEnabled = false
        googleMap?.uiSettings?.isMapToolbarEnabled = false
        googleMap?.uiSettings?.isMyLocationButtonEnabled = false
        googleMap?.isTrafficEnabled = true

        googleMap?.setOnMapClickListener {
            mapsCallbacks?.onMapClicked()
        }

        googleMap?.setOnMapLongClickListener {
            mapsCallbacks?.onMapLongClicked(it)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {

    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        with(super.dispatchTouchEvent(ev)) {
            onTouch?.invoke(ev, this)
            return this
        }
    }

    // -------------------------------------------------------------------------------------------------------- //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onResume()
    }

    override fun onResume() {
        super.onResume()
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
        removeCallbacks { }
    }

    // -------------------------------------------------------------------------------------------------------- //

    open fun getCamera(): CameraPosition? {
        return googleMap?.cameraPosition
    }

    open fun setCamera(cameraPosition: CameraPosition?) {
        cameraPosition ?: return
        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    open fun setBuildings(value: Boolean) {
        googleMap?.isBuildingsEnabled = value
    }

    open fun setTraffic(value: Boolean) {
        googleMap?.isTrafficEnabled = value
    }

    protected fun setMapStyle(label: Boolean, satellite: Boolean, highContrast: Boolean) {
        setSatellite(label = label, satellite = satellite)

        if (!googleMap.isNull()) {
            if (highContrast) {
                googleMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(context,
                    if (label) {
                        R.raw.map_high_contrast_labelled
                    } else {
                        R.raw.map_high_contrast_non_labelled
                    }
                ))

            } else {
                googleMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(context,
                    when (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                        Configuration.UI_MODE_NIGHT_YES -> {
                            if (label) {
                                R.raw.maps_dark_labelled
                            } else {
                                R.raw.maps_dark_no_label
                            }
                        }

                        Configuration.UI_MODE_NIGHT_NO -> {
                            if (label) {
                                R.raw.maps_light_labelled
                            } else {
                                R.raw.maps_light_no_label
                            }
                        }

                        else -> 0
                    }
                ))
            }
        }
    }

    private fun setSatellite(satellite: Boolean, label: Boolean) {
        if (googleMap.isNotNull())
            googleMap?.mapType = if (satellite) {
                if (label) {
                    GoogleMap.MAP_TYPE_HYBRID
                } else {
                    GoogleMap.MAP_TYPE_SATELLITE
                }
            } else {
                GoogleMap.MAP_TYPE_NORMAL
            }
    }

    // -------------------------------------------------------------------------------------------------------- //

    internal fun isCameraWithinBounds(latLng: LatLng): Boolean {
        return googleMap?.projection?.visibleRegion?.latLngBounds?.contains(latLng)!!
    }

    // -------------------------------------------------------------------------------------------------------- //

    fun setOnMapsCallbackListener(mapsCallbacks: MapsCallbacks) {
        this.mapsCallbacks = mapsCallbacks
    }
}
