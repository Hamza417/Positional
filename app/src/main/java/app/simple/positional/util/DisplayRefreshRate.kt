package app.simple.positional.util

import android.app.Activity
import android.content.Context

fun getDisplayRefreshRate(context: Context, activity: Activity): Float? {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        context.display?.refreshRate
    } else {
        // This method is for devices lower than API 30, deprecated in Android 11 but will work in older android versions
        @Suppress("deprecation")
        activity.windowManager.defaultDisplay.refreshRate
    }
}