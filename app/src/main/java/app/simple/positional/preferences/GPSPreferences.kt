package app.simple.positional.preferences

import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import org.jetbrains.annotations.NotNull

/**
 * Only preferences related to GPS panel
 */
object GPSPreferences {

    private const val mapZoom = "map_zoom_value"
    private const val mapTilt = "map_tilt_value"
    const val pinSize = "pin_size"
    const val pinOpacity = "pin_opacity"
    const val useBearingRotation = "use_bearing_rotation"
    const val mapAutoCenter = "auto_center_map"
    const val GPSLabelMode = "gps_label_mode"
    const val GPSSatellite = "gps_satellite_mode"
    const val highContrastMap = "high_contrast_map"
    const val showBuilding = "show_buildings_on_map"
    const val useVolumeKeys = "use_volume_keys_to_zoom"
    const val pinSkin = "current_pin_skin"
    const val compass = "is_location_map_compass_rotation"

    //--------------------------------------------------------------------------------------------------//

    fun isLabelOn(): Boolean {
        return getSharedPreferences().getBoolean(GPSLabelMode, true)
    }

    fun setLabelMode(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(GPSLabelMode, value).apply()
    }

    //--------------------------------------------------------------------------------------------------//

    fun isCompassRotation(): Boolean {
        return getSharedPreferences().getBoolean(compass, true)
    }

    fun setCompassRotation(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(compass, value).apply()
    }

    //--------------------------------------------------------------------------------------------------//

    fun isSatelliteOn(): Boolean {
        return getSharedPreferences().getBoolean(GPSSatellite, false)
    }

    fun setSatelliteMode(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(GPSSatellite, value).apply()
    }

    //--------------------------------------------------------------------------------------------------//

    fun setHighContrastMap(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(highContrastMap, boolean).apply()
    }

    fun getHighContrastMap(): Boolean {
        return getSharedPreferences().getBoolean(highContrastMap, false)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setShowBuildingsOnMap(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(showBuilding, boolean).apply()
    }

    fun getShowBuildingsOnMap(): Boolean {
        return getSharedPreferences().getBoolean(showBuilding, false)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setMapZoom(@NotNull value: Float) {
        getSharedPreferences().edit().putFloat(mapZoom, value).apply()
    }

    fun getMapZoom(): Float {
        return getSharedPreferences().getFloat(mapZoom, 15F)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setMapTilt(@NotNull value: Float) {
        getSharedPreferences().edit().putFloat(mapTilt, value).apply()
    }

    fun getMapTilt(): Float {
        return getSharedPreferences().getFloat(mapTilt, 15F)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setMapAutoCenter(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(mapAutoCenter, boolean).apply()
    }

    fun getMapAutoCenter(): Boolean {
        return getSharedPreferences().getBoolean(mapAutoCenter, true)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setUseVolumeKeys(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(useVolumeKeys, boolean).apply()
    }

    fun isUsingVolumeKeys(): Boolean {
        return getSharedPreferences().getBoolean(useVolumeKeys, false)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setUseBearingRotation(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(useBearingRotation, boolean).apply()
    }

    fun isBearingRotationOn(): Boolean {
        return getSharedPreferences().getBoolean(useBearingRotation, false)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setPinSize(value: Int) {
        getSharedPreferences().edit().putInt(pinSize, value).apply()
    }

    fun getPinSize(): Int {
        return getSharedPreferences().getInt(pinSize, 400)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setPinOpacity(value: Int) {
        getSharedPreferences().edit().putInt(pinOpacity, value).apply()
    }

    fun getPinOpacity(): Int {
        return getSharedPreferences().getInt(pinOpacity, 255)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setPinSkin(value: Int) {
        getSharedPreferences().edit().putInt(pinSkin, value).apply()
    }

    fun getPinSkin(): Int {
        return getSharedPreferences().getInt(pinSkin, 0)
    }
}