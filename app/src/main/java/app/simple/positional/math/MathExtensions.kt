package app.simple.positional.math

import kotlin.math.*

@Suppress("unused")
object MathExtensions {
    fun normalizeAngle(angle: Float): Float {
        return wrap(angle, 0f, 360f) % 360
    }

    fun normalizeAngle(angle: Double): Double {
        return wrap(angle, 0.0, 360.0) % 360
    }

    private fun wrap(value: Float, min: Float, max: Float): Float {
        return wrap(value.toDouble(), min.toDouble(), max.toDouble()).toFloat()
    }

    private fun wrap(value: Double, min: Double, max: Double): Double {
        val range = max - min

        var newValue = value

        while (newValue > max) {
            newValue -= range
        }

        while (newValue < min) {
            newValue += range
        }

        return newValue
    }

    fun sinDegrees(angle: Double): Double {
        return sin(angle.toRadians())
    }

    fun tanDegrees(angle: Double): Double {
        return tan(angle.toRadians())
    }

    fun tanDegrees(angle: Float): Float {
        return tan(angle.toRadians())
    }

    fun cosDegrees(angle: Double): Double {
        return cos(angle.toRadians())
    }

    private fun Double.toRadians(): Double {
        return Math.toRadians(this)
    }

    private fun Float.toRadians(): Float {
        return Math.toRadians(this.toDouble()).toFloat()
    }

    fun deltaAngle(angle1: Float, angle2: Float): Float {
        var delta = angle2 - angle1
        delta += 180
        delta -= floor(delta / 360) * 360
        delta -= 180
        if (abs(abs(delta) - 180) <= Float.MIN_VALUE) {
            delta = 180f
        }
        return delta
    }

    fun clamp(value: Float, minimum: Float, maximum: Float): Float {
        return min(maximum, max(minimum, value))
    }

    fun Float.toDegrees(): Float {
        return Math.toDegrees(this.toDouble()).toFloat()
    }

    fun Double.toDegrees(): Double {
        return Math.toDegrees(this)
    }

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
}