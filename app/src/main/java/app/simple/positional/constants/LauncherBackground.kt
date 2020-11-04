package app.simple.positional.constants

import android.graphics.Color
import app.simple.positional.R

val vectorBackground = arrayOf(
        R.drawable.launcher_background_one,
        R.drawable.launcher_background_two,
        R.drawable.launcher_background_three,
        R.drawable.launcher_background_four,
        R.drawable.launcher_background_five,
        R.drawable.launcher_background_six,
        R.drawable.launcher_background_seven,
        R.drawable.launcher_background_eight
)

val vectorColors: Array<Array<Int>> = arrayOf(
        arrayOf(parseColor("#FFF6E58D"), parseColor("#FFE056FD")),
        arrayOf(parseColor("#FFFFD71D"), parseColor("#FF804700")),
        arrayOf(parseColor("#FFaa8659"), parseColor("#FFaa8659")),
        arrayOf(parseColor("#FF9d56a0"), parseColor("#FF246887")),
        arrayOf(parseColor("#FFDE542A"), parseColor("#FFBA2D0A")),
        arrayOf(parseColor("#FF52618c"), parseColor("#FF6b8ea9")),
        arrayOf(parseColor("#FF434E94"), parseColor("#FF081146")),
        arrayOf(parseColor("#FFDE7E42"), parseColor("#FFBF5047"))
)

fun parseColor(value: String): Int {
    return Color.parseColor(value)
}