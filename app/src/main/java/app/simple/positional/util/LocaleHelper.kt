package app.simple.positional.util

import android.content.res.Resources
import android.view.View
import app.simple.positional.model.Locales
import java.util.Locale

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
            Locales("اردو", "ur"),
            Locales("Português (Brasil)", "pt-BR"),
            Locales("繁體中文 (Traditional Chinese)", "zh-TW"),
            Locales("Deutsch (German)", "de-DE"),
            Locales("Hungarian (Magyar)", "hu-HU"),
            Locales("Spanish (Español)", "es-ES"),
        Locales("Malayalam (മലയാളം)", "ml-IN"),
    )

    fun isOneOfTraditionalChinese(): Boolean {
        return with(getSystemLanguageCode()) {
            this == "zh" ||
                    this == "zh-HK" ||
                    this == "zh-MO" ||
                    this == "zh-TW" ||
                    this == "zh-Hant" ||
                    this == "zh-Hant-HK" ||
                    this == "zh-Hant-MO" ||
                    this == "zh-Hant-TW" ||
                    this == "zh-Hant-CN" ||
                    this == "zh-Hant-SG"
        }
    }

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
