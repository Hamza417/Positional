package app.simple.positional.preferences

import app.simple.positional.singleton.SharedPreferences
import org.jetbrains.annotations.NotNull

object BottomBarPreferences {

    const val clockPanel = "clock_panel"
    const val compassPanel = "compass_panel"
    const val gpsPanel = "gps_panel"
    const val trailPanel = "trail_panel"
    const val levelPanel = "level_panel"
    const val settingsPanel = "settings_panel"

    //--------------------------------------------------------------------------------------------------//

    fun setClockPanelVisibility(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(clockPanel, value).apply()
    }

    fun getClockPanelVisibility(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(clockPanel, true)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setCompassPanelVisibility(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(compassPanel, value).apply()
    }

    fun getCompassPanelVisibility(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(compassPanel, true)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setGpsPanelVisibility(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(gpsPanel, value).apply()
    }

    fun getGpsPanelVisibility(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(gpsPanel, true)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setTrailPanelVisibility(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(trailPanel, value).apply()
    }

    fun getTrailPanelVisibility(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(trailPanel, true)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setLevelPanelVisibility(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(levelPanel, value).apply()
    }

    fun getLevelPanelVisibility(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(levelPanel, true)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setSettingsPanelVisibility(@NotNull value: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(settingsPanel, value).apply()
    }

    fun getSettingsPanelVisibility(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(settingsPanel, true)
    }
}
