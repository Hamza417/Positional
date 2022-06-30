package app.simple.positional.preferences

import android.annotation.SuppressLint
import app.simple.positional.singleton.SharedPreferences
import org.jetbrains.annotations.NotNull

object DirectionPreferences {

    const val directionLatitude = "direct_target_latitude"
    const val directionLongitude = "direction_target_longitude"

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
}