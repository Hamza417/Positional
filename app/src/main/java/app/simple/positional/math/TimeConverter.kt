package app.simple.positional.math

import java.time.ZonedDateTime

object TimeConverter {

    fun getSecondsInDegreesWithDecimalPrecision(calendar: ZonedDateTime): Float {
        return (calendar.second + calendar.nano / 1000000000f) * 6f
    }

    fun getSecondsInDegrees(zonedDateTime: ZonedDateTime): Float {
        return zonedDateTime.second * 6f
    }

    fun getMinutesInDegrees(zonedDateTime: ZonedDateTime): Float {
        return (zonedDateTime.minute + zonedDateTime.second / 60f) * 6f
    }

    fun getHoursInDegrees(zonedDateTime: ZonedDateTime): Float {
        return 0.5f * (60f * zonedDateTime.hour + zonedDateTime.minute)
    }
}
