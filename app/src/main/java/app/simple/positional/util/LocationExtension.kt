package app.simple.positional.util

import android.content.Context
import android.location.LocationManager
import com.google.android.gms.maps.model.LatLng
import kotlin.math.*

object LocationExtension {
    fun getLocationStatus(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
    }

    fun getDirection(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val pi = Math.PI
        val deltaTheta = ln(tan(lat2 / 2 + pi / 4) / tan(lat1 / 2 + pi / 4))
        val deltaLongitude = abs(lng1 - lng2)
        val theta = atan2(deltaLongitude, deltaTheta)
        return Math.toDegrees(theta) //direction in degree
    }

    fun bearing(endPoint: LatLng, startPoint: LatLng): String {

        val radians = atan2((endPoint.longitude - startPoint.longitude), (endPoint.latitude - startPoint.longitude))

        val compassReading = radians * (180 / Math.PI)

        val coordinateNames = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW", "N")
        var coordinateIndex = (compassReading / 45).roundToInt()
        if (coordinateIndex < 0) {
            coordinateIndex += 8
        }
        return coordinateNames[coordinateIndex] // returns the coordinate value
    }
}