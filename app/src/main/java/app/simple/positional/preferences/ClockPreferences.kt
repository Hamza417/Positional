package app.simple.positional.preferences

import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import org.jetbrains.annotations.NotNull
import java.util.*

/**
 * Only preferences related to Clock panel
 */
object ClockPreferences {

    private const val clockDefaultTimeFormat = "is_clock_time_type_am_pm"
    private const val timeZoneScrollPosition = "last_selected_timezone_position"
    private const val isUsingSecondsPrecision = "is_using_seconds_precision"
    const val clockNeedleMovementType = "clock_needle_movement_type_updated"
    const val clockNeedle = "clock_needle_res_value"
    const val timezone = "custom_timezone"

    //--------------------------------------------------------------------------------------------------//

    fun setDefaultClockTime(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(clockDefaultTimeFormat, value).apply()
    }

    fun getDefaultClockTime(): Boolean {
        return getSharedPreferences().getBoolean(clockDefaultTimeFormat, true)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setClockNeedleTheme(@NotNull value: Int) {
        getSharedPreferences().edit().putInt(clockNeedle, value).apply()
    }

    fun getClockNeedleTheme(): Int {
        return getSharedPreferences().getInt(clockNeedle, 1)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setMovementType(@NotNull value: String) {
        getSharedPreferences().edit().putString(clockNeedleMovementType, value).apply()
    }

    fun getMovementType(): String {
        return getSharedPreferences().getString(clockNeedleMovementType, "smooth")!!
    }

    //--------------------------------------------------------------------------------------------------//

    fun setTimeZone(@NotNull value: String) {
        getSharedPreferences().edit().putString(timezone, value).apply()
    }

    fun getTimeZone(): String {
        return getSharedPreferences().getString(timezone, Calendar.getInstance().timeZone.id)!!
    }

    //--------------------------------------------------------------------------------------------------//

    fun setTimezoneSelectedPosition(@NotNull value: Int) {
        getSharedPreferences().edit().putInt(timeZoneScrollPosition, value).apply()
    }

    fun getTimezoneSelectedPosition(): Int {
        return getSharedPreferences().getInt(timeZoneScrollPosition, 0)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setUseSecondsPrecision(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(isUsingSecondsPrecision, value).apply()
    }

    fun isUsingSecondsPrecision(): Boolean {
        return getSharedPreferences().getBoolean(isUsingSecondsPrecision, true)
    }
}