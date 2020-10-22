package app.simple.positional.util

import java.util.*

fun getSecondsInDegrees(calendar: Calendar): Float {
    return calendar.get(Calendar.SECOND) * 6f
}

fun getMinutesInDegrees(calendar: Calendar): Float {
    return calendar.get(Calendar.MINUTE) * 6f
}

fun getHoursInDegrees(calendar: Calendar): Float {
    return 0.5f * (60f * calendar.get(Calendar.HOUR) + calendar.get(Calendar.MINUTE))
}