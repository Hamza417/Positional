package app.simple.positional.decorations.maps

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import androidx.constraintlayout.helper.widget.Layer
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toDrawable
import app.simple.positional.BuildConfig
import app.simple.positional.R
import app.simple.positional.adapters.MapTilesAdapter
import app.simple.positional.constants.LocationPins
import app.simple.positional.preferences.GPSPreferences
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.preferences.OSMPreferences
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import app.simple.positional.util.BitmapHelper.toBitmap
import app.simple.positional.util.ColorUtils
import app.simple.positional.util.NullSafety.isNotNull
import app.simple.positional.util.StatusBarHeight
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import org.osmdroid.config.Configuration
import org.osmdroid.events.*
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.ScaleBarOverlay
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

    var address: String = ""

    private var latLng: LatLng? = null
    private var mapsCallbacks: OsmMapsCallbacks? = null
    private var marker: Drawable? = null
    private val viewHandler = Handler(Looper.getMainLooper())
    private var mapViewListener: MapListener

    private var isCustomCoordinate = false
    private var isBearingRotation = false
    private var customLatitude = 0.0
    private var customLongitude = 0.0
    private var bearing = 0F
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
            ResourcesCompat.getDrawable(resources, LocationPins.locationsPins[GPSPreferences.getPinSkin()], null)
        }

        if (isCustomCoordinate) {
            customLatitude = MainPreferences.getCoordinates()[0].toDouble()
            customLongitude = MainPreferences.getCoordinates()[1].toDouble()
            latLng = LatLng(customLatitude, customLongitude)
            moveMap()
            addMarker()
        } else {
            controller.animateTo(GeoPoint(lastLatitude, lastLongitude),
                    GPSPreferences.getMapZoom().toDouble(),
                    0,
                    bearing)
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
        setTileSource()
        setMultiTouchControls(true)
        setDestroyMode(true)
        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        maxZoomLevel = 15.0
        minZoomLevel = 9.0
        setMapStyle()
        overlays.add(RotationGestureOverlay(this))
        setLayerType(Layer.LAYER_TYPE_HARDWARE, null)
        controller.setZoom(GPSPreferences.getMapZoom().toDouble())
        this.overlayManager.tilesOverlay.loadingLineColor = Color.DKGRAY
        showScaleBar()
        showCopyrightNotice()
        onResume()

        val mReceive: MapEventsReceiver = object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                // Toast.makeText(context, p.latitude.toString() + " - " + p.longitude, Toast.LENGTH_LONG).show()
                mapsCallbacks!!.onMapClicked(this@OsmMaps)
                return false
            }

            override fun longPressHelper(p: GeoPoint): Boolean {
                return false
            }
        }

        mapViewListener = DelayedMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent): Boolean {
                viewHandler.removeCallbacks(mapMoved)
                return true
            }

            override fun onZoom(event: ZoomEvent): Boolean {
                GPSPreferences.setMapZoom(event.zoomLevel.toFloat())
                return true
            }
        }, 500L)

        addMapListener(mapViewListener)
        overlays.add(MapEventsOverlay(mReceive))
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                controller.stopAnimation(false)
                viewHandler.removeCallbacks(mapMoved)
            }
            MotionEvent.ACTION_UP -> {
                postCallbacks()
            }
        }

        return super.dispatchTouchEvent(event)
    }

    fun moveMap() {
        /**
         * Treat Speed as duration
         */
        controller.animateTo(
                GeoPoint(latLng!!.latitude, latLng!!.longitude),
                GPSPreferences.getMapZoom().toDouble(),
                3000,
                bearing
        )

        // controller.setCenter(GeoPoint(latLng!!.latitude, latLng!!.longitude))
    }

    fun resetCamera() {
        /**
         * Treat Speed as duration
         */
        controller.animateTo(
                GeoPoint(latLng!!.latitude, latLng!!.longitude),
                15.0,
                3000,
                if (isBearingRotation) bearing else 0F
        )

        GPSPreferences.setMapZoom(15.0F)
    }

    private fun addMarker() {
        launch {

            overlays.forEach {
                if (it is Marker && it.id == "marker") {
                    overlays.remove(it)
                }
            }

            marker = if (isCustomCoordinate) {
                R.drawable.ic_place_custom.toBitmap(context, GPSPreferences.getPinSize(), GPSPreferences.getPinOpacity())
            } else {
                if (location.isNotNull()) {
                    LocationPins.locationsPins[GPSPreferences.getPinSkin()].toBitmap(context, GPSPreferences.getPinSize(), GPSPreferences.getPinOpacity())
                } else {
                    R.drawable.ic_place_historical.toBitmap(context, GPSPreferences.getPinSize(), GPSPreferences.getPinOpacity())
                }
            }.toDrawable(resources)

            val startMarker = Marker(this@OsmMaps).apply {
                icon = marker
                id = "marker"
                title = address
                position = GeoPoint(latLng!!.latitude, latLng!!.longitude)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            }

            launch {
                withContext(Dispatchers.Main) {
                    overlays.add(startMarker)
                    invalidate()
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

                moveMap()
            }
            viewHandler.postDelayed(this, 6000L)
        }
    }

    fun postCallbacks() {
        if (GPSPreferences.getMapAutoCenter()) {
            viewHandler.postDelayed(mapMoved, 6000L)
        }
    }

    private fun setTileSource() {
        val x = OSMPreferences.getMapTileProvider()
        for (i in MapTilesAdapter.list) {
            if (i.second == x) {
                setTileSource(i.first)
            }
        }
    }

    private fun showScaleBar() {
        val dm: DisplayMetrics = context.resources.displayMetrics
        val scaleBarOverlay = ScaleBarOverlay(this)
        scaleBarOverlay.setCentred(true)
        scaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, StatusBarHeight.getStatusBarHeight(resources) + 50)
        overlays.add(scaleBarOverlay)
    }

    private fun showCopyrightNotice() {
        val copyrightNotice: String = tileProvider.tileSource.copyrightNotice
        val copyrightOverlay = CopyrightOverlay(context)
        copyrightOverlay.setCopyrightNotice(copyrightNotice)
        overlays.add(copyrightOverlay)
    }

    private fun setMapStyle() {
        when (this.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) {
            android.content.res.Configuration.UI_MODE_NIGHT_YES -> {
                overlayManager.tilesOverlay.setColorFilter(ColorUtils.getMapColorFilter())
            }
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
        removeMapListener(mapViewListener)
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
            GPSPreferences.pinOpacity,
            GPSPreferences.pinSkin -> {
                if (latLng.isNotNull()) {
                    addMarker()
                }
            }
            OSMPreferences.mapTileProvider -> {
                setTileSource()
            }
        }
    }
}
