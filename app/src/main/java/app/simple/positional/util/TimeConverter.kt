package app.simple.positional.util

import java.time.ZoneOffset
import java.time.ZonedDateTime

fun getSecondsInDegreesWithDecimalPrecision(calendar: ZonedDateTime): Float {
    return (calendar.second + (calendar.nano / 1000000000f)) * 6f
}

fun getSecondsInDegrees(zonedDateTime: ZonedDateTime): Float {
    return zonedDateTime.second * 6f
}

fun getMinutesInDegrees(zonedDateTime: ZonedDateTime): Float {
    val value = ((zonedDateTime.second / 60f) * 100f) / 100f
    return (zonedDateTime.minute + value) * 6f
}

fun getHoursInDegrees(zonedDateTime: ZonedDateTime): Float {
    return 0.5f * (60f * zonedDateTime.hour + zonedDateTime.minute)
}

fun isValidTimeZone(timezone: String): Boolean {
    val validIDs = ZoneOffset.getAvailableZoneIds()
    for (str in validIDs) {
        if (str != null && str == timezone) {
            return true
        }
    }

    return false
}