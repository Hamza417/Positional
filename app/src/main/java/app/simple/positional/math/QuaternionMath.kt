package app.simple.positional.math

import app.simple.positional.math.MathExtensions.toDegrees
import kotlin.math.*

object QuaternionMath {
    const val X = 0
    const val Y = 1
    const val Z = 2
    const val W = 3

    fun fromEuler(euler: FloatArray, out: FloatArray){
        val cosY = MathExtensions.cosDegrees(euler[2] / 2.0)
        val sinY = MathExtensions.sinDegrees(euler[2] / 2.0)
        val cosP = MathExtensions.cosDegrees(euler[1] / 2.0)
        val sinP = MathExtensions.sinDegrees(euler[1] / 2.0)
        val cosR = MathExtensions.cosDegrees(euler[0] / 2.0)
        val sinR = MathExtensions.sinDegrees(euler[0] / 2.0)

        val w = cosR * cosP * cosY + sinR * sinP * sinY
        val x = sinR * cosP * cosY - cosR * sinP * sinY
        val y = cosR * sinP * cosY + sinR * cosP * sinY
        val z = cosR * cosP * sinY - sinR * sinP * cosY

        out[X] = x.toFloat()
        out[Y] = y.toFloat()
        out[Z] = z.toFloat()
        out[W] = w.toFloat()
    }

    fun rotate(point: FloatArray, quat: FloatArray, out: FloatArray) {
        val u = floatArrayOf(quat[X], quat[Y], quat[Z])
        val s = quat[W]
        val first = Vector3Utils.times(u, Vector3Utils.dot(u, point) * 2f)
        val second = Vector3Utils.times(point, s * s - Vector3Utils.dot(u, u))
        val third = Vector3Utils.times(Vector3Utils.cross(u, point), 2f * s)

        val sum = Vector3Utils.plus(first, Vector3Utils.plus(second, third))
        out[0] = sum[0]
        out[1] = sum[1]
        out[2] = sum[2]
    }

    fun toEuler(quaternion: FloatArray, out: FloatArray) {
        val yaw = atan2(
                2 * (quaternion[W] * quaternion[Z] + quaternion[X] * quaternion[Y]),
                1 - 2 * (quaternion[Y] * quaternion[Y] + quaternion[Z] * quaternion[Z])
        )
        val sinP = 2 * (quaternion[W] * quaternion[Y] - quaternion[Z] * quaternion[X])
        val pitch = if (sinP.absoluteValue >= 1) {
            (Math.PI / 2).withSign(sinP.toDouble()).toFloat()
        } else {
            asin(sinP)
        }
        val roll = atan2(
                2 * (quaternion[W] * quaternion[X] + quaternion[Y] * quaternion[Z]),
                1 - 2 * (quaternion[X] * quaternion[X] + quaternion[Y] * quaternion[Y])
        )

        out[0] = roll.toDegrees()
        out[1] = pitch.toDegrees()
        out[2] = yaw.toDegrees()
    }

    fun multiply(a: FloatArray, b: FloatArray, out: FloatArray) {
        val x = a[W] * b[X] + a[X] * b[W] + a[Y] * b[Z] - a[Z] * b[Y]
        val y = a[W] * b[Y] - a[X] * b[Z] + a[Y] * b[W] + a[Z] * b[X]
        val z = a[W] * b[Z] + a[X] * b[Y] - a[Y] * b[X] + a[Z] * b[W]
        val w = a[W] * b[W] - a[X] * b[X] - a[Y] * b[Y] - a[Z] * b[Z]
        out[X] = x
        out[Y] = y
        out[Z] = z
        out[W] = w
    }

    fun add(a: FloatArray, b: FloatArray, out: FloatArray) {
        out[X] = a[X] + b[X]
        out[Y] = a[Y] + b[Y]
        out[Z] = a[Z] + b[Z]
        out[W] = a[W] + b[W]
    }

    fun subtractRotation(a: FloatArray, b: FloatArray, out: FloatArray){
        val inverse = FloatArray(4)
        inverse(b, inverse)
        multiply(inverse, a, out)
        normalize(out, out)
    }

    fun subtract(a: FloatArray, b: FloatArray, out: FloatArray) {
        out[X] = a[X] - b[X]
        out[Y] = a[Y] - b[Y]
        out[Z] = a[Z] - b[Z]
        out[W] = a[W] - b[W]
    }

    fun magnitude(quat: FloatArray): Float {
        return sqrt(quat[X] * quat[X] + quat[Y] * quat[Y] + quat[Z] * quat[Z] + quat[W] * quat[W])
    }

    fun normalize(quat: FloatArray, out: FloatArray) {
        val mag = magnitude(quat)
        divide(quat, mag, out)
    }

    fun divide(quat: FloatArray, divisor: Float, out: FloatArray) {
        out[X] = quat[X] / divisor
        out[Y] = quat[Y] / divisor
        out[Z] = quat[Z] / divisor
        out[W] = quat[W] / divisor
    }

    fun multiply(quat: FloatArray, scale: Float, out: FloatArray) {
        out[X] = quat[X] * scale
        out[Y] = quat[Y] * scale
        out[Z] = quat[Z] * scale
        out[W] = quat[W] * scale
    }

    fun conjugate(quat: FloatArray, out: FloatArray) {
        out[X] = -quat[X]
        out[Y] = -quat[Y]
        out[Z] = -quat[Z]
        out[W] = quat[W]
    }

    fun inverse(quat: FloatArray, out: FloatArray) {
        val mag = magnitude(quat)
        conjugate(quat, out)
        divide(out, mag * mag, out)
    }
}