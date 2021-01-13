@file:Suppress("unused")

package app.simple.positional.math

import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.hypot
import kotlin.math.sqrt

object Angle {
    /**
     * @return The angle of the unit circle with the image view's center
     */
    fun getAngleUsingQuadrant(xTouch: Double, yTouch: Double, dialerWidth: Float, dialerHeight: Float): Double {
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

    fun getAngle(xTouch: Double, yTouch: Double, dialerWidth: Float, dialerHeight: Float): Float {
        val centerX = dialerWidth / 2.0F
        val centerY = dialerHeight / 2.0F
        val x2 = xTouch - centerX
        val y2 = yTouch - centerY
        val d1 = sqrt((centerY * centerY).toDouble())
        val d2 = sqrt((x2 * x2 + y2 * y2))
        return if (xTouch >= centerX) {
            Math.toDegrees(acos((-centerY * y2) / (d1 * d2))).toFloat()
        } else
            (360 - Math.toDegrees(acos((-centerY * y2) / (d1 * d2)))).toFloat()
    }

    fun Float.toThreeSixty(): Float {
        return if (this < 0) {
            this + 360F
        } else {
            this
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
}