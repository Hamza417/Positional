package app.simple.positional.util

import android.content.Context
import android.location.Location
import android.location.LocationManager
import app.simple.positional.model.TrailPoint
import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

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
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }

    fun isValidLongitude(longitude: Double): Boolean {
        return longitude >= -180 && longitude <= 180
    }

    fun isValidLatitude(latitude: Double): Boolean {
        return latitude >= -90 && latitude <= 90
    }

    fun isHemisphereNorth(latitude: Double): Boolean {
        return latitude >= 0
    }

    fun isHemisphereSouth(latitude: Double): Boolean {
        return latitude < 0
    }

    fun measureDisplacement(list: List<TrailPoint>): Float {

        val result = floatArrayOf(0f)
        var distance = 0F

        for (i in list.indices) {
            if (i < list.size - 1) {
                Location.distanceBetween(
                    list[i].latitude,
                    list[i].longitude,
                    list[i + 1].latitude,
                    list[i + 1].longitude,
                    result
                )

                distance += result[0]
            }
        }

        return distance
    }

    /**
     * Calculated distance in meters
     */
    fun measureDisplacement(list: Array<LatLng>): Float {
        val result = floatArrayOf(0f)
        var distance = 0F

        for (i in list.indices) {
            if (i < list.size - 1) {
                Location.distanceBetween(
                    list[i].latitude,
                    list[i].longitude,
                    list[i + 1].latitude,
                    list[i + 1].longitude,
                    result
                )

                distance += result[0]
            }
        }

        return distance
    }

    fun calculateBearingAngle(startLatitude: Double, startLongitude: Double, endLatitude: Double, endLongitude: Double): Double {
        val phi1 = Math.toRadians(startLatitude)
        val phi2 = Math.toRadians(endLatitude)
        val deltaLambda = Math.toRadians(endLongitude - startLongitude)
        val theta: Double =
            atan2(sin(deltaLambda) * cos(phi2), cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda))
        return Math.toDegrees(theta)
    }
}
