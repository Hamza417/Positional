package app.simple.positional.util

import android.view.Surface
import android.view.WindowManager

public fun adjustAzimuthForDisplayRotation(azimuth: Float, windowManager: WindowManager): Float {
    return when (val displayRotation = windowManager.defaultDisplay.rotation) {
        Surface.ROTATION_0 -> azimuth
        Surface.ROTATION_90 -> azimuth - 270f
        Surface.ROTATION_180 -> azimuth - 180f
        Surface.ROTATION_270 -> azimuth - 90f
        else -> throw IllegalArgumentException("Unexpected display rotation: $displayRotation")
    }
}