package app.simple.positional.preferences

import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import org.jetbrains.annotations.NotNull

object LevelPreferences {

    private const val noSensorAlertLevel = "no_sensor_alert_level_dialog_show"
    const val isSquareStyle = "is_square_style"

    fun isNoSensorAlertON(): Boolean {
        return getSharedPreferences().getBoolean(noSensorAlertLevel, true)
    }

    fun setNoSensorAlert(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(noSensorAlertLevel, value).apply()
    }

    //--------------------------------------------------------------------------------------------------//

    fun isSquareStyle(): Boolean {
        return getSharedPreferences().getBoolean(isSquareStyle, false)
    }

    fun setSquareStyle(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(isSquareStyle, value).apply()
    }
}