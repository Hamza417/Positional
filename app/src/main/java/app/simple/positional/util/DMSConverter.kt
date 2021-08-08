package app.simple.positional.util

import android.content.Context
import android.location.Location
import app.simple.positional.R
import app.simple.positional.math.MathExtensions

object DMSConverter {
    fun latitudeAsDMS(latitude: Double, context: Context): String {
        val direction = if (latitude > 0) context.resources.getString(R.string.north_N) else context.resources.getString(R.string.south_S)
        var strLatitude = toDMS(latitude)
        strLatitude += " $direction"
        return strLatitude
    }

    fun longitudeAsDMS(longitude: Double, context: Context): String {
        val direction = if (longitude < 0) context.resources.getString(R.string.west_W) else context.resources.getString(R.string.east_E)
        var strLongitude = toDMS(longitude)
        strLongitude += " $direction"
        return strLongitude
    }

    fun getLatitudeAsDD(latitude: Double, context: Context): String {
        return if (latitude > 0)
            "${MathExtensions.round(latitude, 3)}° ${context.resources.getString(R.string.north_N)}"
        else
            "${MathExtensions.round(latitude, 3)}° ${context.resources.getString(R.string.south_S)}"
    }

    fun getLongitudeAsDD(longitude: Double, context: Context): String {
        return if (longitude > 0)
            "${MathExtensions.round(longitude, 3)}° ${context.resources.getString(R.string.east_E)}"
        else
            "${MathExtensions.round(longitude, 3)}° ${context.resources.getString(R.string.west_W)}"
    }

    fun getLatitudeAsDM(lat: Double, context: Context): String {
        val ddmLat = replaceDelimiters(Location.convert(lat, Location.FORMAT_MINUTES))
        return if (lat >= 0.0) {
            "$ddmLat ${context.resources.getString(R.string.north_N)}"
        } else {
            "${ddmLat.replaceFirst("-".toRegex(), "")} ${context.resources.getString(R.string.south_S)}"
        }
    }

    fun getLongitudeAsDM(longitude: Double, context: Context): String {
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
