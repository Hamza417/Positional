package app.simple.positional.preference

import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import org.jetbrains.annotations.NotNull

object CompassPreference {

    const val direction_code = "direction_code"
    const val flowerBloom = "flower"
    const val flowerBloomTheme = "flower_theme"
    const val dampingCoefficient = "damping_coefficient"
    const val magneticCoefficient = "magnetic_coefficient"
    const val rotationalInertia = "rotational_inertia"
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

    fun isFlowerBloomOn(): Boolean {
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
    fun setDampingCoefficient(@NotNull value: Float) {
        getSharedPreferences().edit().putFloat(dampingCoefficient, value).apply()
    }

    fun getDampingCoefficient(): Float {
        return getSharedPreferences().getFloat(dampingCoefficient, 10F)
    }

    fun setRotationalInertia(@NotNull value: Float) {
        getSharedPreferences().edit().putFloat(rotationalInertia, value).apply()
    }

    fun getRotationalInertia(): Float {
        return getSharedPreferences().getFloat(rotationalInertia, 0.1f)
    }

    fun setMagneticCoefficient(@NotNull value: Float) {
        getSharedPreferences().edit().putFloat(magneticCoefficient, value).apply()
    }

    fun getMagneticCoefficient(): Float {
        return getSharedPreferences().getFloat(magneticCoefficient, 1000f)
    }

    fun isNoSensorAlertON(): Boolean {
        return getSharedPreferences().getBoolean(noSensorAlertCompass, true)
    }

    fun setNoSensorAlert(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(noSensorAlertCompass, value).apply()
    }
}
