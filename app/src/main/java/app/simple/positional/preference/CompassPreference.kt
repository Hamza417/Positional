package app.simple.positional.preference

import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import org.jetbrains.annotations.NotNull

object CompassPreference {

    private const val direction_code = "direction_code"
    private const val flowerBloom = "flower"
    private const val flowerBloomTheme = "flower_theme"
    private const val compassSpeed = "compass_speed"
    private const val noSensorAlertCompass = "no_sensor_alert_compass_dialog_show"

    // Parallax
    fun setDirectionCode(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(direction_code, value).apply()
    }

    fun getDirectionCode(): Boolean {
        return getSharedPreferences().getBoolean(direction_code, true)
    }

    // Flower Bloom
    fun setFlowerBloom(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(flowerBloom, value).apply()
    }

    fun isFlowerBloom(): Boolean {
        return getSharedPreferences().getBoolean(flowerBloom, false)
    }

    // Flower Bloom Theme
    fun setFlowerBloom(@NotNull value: Int) {
        getSharedPreferences().edit().putInt(flowerBloomTheme, value).apply()
    }

    fun getFlowerBloomTheme(): Int {
        return getSharedPreferences().getInt(flowerBloomTheme, 0)
    }

    // Compass Sensor Speed
    fun setCompassSpeed(@NotNull value: Float) {
        getSharedPreferences().edit().putFloat(compassSpeed, value).apply()
    }

    fun getCompassSpeed(): Float {
        return getSharedPreferences().getFloat(compassSpeed, 0.06f)
    }

    fun isNoSensorAlertON(): Boolean {
        return getSharedPreferences().getBoolean(noSensorAlertCompass, true)
    }

    fun setNoSensorAlert(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(noSensorAlertCompass, value).apply()
    }
}