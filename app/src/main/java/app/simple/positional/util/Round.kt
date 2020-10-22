package app.simple.positional.util

import kotlin.math.roundToInt

fun round(value: Double, places: Int): Double {
    var value = value
    require(places >= 0)
    val factor = Math.pow(10.0, places.toDouble()).toLong()
    value *= factor
    val tmp = value.roundToInt()
    return tmp.toDouble() / factor
}