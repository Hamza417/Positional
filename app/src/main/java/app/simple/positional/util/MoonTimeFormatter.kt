package app.simple.positional.util

import app.simple.positional.preferences.ClockPreferences
import java.text.ParseException
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object MoonTimeFormatter {

    /**
     * @param zonedDateTime here is passed a time like this 2020-10-22T06:00:05.447+05:30[Asia/Kolkata] and
     * this function will trim it to extract the 24 Hr formatted Time out of it
     */
    fun formatZonedTimeDate(zonedDateTime: ZonedDateTime): String {
        return try {
            zonedDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss").withLocale(LocaleHelper.getAppLocale()))
        } catch (e: ParseException) {
            "N/A"
        }
    }

    fun formatMoonDate(zonedDateTime: ZonedDateTime): String {
        return try {
            zonedDateTime.format(DateTimeFormatter.ofPattern(getMoonTimePattern()).withLocale(LocaleHelper.getAppLocale()))
        } catch (e: ParseException) {
            "N/A"
        }
    }

    private fun getMoonTimePattern(): String {
        return if (ClockPreferences.getDefaultClockTime()) {
            if (ClockPreferences.isUsingSecondsPrecision()) {
                "dd MMM, yyyy, hh:mm:ss a"
            } else {
                "dd MMM, yyyy, hh:mm a"
            }
        } else {
            if (ClockPreferences.isUsingSecondsPrecision()) {
                "dd MMM, yyyy, HH:mm:ss"
            } else {
                "dd MMM, yyyy, HH:mm"
            }
        }
    }
}
