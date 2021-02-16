package app.simple.positional.constants

import android.graphics.Color
import kotlin.math.roundToInt

object SpeedometerConstants {

    private const val speedometerColorValueDivider = 10.0F

    fun getSpeedometerColor(value: Double): Int {
        return parseColor(getColor()[getValue(value)])
    }

    private fun getValue(value: Double): Int {
        return if ((value / speedometerColorValueDivider).roundToInt() == 0) {
            0
        } else {
            (value / speedometerColorValueDivider).roundToInt() - 1
        }
    }

    private fun getColor(): Array<String> {
        return arrayOf(
                "#008f15",
                "#3a9f11",
                "#5daf0b",
                "#7cbe04",
                "#9cce00",
                "#bcdd00",
                "#ddeb01",
                "#fff90f",
                "#ffe000",
                "#ffc600",
                "#ffaa00",
                "#ff8e00",
                "#ff6f00",
                "#ff4a00",
                "#ff0000",
        )
    }

    private fun parseColor(color: String): Int {
        return Color.parseColor(color)
    }
}
