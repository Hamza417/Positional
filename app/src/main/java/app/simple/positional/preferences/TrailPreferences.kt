package app.simple.positional.preferences

import app.simple.positional.singleton.SharedPreferences
import org.jetbrains.annotations.NotNull

object TrailPreferences {

    private const val mapZoom = "trail_map_zoom_value"
    private const val mapTilt = "trail_map_tilt_value"
    const val wrapped = "are_polylines_wrapped"
    const val geodesic = "is_trail_geodesic"
    const val trailLabelMode = "trail_map_label_mode"
    const val trailSatellite = "trail_map_satellite_mode"
    const val trailHighContrastMap = "trail_map_high_contrast_map"
    const val trailShowBuilding = "trail_show_buildings_on_map"

    //--------------------------------------------------------------------------------------------------//

    fun setGeodesic(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(geodesic, value).apply()
    }

    fun isTrailGeodesic(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(geodesic, true)
    }

    //--------------------------------------------------------------------------------------------------//

    fun isLabelOn(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(trailLabelMode, true)
    }

    fun setLabelMode(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(trailLabelMode, value).apply()
    }

    //--------------------------------------------------------------------------------------------------//

    fun isSatelliteOn(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(trailSatellite, false)
    }

    fun setSatelliteMode(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(trailSatellite, value).apply()
    }

    //--------------------------------------------------------------------------------------------------//

    fun setHighContrastMap(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(trailHighContrastMap, boolean).apply()
    }

    fun getHighContrastMap(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(trailHighContrastMap, false)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setShowBuildingsOnMap(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(trailShowBuilding, boolean).apply()
    }

    fun getShowBuildingsOnMap(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(trailShowBuilding, false)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setMapZoom(@NotNull value: Float) {
        SharedPreferences.getSharedPreferences().edit().putFloat(mapZoom, value).apply()
    }

    fun getMapZoom(): Float {
        return SharedPreferences.getSharedPreferences().getFloat(mapZoom, 15F)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setMapTilt(@NotNull value: Float) {
        SharedPreferences.getSharedPreferences().edit().putFloat(mapTilt, value).apply()
    }

    fun getMapTilt(): Float {
        return SharedPreferences.getSharedPreferences().getFloat(mapTilt, 15F)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setWrapStatus(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(wrapped, value).apply()
    }

    fun arePolylinesWrapped(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(wrapped, true)
    }
}