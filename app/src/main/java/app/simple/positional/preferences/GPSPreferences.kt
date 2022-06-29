package app.simple.positional.preferences

import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import org.jetbrains.annotations.NotNull

/**
 * Only preferences related to GPS panel
 */
object GPSPreferences {

    private const val mapZoom = "map_zoom_value"
    private const val mapTilt = "map_tilt_value"
    private const val mapBearing = "map_bearing_value"
    private const val mapTargetMarkerLatitude = "target_marker_latitude"
    private const val mapTargetMarkerLongitude = "target_marker_longitude"
    private const val mapTargetMarkerStartLatitude = "target_start_latitude"
    private const val mapTargetMarkerStartLongitude = "target_start_longitude"

    const val pinSize = "map_pin_size"
    const val pinOpacity = "pin_opacity"
    const val useBearingRotation = "use_bearing_rotation"
    const val GPSLabelMode = "gps_label_mode"
    const val GPSSatellite = "gps_satellite_mode"
    const val highContrastMap = "high_contrast_map"
    const val showBuilding = "show_buildings_on_map"
    const val useVolumeKeys = "use_volume_keys_to_zoom"
    const val pinSkin = "current_pin_skin"
    const val compassRotation = "is_location_map_compass_rotation"
    const val toolsGravity = "is_tools_gravity_left"
    const val isNorthOnly = "is_map_north_only"
    const val mapTargetMarker = "target_marker_state"
    const val isTargetMarkerMode = "target_marker_mode"
    const val trafficMode = "traffic_on_maps"

    //--------------------------------------------------------------------------------------------------//

    fun isLabelOn(): Boolean {
        return getSharedPreferences().getBoolean(GPSLabelMode, true)
    }

    fun setLabelMode(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(GPSLabelMode, value).apply()
    }

    //--------------------------------------------------------------------------------------------------//

    fun isToolsGravityLeft(): Boolean {
        return getSharedPreferences().getBoolean(toolsGravity, false)
    }

    fun setToolsGravity(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(toolsGravity, value).apply()
    }

    //--------------------------------------------------------------------------------------------------//

    fun isCompassRotation(): Boolean {
        return getSharedPreferences().getBoolean(compassRotation, false)
    }

    fun setCompassRotation(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(compassRotation, value).apply()
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

    fun setMapBearing(@NotNull value: Float) {
        getSharedPreferences().edit().putFloat(mapBearing, value).apply()
    }

    fun getMapBearing(): Float {
        return getSharedPreferences().getFloat(mapBearing, 15F)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setUseVolumeKeys(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(useVolumeKeys, boolean).apply()
    }

    fun isUsingVolumeKeys(): Boolean {
        return getSharedPreferences().getBoolean(useVolumeKeys, false)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setBearingRotation(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(useBearingRotation, boolean).apply()
    }

    fun isBearingRotation(): Boolean {
        return getSharedPreferences().getBoolean(useBearingRotation, false)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setNorthOnly(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(isNorthOnly, boolean).apply()
    }

    fun isNorthOnly(): Boolean {
        return getSharedPreferences().getBoolean(isNorthOnly, true)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setPinSize(value: Int) {
        getSharedPreferences().edit().putInt(pinSize, value).apply()
    }

    fun getPinSize(): Int {
        return getSharedPreferences().getInt(pinSize, 4)
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

    //--------------------------------------------------------------------------------------------------//

    fun setTargetMarkerLatitude(@NotNull value: Float) {
        getSharedPreferences().edit().putFloat(mapTargetMarkerLatitude, value).apply()
    }

    fun setTargetMarkerLongitude(@NotNull value: Float) {
        getSharedPreferences().edit().putFloat(mapTargetMarkerLongitude, value).apply()
    }

    fun getTargetMarkerCoordinates(): FloatArray {
        return floatArrayOf(
                getSharedPreferences().getFloat(mapTargetMarkerLatitude, 48.8584f),
                getSharedPreferences().getFloat(mapTargetMarkerLongitude, 2.2945f)
        )
    }

    fun setTargetMarkerStartLatitude(@NotNull value: Float) {
        getSharedPreferences().edit().putFloat(mapTargetMarkerStartLatitude, value).apply()
    }

    fun setTargetMarkerStartLongitude(@NotNull value: Float) {
        getSharedPreferences().edit().putFloat(mapTargetMarkerStartLongitude, value).apply()
    }

    fun getTargetMarkerStartCoordinates(): FloatArray {
        return floatArrayOf(
                getSharedPreferences().getFloat(mapTargetMarkerStartLatitude, 48.8584f),
                getSharedPreferences().getFloat(mapTargetMarkerStartLongitude, 2.2945f)
        )
    }

    //--------------------------------------------------------------------------------------------------//

    fun setTargetMarker(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(mapTargetMarker, boolean).apply()
    }

    fun isTargetMarkerSet(): Boolean {
        return getSharedPreferences().getBoolean(mapTargetMarker, false)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setTargetMarkerMode(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(isTargetMarkerMode, boolean).apply()
    }

    fun isTargetMarkerMode(): Boolean {
        return getSharedPreferences().getBoolean(isTargetMarkerMode, false)
    }

    //--------------------------------------------------------------------------------------------------//

    fun isTrafficShown(): Boolean {
        return getSharedPreferences().getBoolean(trafficMode, false)
    }

    fun setTrafficMode(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(trafficMode, value).apply()
    }
}