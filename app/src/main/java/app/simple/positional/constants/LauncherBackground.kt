package app.simple.positional.constants

import android.graphics.Color
import app.simple.positional.R

val vectorBackground = arrayOf(
        R.drawable.launcher_background_day_1,
        R.drawable.launcher_background_day_2,
        R.drawable.launcher_background_day_3,
        R.drawable.launcher_background_day_4,
        R.drawable.launcher_background_day_5,
        R.drawable.launcher_background_day_6,
        R.drawable.launcher_background_day_7,
        R.drawable.launcher_background_day_8,
        R.drawable.launcher_background_day_9,
        R.drawable.launcher_background_day_10,
        R.drawable.launcher_background_day_11,
        R.drawable.launcher_background_day_12
)

val vectorBackgroundNight = arrayOf(
        R.drawable.launcher_background_night_1,
        R.drawable.launcher_background_night_2
)

val vectorColors: Array<Array<Int>> = arrayOf(
        arrayOf(parseColor("#FFF6E58D"), parseColor("#FFE056FD")),
        arrayOf(parseColor("#FFFFD71D"), parseColor("#FF804700")),
        arrayOf(parseColor("#FFaa8659"), parseColor("#FFaa8659")),
        arrayOf(parseColor("#FF9d56a0"), parseColor("#FF246887")),
        arrayOf(parseColor("#FFDE542A"), parseColor("#FFBA2D0A")),
        arrayOf(parseColor("#FF52618c"), parseColor("#FF6b8ea9")),
        arrayOf(parseColor("#FF434E94"), parseColor("#FF081146")),
        arrayOf(parseColor("#FFDE7E42"), parseColor("#FFBF5047")),
        arrayOf(parseColor("#FF246887"), parseColor("#FF247ca7")),
        arrayOf(parseColor("#FFff7841"), parseColor("#FFf14f2a")),
        arrayOf(parseColor("#FF596869"), parseColor("#FFa9a094")),
        arrayOf(parseColor("#FFC2602A"), parseColor("#FFE89144"))
)

val vectorNightColors: Array<Array<Int>> = arrayOf(
        arrayOf(parseColor("#FFffa32a"), parseColor("#FFcb555b")),
        arrayOf(parseColor("#FFd34f59"), parseColor("#FFc65464"))
)

fun parseColor(value: String): Int {
    return Color.parseColor(value)
}