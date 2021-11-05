package app.simple.positional.constants

import android.graphics.Color
import app.simple.positional.R
import app.simple.positional.preferences.GPSPreferences

object LocationPins {
    val locationsPins = intArrayOf(
            R.drawable.ic_pin_01,
            R.drawable.ic_pin_02,
            R.drawable.ic_pin_03,
            R.drawable.ic_pin_04,
            R.drawable.ic_pin_05,
            R.drawable.ic_pin_06,
            R.drawable.ic_pin_07,
            R.drawable.ic_pin_08,
            R.drawable.ic_pin_09,
            R.drawable.ic_pin_10
    )

    private val circleFillColor = intArrayOf(
            Color.parseColor("#4D1B9CFF"), // 01
            Color.parseColor("#4DFF8859"), // 02
            Color.parseColor("#4DFF8859"), // 03
            Color.parseColor("#4D4c9acc"), // 04
            Color.parseColor("#4Ded765e"), // 05
            Color.parseColor("#4DEB2A2E"), // 06
            Color.parseColor("#4D943b5e"), // 07
            Color.parseColor("#4D8B3B94"), // 08
            Color.parseColor("#4Df38630"), // 09
            Color.parseColor("#4DFF695B"), // 10
    )

    private val circleStrokeColor = intArrayOf(
            Color.parseColor("#1B9CFF"), // 01
            Color.parseColor("#FF8859"), // 02
            Color.parseColor("#FF8859"), // 03
            Color.parseColor("#4c9acc"), // 04
            Color.parseColor("#ed765e"), // 05
            Color.parseColor("#EB2A2E"), // 06
            Color.parseColor("#943b5e"), // 07
            Color.parseColor("#8B3B94"), // 08
            Color.parseColor("#f38630"), // 09
            Color.parseColor("#FF695B"), // 10
    )

    fun getLocationPin(): Int {
        return locationsPins[GPSPreferences.getPinSkin()]
    }

    fun getCircleFillColor(): Int {
        return circleFillColor[GPSPreferences.getPinSkin()]
    }

    fun getCircleStrokeColor(): Int {
        return circleStrokeColor[GPSPreferences.getPinSkin()]
    }
}