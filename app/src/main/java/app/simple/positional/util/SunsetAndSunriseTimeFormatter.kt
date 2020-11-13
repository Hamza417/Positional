package app.simple.positional.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * @param string here is passed a time like this {2020-10-22T06:00:05.447+05:30[Asia/Kolkata]} and
 * this function will trim it to extract the Time out of it
 */
fun formatZonedTimeDate(string: String): String {
    return try {
        val date: Date? = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault()).parse(string)
        SimpleDateFormat("H:mm:ss", Locale.getDefault()).format(date!!) // 9:00
    } catch (e: ParseException) {
        "N/A"
    }
}

fun formatMoonDate(string: String): String {
    return try {
        val date: Date? = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault()).parse(string)
        SimpleDateFormat("dd MMM, yyyy, H:mm:ss", Locale.getDefault()).format(date!!) // 12 Dec 2020, 9:00
    } catch (e: ParseException) {
        "N/A"
    }
}