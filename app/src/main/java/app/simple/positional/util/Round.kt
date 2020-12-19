package app.simple.positional.util

import kotlin.math.pow
import kotlin.math.roundToInt

fun round(someValue: Double, places: Int): Double {
    return try {
        var value = someValue
        require(places >= 0)
        val factor = 10.0.pow(places.toDouble()).toLong()
        value *= factor
        val tmp = value.roundToInt()
        tmp.toDouble() / factor
    } catch (e: IllegalArgumentException) {
        Double.NaN
    }
}