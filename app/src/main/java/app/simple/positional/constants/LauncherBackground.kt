package app.simple.positional.constants

import android.graphics.Color
import app.simple.positional.R

val vectorBackground = arrayOf(
        R.drawable.launcher_day_01,
        R.drawable.launcher_day_02,
        R.drawable.launcher_day_03,
        R.drawable.launcher_day_04,
        R.drawable.launcher_day_05,
        R.drawable.launcher_day_06,
        R.drawable.launcher_day_07,
        R.drawable.launcher_day_08,
        R.drawable.launcher_day_09,
        R.drawable.launcher_day_10,
        R.drawable.launcher_day_11,
        R.drawable.launcher_day_12,
        R.drawable.launcher_day_13,
        R.drawable.launcher_day_14,
        R.drawable.launcher_day_15,
        R.drawable.launcher_day_16,
        R.drawable.launcher_day_17,
        R.drawable.launcher_day_18,
        R.drawable.launcher_day_19,
        R.drawable.launcher_day_20,
        R.drawable.launcher_day_21,
        R.drawable.launcher_day_22,
        R.drawable.launcher_day_23,
        R.drawable.launcher_day_24,
        R.drawable.launcher_day_25,
        R.drawable.launcher_day_26,
        R.drawable.launcher_day_27,
        R.drawable.launcher_day_28,
        R.drawable.launcher_day_29,
        R.drawable.launcher_day_30,
        R.drawable.launcher_day_31,
        R.drawable.launcher_day_32,
        R.drawable.launcher_day_33
)

val vectorBackgroundNight = arrayOf(
        R.drawable.launcher_night_01,
        R.drawable.launcher_night_02,
        R.drawable.launcher_night_03,
        R.drawable.launcher_night_04,
        R.drawable.launcher_night_05,
        R.drawable.launcher_night_06,
        R.drawable.launcher_night_07,
        R.drawable.launcher_night_08,
        R.drawable.launcher_night_09,
        R.drawable.launcher_night_10,
        R.drawable.launcher_night_11,
        R.drawable.launcher_night_12,
        R.drawable.launcher_night_13
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
        arrayOf(parseColor("#FFC2602A"), parseColor("#FFE89144")),
        arrayOf(parseColor("#FFFC6F55"), parseColor("#FFC3687B")),
        arrayOf(parseColor("#FF69525E"), parseColor("#FF432131")),
        arrayOf(parseColor("#FF441F00"), parseColor("#FFBE804C")),
        arrayOf(parseColor("#FF871E67"), parseColor("#FF4a0a32")),
        arrayOf(parseColor("#FFFF8A9F"), parseColor("#FFB6577C")),
        arrayOf(parseColor("#FFffe8c7"), parseColor("#FFf5c579")),
        arrayOf(parseColor("#FF91AEAD"), parseColor("#FF2f4a5d")),
        arrayOf(parseColor("#FF008D7D"), parseColor("#FF1e0b53")),
        arrayOf(parseColor("#FF872133"), parseColor("#FFBB4657")),
        arrayOf(parseColor("#FF44408D"), parseColor("#FF1C7EB6")),
        arrayOf(parseColor("#FF9F38AA"), parseColor("#FF541A5A")),
        arrayOf(parseColor("#FFEBAC59"), parseColor("#FF9C314C")),
        arrayOf(parseColor("#FF00748e"), parseColor("#FF004b5b")),
        arrayOf(parseColor("#FF8D6AA9"), parseColor("#FFBD95C3")),
        arrayOf(parseColor("#FF48543E"), parseColor("#FFBEA46F")),
        arrayOf(parseColor("#FFBE4E31"), parseColor("#FF7A3027")),
        arrayOf(parseColor("#FF912B86"), parseColor("#FF33307F")),
        arrayOf(parseColor("#FF7b8c62"), parseColor("#FF4b594b")),
        arrayOf(parseColor("#FFB1606B"), parseColor("#FFF3A464")),
        arrayOf(parseColor("#FFF27E8B"), parseColor("#FF395175")),
        arrayOf(parseColor("#FFBC8E6F"), parseColor("#FFE9C49E")),
)

val vectorNightColors: Array<Array<Int>> = arrayOf(
        arrayOf(parseColor("#FFffa32a"), parseColor("#FFcb555b")),
        arrayOf(parseColor("#FFd34f59"), parseColor("#FFc65464")),
        arrayOf(parseColor("#FF4c355e"), parseColor("#FF311b3f")),
        arrayOf(parseColor("#FF527AAA"), parseColor("#FF20344A")),
        arrayOf(parseColor("#FF3392B1"), parseColor("#FF1B2A65")),
        arrayOf(parseColor("#FF212B4F"), parseColor("#FF131E3A")),
        arrayOf(parseColor("#FF2d4383"), parseColor("#FFae4aa0")),
        arrayOf(parseColor("#FF9fb0cc"), parseColor("#FF121c33")),
        arrayOf(parseColor("#FF030B26"), parseColor("#FF463959")),
        arrayOf(parseColor("#FF608396"), parseColor("#FFf88063")),
        arrayOf(parseColor("#FF554686"), parseColor("#FF6e51c9")),
        arrayOf(parseColor("#FFf0a87f"), parseColor("#FF415262")),
        arrayOf(parseColor("#FFFFEC84"), parseColor("#FFFF8B88"))
)

fun parseColor(value: String): Int {
    return Color.parseColor(value)
}