package app.simple.positional.singleton

import com.google.android.gms.maps.model.LatLng

object DistanceSingleton {
    var totalDistance: Float? = null
    var initialPointCoordinates: LatLng? = null
    var distanceCoordinates: LatLng? = null

    /**
     *  Makes sure that the [initialPointCoordinates] is set only once throughout
     *  the application lifecycle
     */
    var isInitialLocationSet: Boolean? = null

    init {
        totalDistance = 0f
        initialPointCoordinates = LatLng(0.0, 0.0)
        distanceCoordinates = LatLng(0.0, 0.0)
        isInitialLocationSet = false
    }
}
