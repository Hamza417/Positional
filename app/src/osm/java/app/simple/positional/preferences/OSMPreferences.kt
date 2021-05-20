package app.simple.positional.preferences

import app.simple.positional.preference.ClockPreferences
import app.simple.positional.singleton.SharedPreferences
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import org.jetbrains.annotations.NotNull

object OSMPreferences {

    const val mapTileProvider = "map_tiles_provider"

    //--------------------------------------------------------------------------------------------------//

    fun setMapTileProvider(@NotNull value: String) {
        getSharedPreferences().edit().putString(mapTileProvider, value).apply()
    }

    fun getMapTileProvider(): String {
        return getSharedPreferences().getString(mapTileProvider, "DEFAULT_TILE_SOURCE")!!
    }
}