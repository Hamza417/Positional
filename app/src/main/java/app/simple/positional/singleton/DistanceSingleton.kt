package app.simple.positional.singleton

import com.google.android.gms.maps.model.LatLng

object DistanceSingleton {
    var totalDistance: Float? = null
    var initialPointCoordinates: LatLng? = null
    var distanceCoordinates: LatLng? = null

    /**
     * This is used to check if the map panel is visible
     *
     * If visible the calculation will be done inside the panel
     * else the calculation will be done in service until the
     * panel is opened again
     */
    var isMapPanelVisible: Boolean? = null

    /**
     *  Makes sure that the [initialPointCoordinates] is set only once throughout
     *  the application lifecycle
     */
    var isInitialLocationSet: Boolean? = null
    var isNotificationAllowed: Boolean? = null

    init {
        totalDistance = 0f
        initialPointCoordinates = LatLng(0.0, 0.0)
        distanceCoordinates = LatLng(0.0, 0.0)
        isMapPanelVisible = false
        isInitialLocationSet = false
        isNotificationAllowed = false
    }
}