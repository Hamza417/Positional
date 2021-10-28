package app.simple.positional.util

import android.icu.text.MessageFormat

object Ordinal {
    fun Number?.toOrdinal(): String {
        if (this == null) {
            return "N/A"
        }

        val testArgs = arrayOf<Any>(this)
        val messageFormat = MessageFormat("{0,ordinal}", LocaleHelper.getAppLocale())

        return messageFormat.format(testArgs)
    }

    private fun ordinal(i: Int): String {
        val suffixes = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")
        return when (i % 100) {
            11, 12, 13 -> i.toString() + "th"
            else -> StringBuilder().append(i).append(suffixes[i % 10]).toString()
        }
    }
}
