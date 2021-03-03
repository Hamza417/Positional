@file:Suppress("unused")

package app.simple.positional.math

import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.hypot
import kotlin.math.sqrt

object Angle {
    /**
     * @deprecated use [Angle.getAngle] function for accurate results
     * @return The angle of the unit circle with the image view's center
     */
    @Deprecated("Not suitable for angle measurements")
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
        val d2 = sqrt(x2 * x2 + y2 * y2)
        return if (xTouch >= centerX) {
            Math.toDegrees(acos(-centerY * y2 / (d1 * d2))).toFloat()
        } else
            (360 - Math.toDegrees(acos(-centerY * y2 / (d1 * d2)))).toFloat()
    }

    /**
     * Normalizes Euler angle within an acceptable range
     * such as an image rotation could exceed 360° in some cases where
     * few extra values are simplified together, in short this fun limits
     * the value range to be within 0° - 360° even if the actual values are
     * more or less
     *
     * @param [inverseResult] inverses the final value by 360° useful in cases
     * like compass rotation where degrees are inverted for fetching direction
     *
     * @return [Float]
     */
    fun Float.normalizeEulerAngle(inverseResult: Boolean): Float {
        var normalized = this % 360
        if (normalized < 0) normalized += 360
        return if (inverseResult) normalized - 360F else normalized
    }

    /**
     * @return [Int] The selected quadrant.
     */
    private fun getQuadrant(x: Double, y: Double): Int {
        return if (x >= 0) {
            if (y >= 0) 1 else 4
        } else {
            if (y >= 0) 2 else 3
        }
    }
}