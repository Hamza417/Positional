package app.simple.positional.preference

import android.content.Context
import app.simple.positional.constants.*

class ClockPreferences {
    fun setClockFaceTheme(value: Int, context: Context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt(clockFace, value).apply()
    }

    fun getClockFaceTheme(context: Context): Int {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt(clockFace, 12)
    }

    fun setClockNeedleTheme(value: Int, context: Context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt(clockNeedle, value).apply()
    }

    fun getClockNeedleTheme(context: Context): Int {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt(clockNeedle, 1)
    }

    fun setLastFace(dial: Int, context: Context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putInt(lastClockFace, dial).apply()
    }

    fun getLastFace(context: Context): Int {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getInt(lastClockFace, 1)
    }

    fun setMovementType(value: Boolean, context: Context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putBoolean(clockNeedleMovementType, value).apply()
    }

    fun getMovementType(context: Context): Boolean {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getBoolean(clockNeedleMovementType, true)
    }

    // Dial Opacity
    fun setFaceOpacity(value: Float, context: Context) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putFloat(clockFaceAlpha, value).apply()
    }

    fun getFaceOpacity(context: Context): Float {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getFloat(clockFaceAlpha, 0.75f)
    }
}