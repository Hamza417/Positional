package app.simple.positional.preference

import app.simple.positional.preference.SharedPreferences.getSharedPreferences
import org.jetbrains.annotations.NotNull

object LevelPreferences {

    private const val noSensorAlertLevel = "no_sensor_alert_level_dialog_show"

    fun isNoSensorAlertON(): Boolean {
        return getSharedPreferences().getBoolean(noSensorAlertLevel, true)
    }

    fun setNoSensorAlert(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(noSensorAlertLevel, value).apply()
    }
}