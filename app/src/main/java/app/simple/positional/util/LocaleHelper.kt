package app.simple.positional.util

import android.content.res.Resources
import android.view.View
import app.simple.positional.model.Locales
import java.util.*

object LocaleHelper {

    private var appLocale = Locale.getDefault()

    /**
     * List of languages currently supported by
     * the app.
     *
     * Do not include incomplete translations.
     */
    val localeList = arrayListOf(
            Locales("autoSystemLanguageString" /* Placeholder */, "default"),
            Locales("English (US)", "en"),
            Locales("български", "bg"),
            Locales("Français", "fr"),
            Locales("Čeština", "cs"),
            Locales("हिन्दी", "hi"),
            Locales("Română", "ro"),
            Locales("Русский", "ru"),
            Locales("اردو", "ur"))

    fun getSystemLanguageCode(): String {
        return Resources.getSystem().configuration.locales[0].language
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

    fun Resources.isRTL(): Boolean {
        return configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
    }
}
