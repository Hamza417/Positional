package app.simple.positional.preference

import android.content.Context
import app.simple.positional.constants.noSensorAlertLevel
import app.simple.positional.constants.preferences

class LevelPreferences {
    fun isNoSensorAlertON(context: Context): Boolean {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getBoolean(noSensorAlertLevel, true)
    }

    fun setNoSensorAlert(value: Boolean, context: Context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putBoolean(noSensorAlertLevel, value).apply()
    }
}