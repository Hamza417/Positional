package app.simple.positional.util

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable
import app.simple.positional.R

object StringUtils {
    fun buildSpannableString(s: String): SpannableString {
        val spannableString = SpannableString(s)
        spannableString.setSpan(RelativeSizeSpan(0.5f), 5, s.length, 0) // set size
        spannableString.setSpan(ForegroundColorSpan(Color.GRAY), 5, s.length, 0) // set color
        return spannableString
    }

    fun buildSpannableString(string: String, startValue: Int): SpannableString {
        val spannableString = SpannableString(string)
        spannableString.setSpan(RelativeSizeSpan(0.5f), startValue, string.length, 0) // set size
        spannableString.setSpan(ForegroundColorSpan(Color.GRAY), startValue, string.length, 0) // set color
        return spannableString
    }

    /**
     * This function is solely used for coloring the path
     * strings in the format of a/y/z and the last index
     * of "/" is used.
     *
     * @param context used for fetching text color resource
     * @param lookupIndex [String] that needs to be looked for
     *                    conversion
     *
     * In case the string does not contain any slashes or is
     * null string or anything. This will return
     * back the normal [Spannable] string.
     */
    fun String.optimizeToColoredString(context: Context, lookupIndex: String): Spannable {
        kotlin.runCatching {
            val spannable: Spannable = SpannableString(this)
            spannable.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.textSecondary)),
                              0,
                              this.lastIndexOf(lookupIndex),
                              Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            return spannable
        }.getOrElse {
            return this.toSpannable()
        }
    }
}