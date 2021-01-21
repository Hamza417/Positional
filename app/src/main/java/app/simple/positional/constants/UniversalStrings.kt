package app.simple.positional.constants

import android.content.Context
import app.simple.positional.R

object UniversalStrings {

    var autoSystemLanguageString = "Auto"

    fun init(context: Context) {
        autoSystemLanguageString = context.getString(R.string.auto_system_default_language)
    }
}