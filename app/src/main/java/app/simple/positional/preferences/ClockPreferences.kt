package app.simple.positional.preferences

import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import java.util.Calendar

/**
 * Only preferences related to Clock panel
 */
object ClockPreferences {

    private const val isUsingSecondsPrecision = "is_using_seconds_precision"
    private const val isUsingAmPm = "is_clock_time_type_am_pm"
    const val clockNeedleMovementType = "clock_needle_movement_type_updated"
    const val clockNeedle = "clock_needle_res_value"
    const val timezone = "custom_timezone"
    const val is24HourFace = "is_clock_face_24_hour"

    //--------------------------------------------------------------------------------------------------//

    fun setDefaultClockTime(value: Boolean) {
        getSharedPreferences().edit().putBoolean(isUsingAmPm, value).apply()
    }

    fun getDefaultClockTimeFormat(): Boolean {
        return getSharedPreferences().getBoolean(isUsingAmPm, true)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setClockNeedleTheme(value: Int) {
        getSharedPreferences().edit().putInt(clockNeedle, value).apply()
    }

    fun getClockNeedleTheme(): Int {
        return getSharedPreferences().getInt(clockNeedle, 0)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setMovementType(value: String) {
        getSharedPreferences().edit().putString(clockNeedleMovementType, value).apply()
    }

    fun getMovementType(): String {
        return getSharedPreferences().getString(clockNeedleMovementType, "smooth")!!
    }

    //--------------------------------------------------------------------------------------------------//

    fun setTimeZone(value: String) {
        getSharedPreferences().edit().putString(timezone, value).apply()
    }

    fun getTimeZone(): String {
        return getSharedPreferences().getString(timezone, Calendar.getInstance().timeZone.id)!!
    }

    //--------------------------------------------------------------------------------------------------//

    fun setUseSecondsPrecision(value: Boolean) {
        getSharedPreferences().edit().putBoolean(isUsingSecondsPrecision, value).apply()
    }

    fun isUsingSecondsPrecision(): Boolean {
        return getSharedPreferences().getBoolean(isUsingSecondsPrecision, true)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setClockFaceType(value: Boolean) {
        getSharedPreferences().edit().putBoolean(is24HourFace, value).apply()
    }

    fun isClockFace24Hour(): Boolean {
        return getSharedPreferences().getBoolean(is24HourFace, false)
    }
}