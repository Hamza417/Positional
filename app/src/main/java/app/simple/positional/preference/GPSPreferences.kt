package app.simple.positional.preference

import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import org.jetbrains.annotations.NotNull

object GPSPreferences {

    private const val GPSLabelMode = "gps_label_mode"
    private const val GPSSatellite = "gps_satellite_mode"
    private const val lastLatitude = "last_latitude"
    private const val lastLongitude = "last_longitude"

    fun isLabelOn(): Boolean {
        return getSharedPreferences().getBoolean(GPSLabelMode, true)
    }

    fun setLabelMode(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(GPSLabelMode, value).apply()
    }

    fun isSatelliteOn(): Boolean {
        return getSharedPreferences().getBoolean(GPSSatellite, false)
    }

    fun setSatelliteMode(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(GPSSatellite, value).apply()
    }

    // Last Session Coordinates
    fun setLastLatitude(@NotNull value: Float) {
        getSharedPreferences().edit().putFloat(lastLatitude, value).apply()
    }

    fun setLastLongitude(@NotNull value: Float) {
        getSharedPreferences().edit().putFloat(lastLongitude, value).apply()
    }

    fun getLastCoordinates(): FloatArray {
        return floatArrayOf(
                getSharedPreferences().getFloat(lastLatitude, 48.8584f),
                getSharedPreferences().getFloat(lastLongitude, 2.2945f)
        )
    }
}