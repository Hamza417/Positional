package app.simple.positional.preference

import android.content.Context
import androidx.annotation.IntRange
import androidx.annotation.NonNull
import app.simple.positional.constants.*

class MainPreferences {
    /**
     * @param value for storing theme preferences
     * 1 - Light
     * 2 - Dark
     * 3 - System
     * 4 - Day/Night
     */
    fun setCurrentTheme(@NonNull context: Context, @NonNull @IntRange(from = 1, to = 4) value: Int) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt(currentTheme, value).apply()
    }

    fun getCurrentTheme(context: Context): Int {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt(currentTheme, 1)
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
}