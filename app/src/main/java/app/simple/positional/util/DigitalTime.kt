package app.simple.positional.util

import android.content.Context
import android.text.SpannableString
import android.text.format.DateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Returns the time in a localized format. The 12-hours format is always displayed with
 * AM/PM (and not for example vorm/nachm in german).
 *
 * @param context the context
 * @return Localized time (15:24 or 3:24 PM).
 */
fun getTime(context: Context?, zoneDateTime: ZonedDateTime): SpannableString {
    return if (DateFormat.is24HourFormat(context)) {
        buildSpannableString(zoneDateTime.format(DateTimeFormatter.ofPattern("HH:mm")), 0)
    } else {
        buildSpannableString(zoneDateTime.format(DateTimeFormatter.ofPattern("hh:mm a")), 2)
    }
}