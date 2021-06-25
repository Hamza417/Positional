package app.simple.positional.math

import java.time.ZonedDateTime

object TimeConverter {
    fun getSecondsInDegrees(zonedDateTime: ZonedDateTime, floatPrecision: Boolean): Float {
        return if (floatPrecision) {
            (zonedDateTime.second + zonedDateTime.nano / 1000000000f) * 6f
        } else {
            zonedDateTime.second * 6f
        }
    }

    fun getMinutesInDegrees(zonedDateTime: ZonedDateTime): Float {
        return (zonedDateTime.minute + zonedDateTime.second / 60f) * 6f
    }

    fun getHoursInDegrees(zonedDateTime: ZonedDateTime): Float {
        return zonedDateTime.hour * (360.0F / 12F) + zonedDateTime.minute * (360.0F / 12F / 60F)
    }

    fun getHoursInDegreesFor24(zonedDateTime: ZonedDateTime): Float {
        return zonedDateTime.hour * (360.0F / 24F) + zonedDateTime.minute * (360.0F / 24F / 60F)
    }
}
