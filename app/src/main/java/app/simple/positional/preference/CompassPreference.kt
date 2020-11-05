package app.simple.positional.preference

import android.content.Context
import android.hardware.SensorManager
import app.simple.positional.R
import app.simple.positional.constants.*

class CompassPreference {
    // Parallax
    fun setParallax(value: Boolean, context: Context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putBoolean(parallax, value).apply()
    }

    fun getParallax(context: Context): Boolean {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getBoolean(parallax, false)
    }

    // Delay
    fun getDelay(context: Context): Int {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt(delay, SensorManager.SENSOR_DELAY_GAME)
    }

    fun setDelay(value: Int, context: Context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt(delay, value).apply()
    }

    // Compass Skins
    fun setSkins(needleSkinValue: Int, dialSkinValue: Int, context: Context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt(needle, needleSkinValue).apply()
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt(dial, dialSkinValue).apply()
    }

    fun setNeedle(needleSkinValue: Int, context: Context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt(needle, needleSkinValue).apply()
    }

    fun setDial(dialSkinValue: Int, context: Context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt(dial, dialSkinValue).apply()
    }

    fun getNeedle(context: Context): Int {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt(needle, R.drawable.compass_needle_classic)
    }

    fun setLastNeedle(dial: Int, context: Context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt(lastNeedleTheme, dial).apply()
    }

    fun getLastNeedle(context: Context): Int {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt(lastNeedleTheme, R.drawable.compass_dial_minimal)
    }

    fun getDial(context: Context): Int {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt(dial, R.drawable.compass_dial_minimal)
    }

    fun setLastDial(dial: Int, context: Context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt(lastDialTheme, dial).apply()
    }

    fun getLastDial(context: Context): Int {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt(lastDialTheme, R.drawable.compass_dial_minimal)
    }

    fun getSkins(context: Context): IntArray {
        return intArrayOf(
                context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt(needle, R.drawable.compass_needle_classic),
                context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt(dial, R.drawable.compass_dial_minimal))
    }

    // Dial Opacity
    fun setDialOpacity(value: Float, context: Context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putFloat(dialAlpha, value).apply()
    }

    fun getDialOpacity(context: Context): Float {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getFloat(dialAlpha, 0.75f)
    }

    /**
     * Rotate which Dial or Needle
     *
     * @param value 1 for needle
     * 2 for dial
     * 3 for both
     * Needle is the priority/default
     */
    fun setRotatePreference(context: Context, value: Int) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt(whichObjectToRotate, value).apply()
    }

    fun getRotatePreference(context: Context): Int {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt(whichObjectToRotate, 1)
    }

    // Parallax Sensitivity
    fun setParallaxSensitivity(context: Context, value: Int) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt(sensitivity, value).apply()
    }

    fun getParallaxSensitivity(context: Context): Int {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt(sensitivity, 1)
    }

    /**
     * @param value 1 for Accelerometer
     * 2 for Gyroscope
     * 2 is default, if not available 1 is used
     */
    fun setParallaxSensor(context: Context, value: Int) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt(parallaxSensor, value).apply()
    }

    fun getParallaxSensor(context: Context): Int {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt(parallaxSensor, 2)
    }
}