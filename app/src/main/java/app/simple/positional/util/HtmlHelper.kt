package app.simple.positional.util

import android.os.Build
import android.text.Html
import android.text.Spanned

const val DEPRECATION = "DEPRECATION"

object HtmlHelper {
    fun fromHtml(str: String?): Spanned? {
        /**
         * [DEPRECATION] is used because [Html.fromHtml] causes various API level issues
         */
        @Suppress(DEPRECATION)
        return if (Build.VERSION.SDK_INT >= 24) Html.fromHtml(str, Html.FROM_HTML_MODE_LEGACY) else Html.fromHtml(str)
    }
}