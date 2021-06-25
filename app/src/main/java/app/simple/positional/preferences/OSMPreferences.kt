package app.simple.positional.preferences

import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import org.jetbrains.annotations.NotNull

object OSMPreferences {

    private const val osmMapZoom = "osm_map_zoom_value"
    const val useAlternativeColorFilter = "is_osm_using_alternate_color_filter"
    const val mapTileProvider = "map_tiles_provider"

    //--------------------------------------------------------------------------------------------------//

    fun setMapTileProvider(@NotNull value: String) {
        getSharedPreferences().edit().putString(mapTileProvider, value).apply()
    }

    fun getMapTileProvider(): String {
        return getSharedPreferences().getString(mapTileProvider, "DEFAULT_TILE_SOURCE")!!
    }

    //--------------------------------------------------------------------------------------------------//

    fun setOSMMapZoom(@NotNull value: Float) {
        getSharedPreferences().edit().putFloat(osmMapZoom, value).apply()
    }

    fun getOSMMapZoom(): Float {
        return getSharedPreferences().getFloat(osmMapZoom, 15F)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setAlternateColorFilter(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(useAlternativeColorFilter, value).apply()
    }

    fun isAlternateColorFilter(): Boolean {
        return getSharedPreferences().getBoolean(useAlternativeColorFilter, false)
    }
}