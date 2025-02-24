package app.simple.positional.util

import android.text.Html
import android.text.Spanned

object HtmlHelper {
    fun fromHtml(str: String): Spanned {
        return Html.fromHtml(formatString(str), Html.FROM_HTML_MODE_COMPACT)
    }

    private fun formatString(str: String): String {
        return String.format(LocaleHelper.getAppLocale(), "%s", str)
    }
}
