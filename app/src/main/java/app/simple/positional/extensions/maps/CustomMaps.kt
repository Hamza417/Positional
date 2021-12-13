package app.simple.positional.extensions.maps

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

open class CustomMaps(context: Context, attrs: AttributeSet) : MapView(context, attrs),
                                                               OnMapReadyCallback,
                                                               SharedPreferences.OnSharedPreferenceChangeListener,
                                                               CoroutineScope {

    val viewHandler = Handler(Looper.getMainLooper())
    open var onTouch: ((event: MotionEvent, b: Boolean) -> Unit)? = null
    open var mapsCallbacks: MapsCallbacks? = null
    open var location: Location? = null
    open var latLng: LatLng? = null

    val cameraSpeed = 1000
    val autoCenterDelay = 6000L

    var isMapMovementEnabled = true
    var isAnimating = false

    var googleMap: GoogleMap? = null

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    open val lastLatitude = MainPreferences.getLastCoordinates()[0].toDouble()
    open val lastLongitude = MainPreferences.getLastCoordinates()[1].toDouble()

    init {
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

    fun setOnMapsCallbackListener(mapsCallbacks: MapsCallbacks) {
        this.mapsCallbacks = mapsCallbacks
    }
}