package app.simple.positional.util

import android.content.Context
import android.text.SpannableString
import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Returns the time in a localized format. The 12-hours format is always displayed with
 * AM/PM (and not for example vorm/nachm in german).
 *
 * @param context the context
 * @return Localized time (15:24 or 3:24 PM).
 */
fun getTime(context: Context?, calendar: Calendar): SpannableString? {
    return if (DateFormat.is24HourFormat(context)) {
        buildSpannableString(SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time).toString().toUpperCase(Locale.getDefault()), 0)
    } else {
        buildSpannableString(SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time).toString().toUpperCase(Locale.getDefault()), 2)
    }
}