package app.simple.positional.util

import android.text.SpannableString
import app.simple.positional.preference.ClockPreferences
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object DigitalTimeFormatter {
    /**
     * Returns the time in a localized format. The 12-hours format is always displayed with
     * AM/PM (and not for example vorm/nachm in german).
     *
     * @return Localized time (15:24 or 3:24 PM).
     */
    fun getTime(zoneDateTime: ZonedDateTime): SpannableString {
        return if (ClockPreferences.getDefaultClockTime()) {
            buildSpannableString(zoneDateTime.format(DateTimeFormatter.ofPattern("hh:mm a").withLocale(LocaleHelper.getAppLocale())), 2)
        } else {
            buildSpannableString(zoneDateTime.format(DateTimeFormatter.ofPattern("HH:mm").withLocale(LocaleHelper.getAppLocale())), 0)
        }
    }

    fun getTimeWithSeconds(zoneDateTime: ZonedDateTime): SpannableString {
        return if (ClockPreferences.getDefaultClockTime()) {
            buildSpannableString(zoneDateTime.format(DateTimeFormatter.ofPattern("hh:mm:ss a").withLocale(LocaleHelper.getAppLocale())), 2)
        } else {
            buildSpannableString(zoneDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss").withLocale(LocaleHelper.getAppLocale())), 0)
        }
    }
}