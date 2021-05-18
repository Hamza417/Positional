package app.simple.positional.util

import android.content.res.Resources
import app.simple.positional.model.Locales
import java.util.*

object LocaleHelper {

    private var appLocale = Locale.getDefault()

    /**
     * List of languages currently supported by
     * the app
     */
    val localeList = arrayListOf(
            Locales("autoSystemLanguageString" /* Placeholder */, "default"),
            Locales("English", "en"),
            Locales("български", "bg"),
            Locales("Deutsch", "de"),
            Locales("हिन्दी", "hi"),
            Locales("اردو", "ur"))

    fun getSystemLanguageCode(): String {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales[0].language
        } else {
            @Suppress("deprecation")
            Resources.getSystem().configuration.locale.language
        }
    }

    fun getAppLocale(): Locale {
        return synchronized(this) {
            appLocale
        }
    }

    fun setAppLocale(value: Locale) {
        synchronized(this) {
            appLocale = value
        }
    }
}
