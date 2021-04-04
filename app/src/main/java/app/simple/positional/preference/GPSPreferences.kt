package app.simple.positional.preference

import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import org.jetbrains.annotations.NotNull

object GPSPreferences {

    private const val lastLatitude = "last_latitude"
    private const val lastLongitude = "last_longitude"
    private const val mapZoom = "map_zoom_value"
    private const val mapTilt = "map_tilt_value"
    private const val useSmallerIcon = "use_smaller_icon"
    const val mapAutoCenter = "auto_center_map"
    const val GPSLabelMode = "gps_label_mode"
    const val GPSSatellite = "gps_satellite_mode"
    const val highContrastMap = "high_contrast_map"
    const val showBuilding = "show_buildings_on_map"
    const val useVolumeKeys = "use_volume_keys_to_zoom"

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

    fun setMapZoom(@NotNull value: Float) {
        getSharedPreferences().edit().putFloat(mapZoom, value).apply()
    }

    fun getMapZoom(): Float {
        return getSharedPreferences().getFloat(mapZoom, 15F)
    }

    fun setMapTilt(@NotNull value: Float) {
        getSharedPreferences().edit().putFloat(mapTilt, value).apply()
    }

    fun getMapTilt(): Float {
        return getSharedPreferences().getFloat(mapTilt, 15F)
    }

    fun setMapAutoCenter(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(mapAutoCenter, boolean).apply()
    }

    fun getMapAutoCenter(): Boolean {
        return getSharedPreferences().getBoolean(mapAutoCenter, true)
    }

    fun setUseVolumeKeys(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(useVolumeKeys, boolean).apply()
    }

    fun isUsingVolumeKeys(): Boolean {
        return getSharedPreferences().getBoolean(useVolumeKeys, false)
    }

    fun setUseSmallerIcon(value: Boolean) {
        getSharedPreferences().edit().putBoolean(useSmallerIcon, value).apply()
    }

    fun isUsingSmallerIcon(): Boolean {
        return getSharedPreferences().getBoolean(useSmallerIcon, false)
    }
}