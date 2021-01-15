package app.simple.positional.util

import android.os.Build
import android.text.Html
import android.text.Spanned
import java.util.*

object HtmlHelper {

    fun fromHtml(str: String): Spanned? {
        /**
         * DEPRECATION is used because [Html.fromHtml] causes various API level issues
         */
        @Suppress("DEPRECATION")
        return if (Build.VERSION.SDK_INT >= 24) Html.fromHtml(formatString(str), Html.FROM_HTML_MODE_LEGACY) else Html.fromHtml(formatString(str))
    }

    private fun formatString(str: String): String {
        return String.format(Locale.getDefault(), "%s", str)
    }
}