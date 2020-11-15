package app.simple.positional.preference

import android.content.Context
import app.simple.positional.constants.*

class CompassPreference {
    // Parallax
    fun setDirectionCode(value: Boolean, context: Context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putBoolean(direction_code, value).apply()
    }

    fun getDirectionCode(context: Context): Boolean {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getBoolean(direction_code, true)
    }

    // Flower Bloom
    fun setFlowerBloom(value: Boolean, context: Context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putBoolean(flowerBloom, value).apply()
    }

    fun isFlowerBloom(context: Context): Boolean {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getBoolean(flowerBloom, false)
    }

    // Flower Bloom Theme
    fun setFlowerBloom(value: Int, context: Context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt(flowerBloomTheme, value).apply()
    }

    fun getFlowerBloomTheme(context: Context): Int {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt(flowerBloomTheme, 0)
    }

    // Compass Sensor Speed
    fun setCompassSpeed(value: Float, context: Context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putFloat(compassSpeed, value).apply()
    }

    fun getCompassSpeed(context: Context): Float {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getFloat(compassSpeed, 0.06f)
    }

}