package app.simple.positional.util

import android.app.Activity

object DisplayRefreshRate {
    fun Activity.getDisplayRefreshRate(): Float {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            display?.refreshRate ?: 60F
        } else {
            // This method is for devices lower than API 30, deprecated in Android 11 but will work in older android versions
            @Suppress("deprecation")
            windowManager.defaultDisplay.refreshRate
        }
    }
}