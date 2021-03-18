package app.simple.positional.preference

import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import org.jetbrains.annotations.NotNull

object ClockPreferences {

    private const val clockDefaultTimeFormat = "is_clock_time_type_am_pm"
    const val clockNeedleMovementType = "clock_needle_movement_type"
    const val clockNeedle = "clock_needle_res_value"

    fun setDefaultClockTime(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(clockDefaultTimeFormat, value).apply()
    }

    fun getDefaultClockTime(): Boolean {
        return getSharedPreferences().getBoolean(clockDefaultTimeFormat, true)
    }

    fun setClockNeedleTheme(@NotNull value: Int) {
        getSharedPreferences().edit().putInt(clockNeedle, value).apply()
    }

    fun getClockNeedleTheme(): Int {
        return getSharedPreferences().getInt(clockNeedle, 1)
    }

    fun setMovementType(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(clockNeedleMovementType, value).apply()
    }

    fun getMovementType(): Boolean {
        return getSharedPreferences().getBoolean(clockNeedleMovementType, true)
    }
}