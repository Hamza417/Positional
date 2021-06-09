package app.simple.positional.preferences

import androidx.annotation.IntRange
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import app.simple.positional.singleton.SharedPreferences.getSharedPreferences
import org.jetbrains.annotations.NotNull

object MainPreferences {

    private const val launchCount = "launch_count"
    private const val dayNightMode = "is_day_night_mode"
    private const val showAgain = "show_permission_dialog_again"
    private const val showPlayServicesAgain = "show_play_services_dialog_again"
    private const val licenseStatus = "license_status"
    private const val address = "specified_address"
    private const val screenOn = "keep_the_screen_on"
    private const val appLanguage = "current_language_locale"
    private const val appCornerRadius = "corner_radius"
    private const val skipSplashScreen = "skip_splash_screen"
    const val isCustomCoordinate = "is_custom_coordinate_set"
    const val locationProvider = "location_provider"
    const val unit = "all_measurement_unit"
    const val theme = "current_theme"
    const val latitude = "custom_latitude"
    const val longitude = "custom_longitude"
    const val accentColor = "app_accent_color"
    const val isOSMPanel = "is_map_panel_osm"

    //--------------------------------------------------------------------------------------------------//

    fun setLaunchCount(value: Int) {
        getSharedPreferences().edit().putInt(launchCount, value).apply()
    }

    fun getLaunchCount(): Int {
        return getSharedPreferences().getInt(launchCount, 0)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setScreenOn(value: Boolean) {
        getSharedPreferences().edit().putBoolean(screenOn, value).apply()
    }

    fun isScreenOn(): Boolean {
        return getSharedPreferences().getBoolean(screenOn, false)
    }

    //--------------------------------------------------------------------------------------------------//

    /**
     * @param value for storing theme preferences
     * 1 - Light
     * 2 - Dark
     * 3 - System
     * 4 - Day/Night
     */
    fun setTheme(value: Int) {
        getSharedPreferences().edit().putInt(theme, value).apply()
    }

    fun getTheme(): Int {
        return getSharedPreferences().getInt(theme, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setDayNight(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(dayNightMode, value).apply()
    }

    fun isDayNightOn(): Boolean {
        return getSharedPreferences().getBoolean(dayNightMode, false)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setShowPermissionDialog(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(showAgain, value).apply()
    }

    fun getShowPermissionDialog(): Boolean {
        return getSharedPreferences().getBoolean(showAgain, true)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setShowPlayServiceDialog(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(showPlayServicesAgain, value).apply()
    }

    fun getShowPlayServiceDialog(): Boolean {
        return getSharedPreferences().getBoolean(showPlayServicesAgain, true)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setLicenseStatus(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(licenseStatus, value).apply()
    }

    fun getLicenceStatus(): Boolean {
        return getSharedPreferences().getBoolean(licenseStatus, false)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setUnit(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(unit, value).apply()
    }

    /**
     * returns true if Metric else false
     * for Imperial
     */
    fun getUnit(): Boolean {
        return getSharedPreferences().getBoolean(unit, true)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setCustomCoordinates(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(isCustomCoordinate, value).apply()
    }

    fun isCustomCoordinate(): Boolean {
        return getSharedPreferences().getBoolean(isCustomCoordinate, false)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setLatitude(@NotNull value: Float) {
        getSharedPreferences().edit().putFloat(latitude, value).apply()
    }

    fun setLongitude(@NotNull value: Float) {
        getSharedPreferences().edit().putFloat(longitude, value).apply()
    }

    fun getCoordinates(): FloatArray {
        return floatArrayOf(
                getSharedPreferences().getFloat(latitude, 0f),
                getSharedPreferences().getFloat(longitude, 0f)
        )
    }

    //--------------------------------------------------------------------------------------------------//

    fun setAddress(@NotNull value: String) {
        getSharedPreferences().edit().putString(address, value).apply()
    }

    fun getAddress(): String {
        return getSharedPreferences().getString(address, "")!!
    }

    //--------------------------------------------------------------------------------------------------//

    fun setAppLanguage(@NonNull locale: String) {
        getSharedPreferences().edit().putString(appLanguage, locale).apply()
    }

    fun getAppLanguage(): String? {
        return getSharedPreferences().getString(appLanguage, "default")
    }

    //--------------------------------------------------------------------------------------------------//

    fun setCornerRadius(@IntRange(from = 25, to = 400) radius: Int) {
        getSharedPreferences().edit().putInt(appCornerRadius, radius / 5).apply()
    }

    fun getCornerRadius(): Int {
        return getSharedPreferences().getInt(appCornerRadius, 30)
    }

    //--------------------------------------------------------------------------------------------------//

    /**
     * @param value - Use "fused" for Fused Location Provider and
     *                "android" for Android Location Provider
     */
    fun setLocationProvider(value: String) {
        getSharedPreferences().edit().putString(locationProvider, value).apply()
    }

    fun getLocationProvider(): String {
        return getSharedPreferences().getString(locationProvider, "android")!!
    }

    //--------------------------------------------------------------------------------------------------//

    fun setSkipSplashScreen(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(skipSplashScreen, value).apply()
    }

    fun getSkipSplashScreen(): Boolean {
        return getSharedPreferences().getBoolean(skipSplashScreen, false)
    }

    // ---------------------------------------------------------------------------------------------------------- //

    fun setAccentColor(int: Int) {
        getSharedPreferences().edit().putInt(accentColor, int).apply()
    }

    fun getAccentColor(): Int {
        return getSharedPreferences().getInt(accentColor, 0)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setMapPanelType(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(isOSMPanel, value).apply()
    }

    fun getMapPanelType(): Boolean {
        return getSharedPreferences().getBoolean(isOSMPanel, false)
    }
}