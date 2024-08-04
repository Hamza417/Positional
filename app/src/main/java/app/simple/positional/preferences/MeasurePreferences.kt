package app.simple.positional.preferences

import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import com.google.android.gms.maps.model.LatLng

object MeasurePreferences {

    const val COMPASS_ROTATION = "compass_rotation"
    const val POLYLINES_WRAPPED = "measure_polylines_wrapped"
    const val MAP_ZOOM = "measure_map_zoom_value"
    const val MAP_TILT = "measure_map_tilt_value"
    const val MAP_BEARING = "measure_map_bearing"
    const val LABEL_MODE = "measure_label_mode"
    const val SHOW_BUILDINGS = "measure_show_buildings_on_map"
    const val SATELLITE_MAP = "measure_map_satellite_mode"
    const val HIGH_CONTRAST_MAP = "measure_map_high_contrast_map"
    const val TOOLS_GRAVITY = "measure_tools_gravity"

    private const val LAST_SELECTED_MEASURE = "last_selected_measure"
    private const val LAST_LATITUDE = "_measure_last_latitude"
    private const val LAST_LONGITUDE = "_measure_last_longitude"

    //--------------------------------------------------------------------------------------------------//

    fun isCompassRotation(): Boolean {
        return getSharedPreferences().getBoolean(COMPASS_ROTATION, false)
    }

    fun setCompassRotation(value: Boolean) {
        getSharedPreferences().edit().putBoolean(COMPASS_ROTATION, value).apply()
    }

    fun invertCompassRotation() {
        setCompassRotation(!isCompassRotation())
    }

    //--------------------------------------------------------------------------------------------------//

    fun arePolylinesWrapped(): Boolean {
        return getSharedPreferences().getBoolean(POLYLINES_WRAPPED, false)
    }

    fun setPolylinesWrapped(value: Boolean) {
        getSharedPreferences().edit().putBoolean(POLYLINES_WRAPPED, value).apply()
    }

    fun invertPolylinesWrapped() {
        setPolylinesWrapped(!arePolylinesWrapped())
    }

    //--------------------------------------------------------------------------------------------------//

    fun setMapZoom(value: Float) {
        getSharedPreferences().edit().putFloat(MAP_ZOOM, value).apply()
    }

    fun getMapZoom(): Float {
        return getSharedPreferences().getFloat(MAP_ZOOM, 15F)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setMapTilt(value: Float) {
        getSharedPreferences().edit().putFloat(MAP_TILT, value).apply()
    }

    fun getMapTilt(): Float {
        return getSharedPreferences().getFloat(MAP_TILT, 0F)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setMapBearing(value: Float) {
        getSharedPreferences().edit().putFloat(MAP_BEARING, value).apply()
    }

    fun getMapBearing(): Float {
        return getSharedPreferences().getFloat(MAP_BEARING, 0F)
    }

    //--------------------------------------------------------------------------------------------------//

    fun isLabelOn(): Boolean {
        return getSharedPreferences().getBoolean(LABEL_MODE, true)
    }

    fun setLabelMode(value: Boolean) {
        getSharedPreferences().edit().putBoolean(LABEL_MODE, value).apply()
    }

    //--------------------------------------------------------------------------------------------------//

    fun setShowBuildingsOnMap(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(SHOW_BUILDINGS, boolean).apply()
    }

    fun getShowBuildingsOnMap(): Boolean {
        return getSharedPreferences().getBoolean(SHOW_BUILDINGS, false)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setSatelliteMode(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(SATELLITE_MAP, boolean).apply()
    }

    fun isSatelliteOn(): Boolean {
        return getSharedPreferences().getBoolean(SATELLITE_MAP, false)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setHighContrastMap(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(HIGH_CONTRAST_MAP, boolean).apply()
    }

    fun getHighContrastMap(): Boolean {
        return getSharedPreferences().getBoolean(HIGH_CONTRAST_MAP, false)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setToolsGravityToLeft(boolean: Boolean) {
        getSharedPreferences().edit().putBoolean(TOOLS_GRAVITY, boolean).apply()
    }

    fun isToolsGravityToLeft(): Boolean {
        return getSharedPreferences().getBoolean(TOOLS_GRAVITY, false)
    }

    fun invertToolsGravity() {
        setToolsGravityToLeft(!isToolsGravityToLeft())
    }

    //--------------------------------------------------------------------------------------------------//

    fun setLastSelectedMeasure(id: Long) {
        getSharedPreferences().edit().putLong(LAST_SELECTED_MEASURE, id).apply()
    }

    fun getLastSelectedMeasure(): Long {
        return getSharedPreferences().getLong(LAST_SELECTED_MEASURE, -1)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setLastLatitude(latitude: Double) {
        getSharedPreferences().edit().putFloat(LAST_LATITUDE, latitude.toFloat()).apply()
    }

    fun getLastLatitude(): Double {
        return getSharedPreferences().getFloat(LAST_LATITUDE, 0F).toDouble()
    }

    fun setLastLongitude(longitude: Double) {
        getSharedPreferences().edit().putFloat(LAST_LONGITUDE, longitude.toFloat()).apply()
    }

    fun getLastLongitude(): Double {
        return getSharedPreferences().getFloat(LAST_LONGITUDE, 0F).toDouble()
    }

    fun getLastLatLng(): LatLng {
        return LatLng(getLastLatitude(), getLastLongitude())
    }

    fun isLocationSet(): Boolean {
        return getLastLatitude() != 0.0 && getLastLongitude() != 0.0
    }
}
