package app.simple.positional.preference

import android.content.Context
import app.simple.positional.constants.compassSpeed
import app.simple.positional.constants.flowerBloom
import app.simple.positional.constants.parallax
import app.simple.positional.constants.preferences

class CompassPreference {
    // Parallax
    fun setParallax(value: Boolean, context: Context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putBoolean(parallax, value).apply()
    }

    fun getParallax(context: Context): Boolean {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getBoolean(parallax, false)
    }

    // Flower Bloom
    fun setFlowerBloom(value: Boolean, context: Context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putBoolean(flowerBloom, value).apply()
    }

    fun isFlowerBloom(context: Context): Boolean {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getBoolean(flowerBloom, false)
    }

    // Flower Bloom
    fun setCompassSpeed(value: Float, context: Context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putFloat(compassSpeed, value).apply()
    }

    fun getCompassSpeed(context: Context): Float {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getFloat(compassSpeed, 0.06f)
    }

}