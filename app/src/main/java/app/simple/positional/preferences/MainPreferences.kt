package app.simple.positional.preferences

import android.annotation.SuppressLint
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
    private const val ratingDialog = "is_showing_rating_dialog"
    private const val wideColor = "is_wide_color_gamut"
    private const val currentArt = "current_art_position"

    const val isCustomCoordinate = "is_custom_coordinate_set"
    const val locationProvider = "location_provider"
    const val unit = "all_measurement_unit"
    const val theme = "current_theme"
    const val latitude = "custom_latitude"
    const val longitude = "custom_longitude"
    const val accentColor = "app_accent_color"
    const val lastLatitude = "last_latitude"
    const val lastLongitude = "last_longitude"

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

    fun setShowRatingDialog(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(ratingDialog, value).apply()
    }

    fun getShowRatingDialog(): Boolean {
        return getSharedPreferences().getBoolean(ratingDialog, true)
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

    @SuppressLint("ApplySharedPref")
    fun setCustomCoordinates(@NotNull value: Boolean) {
        getSharedPreferences().edit().putBoolean(isCustomCoordinate, value).commit()
    }

    fun isCustomCoordinate(): Boolean {
        return getSharedPreferences().getBoolean(isCustomCoordinate, false)
    }

    //--------------------------------------------------------------------------------------------------//

    @SuppressLint("ApplySharedPref")
    fun setLatitude(@NotNull value: Float) {
        getSharedPreferences().edit().putFloat(latitude, value).commit()
    }

    @SuppressLint("ApplySharedPref")
    fun setLongitude(@NotNull value: Float) {
        getSharedPreferences().edit().putFloat(longitude, value).commit()
    }

    fun getCoordinates(): FloatArray {
        return floatArrayOf(
                getSharedPreferences().getFloat(latitude, 0f),
                getSharedPreferences().getFloat(longitude, 0f)
        )
    }
    //--------------------------------------------------------------------------------------------------//
    fun setLastLatitude(@NotNull value: Float) {
        getSharedPreferences().edit().putFloat(lastLatitude, value).apply()
    }

    fun setLastLongitude(@NotNull value: Float) {
        getSharedPreferences().edit().putFloat(lastLongitude, value).apply()
    }

    fun getLastCoordinates(): FloatArray {
        return floatArrayOf(
                getSharedPreferences().getFloat(lastLatitude, 48.8584f),
                getSharedPreferences().getFloat(lastLongitude, 2.2945f)
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

    fun setWideColorGamut(value: Boolean) {
        getSharedPreferences().edit().putBoolean(wideColor, value).apply()
    }

    fun isWideColorGamut(): Boolean {
        return getSharedPreferences().getBoolean(wideColor, false)
    }

    //--------------------------------------------------------------------------------------------------//

    fun setCurrentArt(value: Int) {
        getSharedPreferences().edit().putInt(currentArt, value).apply()
    }

    fun getCurrentArt(): Int {
        return getSharedPreferences().getInt(currentArt, 0)
    }
}