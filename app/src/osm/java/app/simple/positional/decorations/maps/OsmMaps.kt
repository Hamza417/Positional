package app.simple.positional.decorations.maps

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.preference.GPSPreferences
import app.simple.positional.preference.MainPreferences
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import app.simple.positional.util.NullSafety.isNotNull
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import kotlin.coroutines.CoroutineContext


class OsmMaps(context: Context, attrs: AttributeSet?) :
        MapView(context, attrs),
        SharedPreferences.OnSharedPreferenceChangeListener,
        CoroutineScope {

    /**
     * Setting the location will also add marker on the map
     * on that location. If the custom coordinate is checked
     * in settings panel, it will mark the map but only the
     * custom coordinates
     */
    var location: Location? = null
        set(value) {
            latLng = if (isCustomCoordinate) {
                LatLng(customLatitude, customLongitude)
            } else {
                LatLng(value!!.latitude, value.longitude)
            }
            addMarker()
            field = value
        }

    private var latLng: LatLng? = null
    private var mapsCallbacks: OsmMapsCallbacks? = null
    private var marker: Drawable? = null
    private val viewHandler = Handler(Looper.getMainLooper())

    private var isCustomCoordinate = false
    private var isBearingRotation = false
    private var customLatitude = 0.0
    private var customLongitude = 0.0
    val lastLatitude = GPSPreferences.getLastCoordinates()[0].toDouble()
    val lastLongitude = GPSPreferences.getLastCoordinates()[1].toDouble()

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    init {
        isCustomCoordinate = MainPreferences.isCustomCoordinate()
        isBearingRotation = GPSPreferences.isBearingRotationOn()

        marker = if (isCustomCoordinate) {
            ResourcesCompat.getDrawable(resources, R.drawable.ic_place_custom, context.theme)
        } else {
            ResourcesCompat.getDrawable(resources, R.drawable.ic_place, context.theme)
        }

        if (isCustomCoordinate) {
            customLatitude = MainPreferences.getCoordinates()[0].toDouble()
            customLongitude = MainPreferences.getCoordinates()[1].toDouble()
            latLng = LatLng(customLatitude, customLongitude)
            moveMap(0F)
            addMarker()
        }

        if (GPSPreferences.isUsingVolumeKeys()) {
            this.isFocusableInTouchMode = true
            this.requestFocus()
        }

        viewHandler.postDelayed({
            /**
             * Workaround for flashing of view when map is
             * Initialized
             */
            this.animate().alpha(1F).setDuration(500).start()
        }, 500)

        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        setMultiTouchControls(true)
        setDestroyMode(true)
        overlays.add(RotationGestureOverlay(this))
        onResume()


        val mReceive: MapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                Toast.makeText(context, p.latitude.toString() + " - " + p.longitude, Toast.LENGTH_LONG).show()
                mapsCallbacks!!.onMapClicked(this@OsmMaps)
                return false
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                return false
            }
        }

        overlays.add(MapEventsOverlay(mReceive))
    }

    fun moveMap(bearing: Float) {
        controller.animateTo(
                GeoPoint(latLng!!.latitude, latLng!!.longitude),
                15.0,
                0,
                if (isBearingRotation) bearing else 0F
        )
    }

    fun addMarker() {
        launch {

            // TODO - implement pin customization

            val startMarker = Marker(this@OsmMaps).apply {
                remove(this@OsmMaps)
                icon = marker
                position = GeoPoint(latLng!!.latitude, latLng!!.longitude)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }

            launch {
                withContext(Dispatchers.Main) {
                    overlays.add(startMarker)
                }
            }
        }
    }

    fun setOnMapCallbacksListener(mapsCallbacks: OsmMapsCallbacks) {
        this.mapsCallbacks = mapsCallbacks
    }

    private val mapMoved = object : Runnable {
        override fun run() {
            if (GPSPreferences.getMapAutoCenter()) {
                val bearing: Float
                latLng = if (isCustomCoordinate) {
                    bearing = 0F
                    LatLng(customLatitude, customLongitude)
                } else {
                    if (location.isNotNull()) {
                        bearing = location!!.bearing
                        LatLng(location!!.latitude, location!!.longitude)
                    } else {
                        bearing = 0F
                        LatLng(lastLatitude, lastLongitude)
                    }
                }

                moveMap(bearing)
            }
            viewHandler.postDelayed(this, 6000L)
        }
    }

    fun postCallbacks() {
        if (GPSPreferences.getMapAutoCenter()) {
            viewHandler.post(mapMoved)
        }
    }

    override fun onResume() {
        super.onResume()
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDetach() {
        super.onDetach()
        job.cancel()
        viewHandler.removeCallbacks(mapMoved)
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            GPSPreferences.mapAutoCenter -> {
                viewHandler.removeCallbacks(mapMoved)
                viewHandler.post(mapMoved)
            }
            GPSPreferences.useBearingRotation -> {
                isBearingRotation = GPSPreferences.isBearingRotationOn()
            }
            GPSPreferences.pinSize,
            GPSPreferences.pinOpacity -> {
                if (latLng.isNotNull()) {
                    addMarker()
                }
            }
        }
    }
}