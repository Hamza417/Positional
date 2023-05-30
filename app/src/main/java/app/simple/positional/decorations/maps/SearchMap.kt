package app.simple.positional.decorations.maps

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import app.simple.positional.R
import app.simple.positional.extensions.maps.CustomMaps
import app.simple.positional.preferences.MainPreferences
import app.simple.positional.util.ConditionUtils.isNull
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions

class SearchMap(context: Context, attrs: AttributeSet) : CustomMaps(context, attrs) {

    internal var callbacks: (latLng: LatLng) -> Unit = {}

    init {
        getMapAsync(this)
    }

    override fun onMapReady(p0: GoogleMap) {
        super.onMapReady(p0)
        setMapStyle()
        moveMapToLastLocation()

        p0.setOnMapClickListener {
            latLng = it
            callbacks.invoke(it)
        }
    }

    private fun moveMapToLastLocation() {
        val latLng = LatLng(
                MainPreferences.getLastCoordinates()[0].toDouble(),
                MainPreferences.getLastCoordinates()[1].toDouble())

        this.googleMap?.moveCamera(
                CameraUpdateFactory.newCameraPosition(
                        CameraPosition(latLng, 15F, 0F, 0F)
                )
        )

        this.googleMap?.setOnCameraMoveListener {
            callbacks.invoke(googleMap?.cameraPosition?.target!!)
        }
    }

    private fun setMapStyle() {
        if (!googleMap.isNull()) {
            googleMap?.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            context,
                            when (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                                Configuration.UI_MODE_NIGHT_YES -> {
                                    R.raw.maps_dark_labelled
                                }
                                Configuration.UI_MODE_NIGHT_NO -> {
                                    R.raw.maps_light_labelled
                                }
                                else -> 0
                            }
                    )
            )
        }
    }

    fun moveCamera(latitude: Double, longitude: Double) {
        googleMap?.moveCamera(
                CameraUpdateFactory.newCameraPosition(
                        CameraPosition(LatLng(latitude, longitude), 15F, 0F, 0F)
                )
        )
    }
}