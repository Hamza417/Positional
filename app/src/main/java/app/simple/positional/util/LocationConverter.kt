package app.simple.positional.util

import android.location.Location
import kotlin.math.absoluteValue

object LocationConverter {

    fun latitudeAsDMS(latitude: Double, decimalPlace: Int): String {
        val direction = if (latitude > 0) "N" else "S"
        var strLatitude = Location.convert(latitude.absoluteValue, Location.FORMAT_SECONDS)
        strLatitude = replaceDelimiters(strLatitude, decimalPlace)
        strLatitude += " $direction"
        return strLatitude
    }

    fun longitudeAsDMS(longitude: Double, decimalPlace: Int): String {
        val direction = if (longitude > 0) "W" else "E"
        var strLongitude = Location.convert(longitude.absoluteValue, Location.FORMAT_SECONDS)
        strLongitude = replaceDelimiters(strLongitude, decimalPlace)
        strLongitude += " $direction"
        return strLongitude
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