package app.simple.positional.util

import kotlin.math.asin
import kotlin.math.hypot

/**
 * @return The angle of the unit circle with the image view's center
 */
fun getAngle(xTouch: Double, yTouch: Double, dialerWidth: Float, dialerHeight: Float): Double {
    val x: Double = xTouch - dialerWidth / 2.0
    val y: Double = dialerHeight - yTouch - dialerHeight / 2.0
    return when (getQuadrant(x, y)) {
        1 -> asin(y / hypot(x, y)) * 180 / Math.PI
        2 -> 180 - asin(y / hypot(x, y)) * 180 / Math.PI
        3 -> 180 + -1 * asin(y / hypot(x, y)) * 180 / Math.PI
        4 -> 360 + asin(y / hypot(x, y)) * 180 / Math.PI
        else -> 0.0
    }
}

/**
 * @return The selected quadrant.
 */
private fun getQuadrant(x: Double, y: Double): Int {
    return if (x >= 0) {
        if (y >= 0) 1 else 4
    } else {
        if (y >= 0) 2 else 3
    }
}