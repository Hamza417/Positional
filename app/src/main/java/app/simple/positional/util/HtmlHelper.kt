package app.simple.positional.util

import android.os.Build
import android.text.Html
import android.text.Spanned
import java.util.*

object HtmlHelper {
    fun fromHtml(str: String): Spanned {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(formatString(str), Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(formatString(str))
        }
    }

    private fun formatString(str: String): String {
        /**
         * TODO - fix locale bug
         */
        return String.format(Locale.ROOT, "%s", str)
    }
}
