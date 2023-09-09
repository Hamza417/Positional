package app.simple.positional.preferences

import app.simple.positional.decorations.views.PhysicalRotationImageView
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences

/**
 * Only preference related to Compass
 */
object CompassPreferences {

    const val direction_code = "direction_code"
    const val flowerBloom = "flower"
    const val flowerBloomTheme = "flower_theme"
    const val dampingCoefficient = "damping_coefficient"
    const val magneticCoefficient = "magnetic_coefficient"
    const val rotationalInertia = "rotational_inertia"
    const val usePhysicalProperties = "use_physical_properties"
    const val useGimbalLock = "use_gimbal_lock"

    //--------------------------------------------------------------------------------------------------//

    fun setDirectionCode(value: Boolean) {
        getSharedPreferences().edit().putBoolean(direction_code, value).apply()
    }

    fun getDirectionCode(): Boolean {
        return getSharedPreferences().getBoolean(direction_code, true)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setFlowerBloom(value: Boolean) {
        getSharedPreferences().edit().putBoolean(flowerBloom, value).apply()
    }

    fun isFlowerBloomOn(): Boolean {
        return getSharedPreferences().getBoolean(flowerBloom, false)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setFlowerBloom(value: Int) {
        getSharedPreferences().edit().putInt(flowerBloomTheme, value).apply()
    }

    fun getFlowerBloomTheme(): Int {
        return getSharedPreferences().getInt(flowerBloomTheme, 0)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setDampingCoefficient(value: Float) {
        getSharedPreferences().edit().putFloat(dampingCoefficient, value).apply()
    }

    fun getDampingCoefficient(): Float {
        return getSharedPreferences().getFloat(dampingCoefficient, PhysicalRotationImageView.ALPHA_DEFAULT)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setRotationalInertia(value: Float) {
        getSharedPreferences().edit().putFloat(rotationalInertia, value).apply()
    }

    fun getRotationalInertia(): Float {
        return getSharedPreferences().getFloat(rotationalInertia, PhysicalRotationImageView.INERTIA_MOMENT_DEFAULT)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setMagneticCoefficient(value: Float) {
        getSharedPreferences().edit().putFloat(magneticCoefficient, value).apply()
    }

    fun getMagneticCoefficient(): Float {
        return getSharedPreferences().getFloat(magneticCoefficient, PhysicalRotationImageView.MB_DEFAULT)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setUsePhysicalProperties(value: Boolean) {
        getSharedPreferences().edit().putBoolean(usePhysicalProperties, value).apply()
    }

    fun isUsingPhysicalProperties(): Boolean {
        return getSharedPreferences().getBoolean(usePhysicalProperties, true)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setUseGimbalLock(value: Boolean) {
        getSharedPreferences().edit().putBoolean(useGimbalLock, value).apply()
    }

    fun isUsingGimbalLock(): Boolean {
        return getSharedPreferences().getBoolean(useGimbalLock, false)
    }
}
