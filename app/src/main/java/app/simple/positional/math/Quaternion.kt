package app.simple.positional.math

data class Quaternion(val x: Float, val y: Float, val z: Float, val w: Float) {

    private val arr = floatArrayOf(x, y, z, w)

    fun toFloatArray(): FloatArray {
        return arr.clone()
    }

    operator fun times(other: Quaternion): Quaternion {
        val out = FloatArray(4)
        QuaternionMath.multiply(arr, other.arr, out)
        return from(out)
    }

    fun subtractRotation(other: Quaternion): Quaternion {
        val out = FloatArray(4)
        QuaternionMath.subtractRotation(arr, other.arr, out)
        return from(out)
    }

    operator fun plus(other: Quaternion): Quaternion {
        val out = FloatArray(4)
        QuaternionMath.add(arr, other.arr, out)
        return from(out)
    }

    operator fun minus(other: Quaternion): Quaternion {
        val out = FloatArray(4)
        QuaternionMath.subtract(arr, other.arr, out)
        return from(out)
    }

    fun magnitude(): Float {
        return QuaternionMath.magnitude(arr)
    }

    fun normalize(): Quaternion {
        val out = FloatArray(4)
        QuaternionMath.normalize(arr, out)
        return from(out)
    }

    fun conjugate(): Quaternion {
        val out = FloatArray(4)
        QuaternionMath.conjugate(arr, out)
        return from(out)
    }

    fun inverse(): Quaternion {
        val out = FloatArray(4)
        QuaternionMath.inverse(arr, out)
        return from(out)
    }

    fun rotate(vector: Vector3): Vector3 {
        val out = FloatArray(3)
        QuaternionMath.rotate(vector.toFloatArray(), arr, out)
        return Vector3.from(out)
    }

    fun toEuler(): Euler {
        val out = FloatArray(3)
        QuaternionMath.toEuler(arr, out)
        return Euler.from(out)
    }

    companion object {
        val zero = Quaternion(0f, 0f, 0f, 1f)

        fun from(arr: FloatArray): Quaternion {
            return Quaternion(arr[0], arr[1], arr[2], arr[3])
        }

        fun from(euler: Euler): Quaternion {
            val out = FloatArray(4)
            QuaternionMath.fromEuler(euler.toFloatArray(), out)
            return from(out)
        }
    }
}
