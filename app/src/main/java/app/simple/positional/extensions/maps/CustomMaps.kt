package app.simple.positional.extensions.maps

import android.animation.ValueAnimator
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
import android.view.animation.DecelerateInterpolator
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

    private var customCameraAnimator: CustomCameraAnimator? = null

    val cameraSpeed = 3000
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
        customCameraAnimator = CustomCameraAnimator(googleMap!!)

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

                        else                           -> 0
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

    internal fun animateCamera(latLng: LatLng, zoom: Float, tilt: Float, duration: Int = cameraSpeed) {
        customCameraAnimator?.animateCamera(latLng, zoom, tilt, duration)
    }

    // -------------------------------------------------------------------------------------------------------- //

    internal fun isCameraWithinBounds(latLng: LatLng): Boolean {
        return googleMap?.projection?.visibleRegion?.latLngBounds?.contains(latLng)!!
    }

    // -------------------------------------------------------------------------------------------------------- //

    fun setOnMapsCallbackListener(mapsCallbacks: MapsCallbacks) {
        this.mapsCallbacks = mapsCallbacks
    }

    private inner class CustomCameraAnimator(private val googleMap: GoogleMap) {
        private var animator: ValueAnimator? = null

        fun animateCamera(target: LatLng, zoom: Float, tilt: Float, duration: Int = 3000) {
            val startPosition = googleMap.cameraPosition.target
            val startZoom = googleMap.cameraPosition.zoom

            animator = ValueAnimator.ofFloat(0f, 1f)
            animator?.duration = duration.toLong()
            animator?.interpolator = DecelerateInterpolator(3F)
            animator?.addUpdateListener { animation ->
                val fraction = animation.animatedFraction
                val lat = startPosition.latitude + (target.latitude - startPosition.latitude) * fraction
                val lng = startPosition.longitude + (target.longitude - startPosition.longitude) * fraction
                val currentZoom = startZoom + (zoom - startZoom) * fraction
                val currentTilt = googleMap.cameraPosition.tilt + (tilt - googleMap.cameraPosition.tilt) * fraction

                val cameraPosition = CameraPosition.Builder()
                        .target(LatLng(lat, lng))
                        .zoom(currentZoom)
                        .tilt(currentTilt)
                        .build()

                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }
            animator?.start()
        }

        fun cancel() {
            animator?.cancel()
        }
    }
}
