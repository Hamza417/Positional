package app.simple.positional.decorations.maps

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Bitmap
import android.location.Location
import android.util.AttributeSet
import app.simple.positional.R
import app.simple.positional.preference.GPSPreferences
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import app.simple.positional.util.BitmapHelper.toBitmap
import app.simple.positional.util.NullSafety.isNotNull
import app.simple.positional.util.NullSafety.isNull
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*

class Maps(context: Context?, attributeSet: AttributeSet?, i: Int) : MapView(context, attributeSet, i),
                                                                     OnMapReadyCallback, SharedPreferences.OnSharedPreferenceChangeListener {
    private var googleMap: GoogleMap? = null
    private var location: Location? = null
    private var mapsCallbacks: MapsCallbacks? = null
    private var marker: Bitmap? = null

    private var isCustomCoordinate = false
    private var customLatitude = 0.0
    private var customLongitude = 0.0

    init {
        getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap

        this.onResume()

        /**
         * Workaround for flashing of view when map is
         * Initialized
         */
        this.animate().alpha(1F).setDuration(500).start()

        marker = if (isCustomCoordinate) {
            R.drawable.ic_place_custom.toBitmap(context, 400)
        } else {
            R.drawable.ic_place.toBitmap(context, 400)
        }

        val latLng = if (isCustomCoordinate)
            LatLng(customLatitude, customLongitude)
        else
            LatLng(GPSPreferences.getLastCoordinates()[0].toDouble(), GPSPreferences.getLastCoordinates()[1].toDouble())

        googleMap.uiSettings.isCompassEnabled = false
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.uiSettings.isMyLocationButtonEnabled = false

        this.googleMap = googleMap

        addMarker(latLng)
        setMapStyle(GPSPreferences.isLabelOn())
        setSatellite()
        setBuildings(GPSPreferences.getShowBuildingsOnMap())

        this.googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(
                latLng,
                GPSPreferences.getMapZoom(),
                GPSPreferences.getMapTilt(),
                0F)))

        this.googleMap?.setOnCameraMoveListener {
            handler.removeCallbacks(mapMoved)
        }

        this.googleMap?.setOnCameraIdleListener {
            GPSPreferences.setMapZoom(this.googleMap?.cameraPosition!!.zoom)
            GPSPreferences.setMapTilt(this.googleMap?.cameraPosition!!.tilt)
            handler.removeCallbacks(mapMoved)
            handler.postDelayed(mapMoved, 10000)
        }

        this.googleMap?.setOnMapClickListener {
            mapsCallbacks?.onMapClicked(this)
        }
    }

    fun lowMemory() {
        onLowMemory()
    }

    fun resume() {
        onResume()
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
    }

    fun destroy() {
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
        clearAnimation()
        onDestroy()
    }

    private fun setSatellite() {
        if (googleMap.isNotNull())
            googleMap?.mapType = if (GPSPreferences.isSatelliteOn()) {
                if (GPSPreferences.isLabelOn()) {
                    GoogleMap.MAP_TYPE_HYBRID
                } else {
                    GoogleMap.MAP_TYPE_SATELLITE
                }
            } else {
                GoogleMap.MAP_TYPE_NORMAL
            }
    }

    private fun setBuildings(value: Boolean) {
        googleMap!!.isBuildingsEnabled = value
    }

    private fun setMapStyle(value: Boolean) {

        setSatellite()

        if (!googleMap.isNull()) {
            if (GPSPreferences.getHighContrastMap()) {
                googleMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                        context,
                        if (value) {
                            R.raw.map_high_contrast_labelled
                        } else {
                            R.raw.map_high_contrast_non_labelled
                        }
                ))

            } else {
                googleMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                        context,
                        when (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                            Configuration.UI_MODE_NIGHT_YES -> {
                                if (value) {
                                    R.raw.maps_dark_labelled
                                } else {
                                    R.raw.maps_dark_no_label
                                }
                            }
                            Configuration.UI_MODE_NIGHT_NO -> {
                                if (value) {
                                    R.raw.maps_light_labelled
                                } else {
                                    R.raw.maps_no_label
                                }
                            }
                            else -> 0
                        }
                ))
            }
        }
    }

    private fun addMarker(latLng: LatLng) {
        if (googleMap.isNull()) return
        googleMap?.clear()
        googleMap?.addMarker(MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(marker)))
    }

    private val mapMoved = object : Runnable {
        override fun run() {
            if (GPSPreferences.getMapAutoCenter()) {
                val latLng = if (isCustomCoordinate) LatLng(customLatitude, customLongitude) else LatLng(location!!.latitude, location!!.longitude)
                moveMapCamera(latLng, GPSPreferences.getMapZoom())
            }
            handler.postDelayed(this, 6000L)
        }
    }

    private fun moveMapCamera(latLng: LatLng, zoom: Float) {
        if (googleMap.isNull()) return

        googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder()
                .target(latLng)
                .tilt(GPSPreferences.getMapTilt())
                .zoom(zoom)
                .bearing(0F).build()), 3000, null)
    }

    fun setOnMapsCallbackListener(mapsCallbacks: MapsCallbacks) {
        this.mapsCallbacks = mapsCallbacks
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            GPSPreferences.highContrastMap, GPSPreferences.GPSLabelMode -> {
                setMapStyle(GPSPreferences.isLabelOn())
            }
            GPSPreferences.GPSSatellite -> {
                setSatellite()
            }
            GPSPreferences.showBuilding -> {
                setBuildings(GPSPreferences.getShowBuildingsOnMap())
            }
            GPSPreferences.mapAutoCenter -> {
                handler.removeCallbacks(mapMoved)
                handler.post(mapMoved)
            }
            GPSPreferences.useVolumeKeys -> {
                this.isFocusableInTouchMode = GPSPreferences.isUsingVolumeKeys()
                if (GPSPreferences.isUsingVolumeKeys()) {
                    this.requestFocus()
                } else {
                    this.clearFocus()
                }
            }
        }
    }
}