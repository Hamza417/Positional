package app.simple.positional.math

data class Euler(val roll: Float, val pitch: Float, val yaw: Float) {
    fun toFloatArray(): FloatArray {
        return floatArrayOf(roll, pitch, yaw)
    }

    fun toQuaternion(): Quaternion {
        return Quaternion.from(this)
    }

    companion object {
        fun from(arr: FloatArray): Euler {
            return Euler(arr[0], arr[1], arr[2])
        }

        fun from(quaternion: Quaternion): Euler {
            return quaternion.toEuler()
        }
    }
}
