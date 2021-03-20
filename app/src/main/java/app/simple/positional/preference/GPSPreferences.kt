package app.simple.positional.preference

import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import org.jetbrains.annotations.NotNull

object GPSPreferences {

    const val GPSLabelMode = "gps_label_mode"
    const val GPSSatellite = "gps_satellite_mode"
    private const val lastLatitude = "last_latitude"
    private const val lastLongitude = "last_longitude"
    const val highContrastMap = "high_contrast_map"
    const val showBuilding = "show_buildings_on_map"

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

    fun setHighContrastMap(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(highContrastMap, boolean).apply()
    }

    fun getHighContrastMap(): Boolean {
        return getSharedPreferences().getBoolean(highContrastMap, false)
    }

    fun setShowBuildingsOnMap(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(showBuilding, boolean).apply()
    }

    fun getShowBuildingsOnMap(): Boolean {
        return getSharedPreferences().getBoolean(showBuilding, false)
    }
}