package app.simple.positional.preference

import app.simple.positional.singleton.SharedPreferences
import org.jetbrains.annotations.NotNull

object WidgetPreferences {
    private val showAgain = "show_widget_alert_again"

    fun isWidgetAlertShowAgain(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(showAgain, true)
    }

    fun setWidgetAlertShowAgain(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(showAgain, value).apply()
    }
}