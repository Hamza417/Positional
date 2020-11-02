package app.simple.positional.util

import java.text.SimpleDateFormat
import java.util.*

/**
 * @param string here is passed a time like this {2020-10-22T06:00:05.447+05:30[Asia/Kolkata]} and
 * this function will trim it to extract the Time out of it
 */
fun formatZonedTimeDate(string: String): String {
    val date: Date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(string)
    return SimpleDateFormat("H:mm:ss").format(date) // 9:00
}