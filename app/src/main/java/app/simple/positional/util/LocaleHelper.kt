package app.simple.positional.util

import android.content.res.Resources
import app.simple.positional.model.Locales

object LocaleHelper {

    var autoSystemLanguageString = "Auto" // Use R.string

    val localeList = arrayListOf(
            Locales(autoSystemLanguageString, getSystemLanguageCode()),
            Locales("English", "en"),
            Locales("български", "bg"),
            Locales("हिन्दी", "hi"),
            Locales("اردو", "ur"))

    fun getSystemLanguageCode(): String {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales[0].language
        } else {
            @Suppress("deprecation") //Required for API < 24
            Resources.getSystem().configuration.locale.language
        }
    }
}