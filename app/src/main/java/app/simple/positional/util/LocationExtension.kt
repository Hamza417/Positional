package app.simple.positional.util

import android.content.Context
import android.location.LocationManager

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
}
