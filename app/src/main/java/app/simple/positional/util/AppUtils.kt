package app.simple.positional.util

import app.simple.positional.BuildConfig

object AppUtils {

    fun isLiteFlavor() : Boolean {
        return BuildConfig.FLAVOR == "lite"
    }

}