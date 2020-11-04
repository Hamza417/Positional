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
        R.drawable.launcher_background_seven
)

val vectorColors: Array<Array<Int>> = arrayOf(
        arrayOf(parseColor("#F6E58D"), parseColor("#E056FD")),
        arrayOf(parseColor("#FFD71D"), parseColor("#804700")),
        arrayOf(parseColor("#aa8659"), parseColor("#aa8659")),
        arrayOf(parseColor("#9d56a0"), parseColor("#246887")),
        arrayOf(parseColor("#DE542A"), parseColor("#BA2D0A")),
        arrayOf(parseColor("#52618c"), parseColor("#6b8ea9")),
        arrayOf(parseColor("#434E94"), parseColor("#081146"))
)

fun parseColor(value: String): Int {
    return Color.parseColor(value)
}