package app.simple.positional.preference

import android.content.Context
import androidx.annotation.NonNull
import app.simple.positional.constants.*

class GPSPreferences {
    fun isLabelOn(context: Context): Boolean {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getBoolean(GPSLabelMode, false)
    }

    fun setLabelMode(context: Context, value: Boolean) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putBoolean(GPSLabelMode, value).apply()
    }

    fun isSatelliteOn(context: Context): Boolean {
        return context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getBoolean(GPSSatellite, false)
    }

    fun setSatelliteMode(context: Context, value: Boolean) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putBoolean(GPSSatellite, value).apply()
    }

    // Last Session Coordinates
    fun setLastLatitude(@NonNull context: Context, value: Float) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putFloat(lastLatitude, value).apply()
    }

    fun setLastLongitude(@NonNull context: Context, value: Float) {
        context.getSharedPreferences(preferences, Context.MODE_PRIVATE).edit().putFloat(lastLongitude, value).apply()
    }

    fun getLastCoordinates(context: Context): Array<Float> {
        return arrayOf(
                context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getFloat(lastLatitude, 48.8584f),
                context.getSharedPreferences(preferences, Context.MODE_PRIVATE).getFloat(lastLongitude, 2.2945f)
        )
    }
}