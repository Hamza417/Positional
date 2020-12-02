package app.simple.positional.preference

import android.content.Context
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import app.simple.positional.constants.*

class MainPreferences {

    fun setLaunchCount(context: Context, value: Int) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt(launchCount, value).apply()
    }

    fun getLaunchCount(context: Context): Int {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt(launchCount, 0)
    }

    fun setScreenOn(context: Context, value: Boolean) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putBoolean(screenOn, value).apply()
    }

    fun isScreenOn(context: Context): Boolean {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getBoolean(screenOn, false)
    }

    /**
     * @param value for storing theme preferences
     * 1 - Light
     * 2 - Dark
     * 3 - System
     * 4 - Day/Night
     */
    fun setTheme(@NonNull context: Context, value: Int) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt(theme, value).apply()
    }

    fun getTheme(context: Context): Int {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt(theme, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    // Day/Night Auto
    fun setDayNight(@NonNull context: Context, @NonNull value: Boolean) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putBoolean(dayNightMode, value).apply()
    }

    fun isDayNightOn(context: Context): Boolean {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getBoolean(dayNightMode, false)
    }

    fun setShowPermissionDialog(context: Context, value: Boolean) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putBoolean(showAgain, value).apply()
    }

    fun getShowPermissionDialog(context: Context): Boolean {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getBoolean(showAgain, true)
    }

    fun setShowPlayServiceDialog(context: Context, value: Boolean) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putBoolean(showPlayServicesAgain, value).apply()
    }

    fun getShowPlayServiceDialog(context: Context): Boolean {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getBoolean(showPlayServicesAgain, true)
    }

    fun setLicenseStatus(context: Context, value: Boolean) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putBoolean(licenseStatus, value).apply()
    }

    fun getLicenceStatus(context: Context): Boolean {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getBoolean(licenseStatus, false)
    }

    fun setUnit(@NonNull context: Context, @NonNull value: Boolean) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putBoolean(unit, value).apply()
    }

    fun getUnit(context: Context): Boolean {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getBoolean(unit, true)
    }

    fun setNotifications(@NonNull context: Context, @NonNull value: Boolean) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putBoolean(notifications, value).apply()
    }

    fun isNotificationOn(context: Context): Boolean {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getBoolean(notifications, true)
    }

    // Coordinates
    fun setCustomCoordinates(context: Context, value: Boolean) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putBoolean(isCustomCoordinate, value).apply()
    }

    fun isCustomCoordinate(context: Context): Boolean {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getBoolean(isCustomCoordinate, false)
    }

    fun setLatitude(@NonNull context: Context, value: Float) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putFloat(latitude, value).apply()
    }

    fun setLongitude(@NonNull context: Context, value: Float) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putFloat(longitude, value).apply()
    }

    fun getCoordinates(context: Context): Array<Float> {
        return arrayOf(
                context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getFloat(latitude, 0f),
                context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getFloat(longitude, 0f)
        )
    }
}