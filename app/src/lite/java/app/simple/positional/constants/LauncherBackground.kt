package app.simple.positional.constants

import android.graphics.Color.parseColor
import app.simple.positional.R

object LauncherBackground {
    val vectorBackground = intArrayOf(
            R.drawable.launcher_day_07,
    )

    val vectorBackgroundNight = intArrayOf(
            R.drawable.launcher_night_01,
    )

    /**
     * The gradient color pattern is linear and offset is +Y to -Y and
     * it is advisable to use the darker shade first and light shade after
     * to achieve a nice looking gradient tint
     */
    val vectorColors: Array<IntArray> = arrayOf(
            intArrayOf(parseColor("#FF434E94"), parseColor("#FF081146")), // 07
    )

    val vectorNightColors: Array<IntArray> = arrayOf(
            intArrayOf(parseColor("#FFffa32a"), parseColor("#FFcb555b")),
    )
}
