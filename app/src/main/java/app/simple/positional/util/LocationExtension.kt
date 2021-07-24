package app.simple.positional.util

import android.content.Context
import android.location.Location
import android.location.LocationManager
import app.simple.positional.model.TrailData

object LocationExtension {

    /**
     * Checks if location is turned on in the device.
     *
     * @return true if on else false
     */
    fun getLocationStatus(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }

    fun isValidLongitude(longitude: Double): Boolean {
        return longitude >= -180 && longitude <= 180
    }

    fun isValidLatitude(latitude: Double): Boolean {
        return latitude >= -90 && latitude <= 90
    }

    fun measureDisplacement(list: List<TrailData>): Float {

        val result = floatArrayOf(0f, 0f)

        for (i in list.indices) {
            if (i < list.size - 1) {
                Location.distanceBetween(
                        list[i].latitude,
                        list[i].longitude,
                        list[i + 1].latitude,
                        list[i + 1].longitude,
                        result
                )
            }
        }

        return result[0]
    }
}
