package app.simple.positional.constants

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
            intArrayOf(0xFF434E94.toInt(), 0xFF081146.toInt()), // 07
    )

    val vectorNightColors: Array<IntArray> = arrayOf(
            intArrayOf(0xFFFFA32A.toInt(), 0xFFCB555B.toInt()),
    )
}
