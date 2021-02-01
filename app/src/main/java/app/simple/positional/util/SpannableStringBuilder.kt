package app.simple.positional.util

import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan

fun buildSpannableString(s: String): SpannableString {
    val spannableString = SpannableString(s)
    spannableString.setSpan(RelativeSizeSpan(0.5f), 5, s.length, 0) // set size
    spannableString.setSpan(ForegroundColorSpan(Color.GRAY), 5, s.length, 0) // set color
    return spannableString
}