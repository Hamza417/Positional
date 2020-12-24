package app.simple.positional.util

import java.text.ParseException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * @param zonedDateTime here is passed a time like this 2020-10-22T06:00:05.447+05:30[Asia/Kolkata] and
 * this function will trim it to extract the 24 Hr formatted Time out of it
 */
fun formatZonedTimeDate(zonedDateTime: ZonedDateTime): String {
    return try {
        zonedDateTime.format(DateTimeFormatter.ofPattern("H:mm:ss"))
    } catch (e: ParseException) {
        "N/A"
    }
}

fun formatMoonDate(zonedDateTime: ZonedDateTime): String {
    return try {
        val date = DateTimeFormatter.ofPattern("dd MMM, yyyy, H:mm:ss")
        zonedDateTime.format(date)
    } catch (e: ParseException) {
        "N/A"
    }
}