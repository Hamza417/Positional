package app.simple.positional.util

import android.content.Context
import android.location.Location
import app.simple.positional.R
import app.simple.positional.math.MathExtensions
import app.simple.positional.preferences.MainPreferences
import com.google.android.gms.maps.model.LatLng
import kotlin.math.abs

object DMSConverter {

    private const val DD_DDDDDD_PLACES = 6

    fun getFormattedCoordinates(location: Location, context: Context): Array<String> {
        return when (MainPreferences.getCoordinatesFormat()) {
            0 -> { // DD
                arrayOf(
                        latitudeAsDD(location.latitude),
                        longitudeAsDD(location.longitude)
                )
            }
            1 -> { // DDM
                arrayOf(
                        latitudeAsDM(location.latitude, context),
                        longitudeAsDM(location.longitude, context)
                )
            }
            2 -> { // DMS
                arrayOf(
                        latitudeAsDMS(location.latitude, context),
                        longitudeAsDMS(location.longitude, context)
                )
            }
            else -> { // DD
                arrayOf(
                        latitudeAsDD(location.latitude),
                        longitudeAsDD(location.longitude)
                )
            }
        }
    }

    fun getFormattedCoordinates(latLng: LatLng, context: Context): Array<String> {
        return when (MainPreferences.getCoordinatesFormat()) {
            0 -> { // DD
                arrayOf(
                        latitudeAsDD(latLng.latitude),
                        longitudeAsDD(latLng.longitude)
                )
            }
            1 -> { // DDM
                arrayOf(
                        latitudeAsDM(latLng.latitude, context),
                        longitudeAsDM(latLng.longitude, context)
                )
            }
            2 -> { // DMS
                arrayOf(
                        latitudeAsDMS(latLng.latitude, context),
                        longitudeAsDMS(latLng.longitude, context)
                )
            }
            else -> { // DD
                arrayOf(
                        latitudeAsDD(latLng.latitude),
                        longitudeAsDD(latLng.longitude)
                )
            }
        }
    }

    fun getFormattedLatitude(latitude: Double, context: Context): String {
        return when (MainPreferences.getCoordinatesFormat()) {
            0 -> { // DD
                latitudeAsDD(latitude)
            }
            1 -> { // DDM
                latitudeAsDM(latitude, context)
            }
            2 -> { // DMS
                latitudeAsDMS(latitude, context)
            }
            else -> { // DD
                latitudeAsDD(latitude)
            }
        }
    }

    fun getFormattedLongitude(longitude: Double, context: Context): String {
        return when (MainPreferences.getCoordinatesFormat()) {
            0 -> { // DD
                longitudeAsDD(longitude)
            }
            1 -> { // DDM
                longitudeAsDM(longitude, context)
            }
            2 -> { // DMS
                longitudeAsDMS(longitude, context)
            }
            else -> { // DD
                longitudeAsDD(longitude)
            }
        }
    }

    fun latitudeAsDMS(latitude: Double, context: Context): String {
        val direction = if (latitude < 0) context.resources.getString(R.string.south_S) else context.resources.getString(R.string.north_N)
        var strLatitude = toDMS(abs(latitude))
        strLatitude += " $direction"
        return strLatitude
    }

    fun longitudeAsDMS(longitude: Double, context: Context): String {
        val direction = if (longitude < 0) context.resources.getString(R.string.west_W) else context.resources.getString(R.string.east_E)
        var strLongitude = toDMS(abs(longitude))
        strLongitude += " $direction"
        return strLongitude
    }

    fun getDMS(latLng: LatLng, context: Context): String {
        return latitudeAsDMS(latLng.latitude, context) + " | " + longitudeAsDMS(latLng.longitude, context)
    }

    fun latitudeAsDD(latitude: Double): String {
        return if (latitude > 0)
            "${MathExtensions.round(latitude, DD_DDDDDD_PLACES)}°"
        else
            "${MathExtensions.round(latitude, DD_DDDDDD_PLACES)}°"
    }

    fun longitudeAsDD(longitude: Double): String {
        return if (longitude > 0)
            "${MathExtensions.round(longitude, DD_DDDDDD_PLACES)}°"
        else
            "${MathExtensions.round(longitude, DD_DDDDDD_PLACES)}°"
    }

    fun latitudeAsDM(lat: Double, context: Context): String {
        val ddmLat = replaceDelimiters(Location.convert(lat, Location.FORMAT_MINUTES))
        return if (lat >= 0.0) {
            "$ddmLat ${context.resources.getString(R.string.north_N)}"
        } else {
            "${ddmLat.replaceFirst("-".toRegex(), "")} ${context.resources.getString(R.string.south_S)}"
        }
    }

    fun longitudeAsDM(longitude: Double, context: Context): String {
        val ddLongitude = replaceDelimiters(Location.convert(longitude, Location.FORMAT_MINUTES))
        return if (longitude >= 0.0) {
            "$ddLongitude ${context.resources.getString(R.string.east_E)}"
        } else {
            "${ddLongitude.replaceFirst("-".toRegex(), "")} ${context.resources.getString(R.string.west_W)}"
        }
    }

    private fun replaceDelimiters(string: String): String {
        return string.replaceFirst(":".toRegex(), "° ").replaceFirst(":".toRegex(), "' ") + "\""
    }

    private fun toDMS(number: Double): String {
        val degree = kotlin.math.floor(number)
        val minutes = (number - kotlin.math.floor(number)) * 60.0
        val seconds = (minutes - kotlin.math.floor(minutes)) * 60.0

        return "%1\$d%2\$s %3\$d%4\$s %5\$.${2}f%6\$s".format(
                degree.toInt(), "°",
                minutes.toInt(), "'",
                seconds, "\""
        )
    }
}
