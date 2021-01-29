package app.simple.positional.util

import android.icu.text.MessageFormat
import android.os.Build

fun Number?.toOrdinal(): String {
    if (this == null) {
        return "N/A"
    }

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        MessageFormat.format("{0,ordinal}", this)
    } else {
        ordinal(this.toInt())
    }
}

private fun ordinal(i: Int): String {
    val suffixes = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")
    return when (i % 100) {
        11, 12, 13 -> i.toString() + "th"
        else -> StringBuilder().append(i).append(suffixes[i % 10]).toString()
    }
}
