package app.simple.positional.util

import java.util.*

fun getSecondsInDegreesWithDecimalPrecision(calendar: Calendar): Float {
    val value = calendar.get(Calendar.MILLISECOND) / 1000f
    return (calendar.get(Calendar.SECOND) + value) * 6f
}

fun getSecondsInDegrees(calendar: Calendar): Float {
    return calendar.get(Calendar.SECOND) * 6f
}

fun getMinutesInDegrees(calendar: Calendar): Float {
    val value = ((calendar.get(Calendar.SECOND) / 60f) * 100f) / 100f
    return (calendar.get(Calendar.MINUTE) + value) * 6f
}

fun getHoursInDegrees(calendar: Calendar): Float {
    return 0.5f * (60f * calendar.get(Calendar.HOUR) + calendar.get(Calendar.MINUTE))
}