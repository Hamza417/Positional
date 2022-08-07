package app.simple.positional.preferences

import android.annotation.SuppressLint
import app.simple.positional.singleton.SharedPreferences
import org.jetbrains.annotations.NotNull

object DirectionPreferences {

    const val directionLatitude = "direction_target_latitude"
    const val directionLongitude = "direction_target_longitude"
    const val directionGimbalLock = "direction_gimbal_lock"
    const val useMapsTarget = "direction_use_maps_target"

    private const val directionLabel = "direction_target_label"

    //--------------------------------------------------------------------------------------------------//

    @SuppressLint("ApplySharedPref")
    fun setTargetLatitude(@NotNull value: Float): Float {
        SharedPreferences.getSharedPreferences().edit().putFloat(directionLatitude, value).apply()
        return value
    }

    @SuppressLint("ApplySharedPref")
    fun setTargetLongitude(@NotNull value: Float): Float {
        SharedPreferences.getSharedPreferences().edit().putFloat(directionLongitude, value).apply()
        return value
    }

    fun getTargetCoordinates(): FloatArray {
        return floatArrayOf(
                SharedPreferences.getSharedPreferences().getFloat(directionLatitude, 0f),
                SharedPreferences.getSharedPreferences().getFloat(directionLongitude, 0f)
        )
    }

    //--------------------------------------------------------------------------------------------------//

    fun setTargetLabel(value: String): String {
        SharedPreferences.getSharedPreferences().edit().putString(directionLabel, value).apply()
        return value
    }

    fun getTargetLabel(): String? {
        return SharedPreferences.getSharedPreferences().getString(directionLabel, null)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setGimbalLock(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(directionGimbalLock, value).apply()
    }

    fun isGimbalLock(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(directionGimbalLock, false)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setUseMapsTarget(value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(useMapsTarget, value).apply()
    }

    fun isUsingMapsTarget(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(useMapsTarget, false)
    }
}