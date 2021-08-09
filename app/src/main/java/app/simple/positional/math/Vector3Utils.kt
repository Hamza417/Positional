package app.simple.positional.math

import kotlin.math.sqrt

object Vector3Utils {
    fun cross(first: FloatArray, second: FloatArray): FloatArray {
        return floatArrayOf(
                first[1] * second[2] - first[2] * second[1],
                first[2] * second[0] - first[0] * second[2],
                first[0] * second[1] - first[1] * second[0]
        )
    }

    fun minus(first: FloatArray, second: FloatArray): FloatArray {
        return floatArrayOf(
                first[0] - second[0],
                first[1] - second[1],
                first[2] - second[2]
        )
    }

    fun plus(first: FloatArray, second: FloatArray): FloatArray {
        return floatArrayOf(
                first[0] + second[0],
                first[1] + second[1],
                first[2] + second[2]
        )
    }

    fun times(arr: FloatArray, factor: Float): FloatArray {
        return floatArrayOf(
                arr[0] * factor,
                arr[1] * factor,
                arr[2] * factor
        )
    }

    fun dot(first: FloatArray, second: FloatArray): Float {
        return first[0] * second[0] + first[1] * second[1] + first[2] * second[2]
    }

    fun magnitude(arr: FloatArray): Float {
        return sqrt(arr[0] * arr[0] + arr[1] * arr[1] + arr[2] * arr[2])
    }

    fun normalize(arr: FloatArray): FloatArray {
        val mag = magnitude(arr)
        return floatArrayOf(
                arr[0] / mag,
                arr[1] / mag,
                arr[2] / mag
        )
    }
}