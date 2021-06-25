package app.simple.positional.util

import android.content.Context
import android.location.Location
import app.simple.positional.R
import app.simple.positional.math.MathExtensions

object DMSConverter {
    fun latitudeAsDMS(latitude: Double, decimalPlace: Int, context: Context): String {
        val direction = if (latitude > 0) context.resources.getString(R.string.north_N) else context.resources.getString(R.string.south_S)
        var strLatitude = replaceDelimiters(Location.convert(latitude, Location.FORMAT_SECONDS), decimalPlace)
        strLatitude += " $direction"
        return strLatitude
    }

    fun longitudeAsDMS(longitude: Double, decimalPlace: Int, context: Context): String {
        val direction = if (longitude < 0) context.resources.getString(R.string.west_W) else context.resources.getString(R.string.east_E)
        var strLongitude = replaceDelimiters(Location.convert(longitude, Location.FORMAT_SECONDS), decimalPlace)
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
        val ddmLat = replaceDelimiters(Location.convert(lat, Location.FORMAT_MINUTES), 3)
        return if (lat >= 0.0) {
            "$ddmLat ${context.resources.getString(R.string.north_N)}"
        } else {
            "${ddmLat.replaceFirst("-".toRegex(), "")} ${context.resources.getString(R.string.south_S)}"
        }
    }

    fun getLongitudeAsDM(longitude: Double, context: Context): String {
        val ddLongitude = replaceDelimiters(Location.convert(longitude, Location.FORMAT_MINUTES), 3)
        return if (longitude >= 0.0) {
            "$ddLongitude ${context.resources.getString(R.string.east_E)}"
        } else {
            "${ddLongitude.replaceFirst("-".toRegex(), "")} ${context.resources.getString(R.string.west_W)}"
        }
    }

    private fun replaceDelimiters(str: String, decimalPlace: Int): String {
        var string = str
        string = string.replaceFirst(":".toRegex(), "°")
        string = string.replaceFirst(":".toRegex(), "'")
        val pointIndex = string.indexOf(".")
        val endIndex = pointIndex + 1 + decimalPlace
        if (endIndex < string.length) {
            string = string.substring(0, endIndex)
        }
        string += "\""
        return string
    }
}
