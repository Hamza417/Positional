package app.simple.positional.preferences

import android.annotation.SuppressLint
import app.simple.positional.singleton.SharedPreferences
import org.jetbrains.annotations.NotNull

object DirectionPreferences {

    const val directionLatitude = "direct_target_latitude"
    const val directionLongitude = "direction_target_longitude"
    const val directionGimbalLock = "direction_gimbal_lock"

    private const val directionLabel = "direction_target_label"

    //--------------------------------------------------------------------------------------------------//

    @SuppressLint("ApplySharedPref")
    fun setTargetLatitude(@NotNull value: Float) {
        SharedPreferences.getSharedPreferences().edit().putFloat(directionLatitude, value).commit()
    }

    @SuppressLint("ApplySharedPref")
    fun setTargetLongitude(@NotNull value: Float) {
        SharedPreferences.getSharedPreferences().edit().putFloat(directionLongitude, value).commit()
    }

    fun getTargetCoordinates(): FloatArray {
        return floatArrayOf(
                SharedPreferences.getSharedPreferences().getFloat(directionLatitude, 0f),
                SharedPreferences.getSharedPreferences().getFloat(directionLongitude, 0f)
        )
    }

    //--------------------------------------------------------------------------------------------------//

    fun setTargetLabel(value: String) {
        SharedPreferences.getSharedPreferences().edit().putString(directionLabel, value).apply()
    }

    fun getTargetLabel(): String? {
        return SharedPreferences.getSharedPreferences().getString(directionLabel, null)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setGimbalLock(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(directionGimbalLock, value).apply()
    }

    fun isGimbalLock(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(directionGimbalLock, true)
    }
}