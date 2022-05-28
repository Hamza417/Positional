package app.simple.positional.math

object LowPassFilter {
    /**
     * Increase for better results
     * at the cost of high CPU usage
     */
    private const val SMOOTH_FACTOR_MAA = 2

    fun smoothAndSetReadings(readings: FloatArray, newReadings: FloatArray, readingsAlpha: Float) {
        readings[0] = readingsAlpha * newReadings[0] + (1 - readingsAlpha) * readings[0] // x
        readings[1] = readingsAlpha * newReadings[1] + (1 - readingsAlpha) * readings[1] // y
        readings[2] = readingsAlpha * newReadings[2] + (1 - readingsAlpha) * readings[2] // z
    }

    fun processWithMovingAverageGravity(list: ArrayList<Float>, gList: ArrayList<Float?>): ArrayList<Float?> {
        val listSize: Int = list.size //input list
        val iterations = listSize / SMOOTH_FACTOR_MAA
        if (gList.isNotEmpty()) {
            gList.clear()
        }
        var i = 0
        var node = 0
        while (i < iterations) {
            var num = 0f
            for (k in node until node + SMOOTH_FACTOR_MAA) {
                num += list[k]
            }
            node += SMOOTH_FACTOR_MAA
            num /= SMOOTH_FACTOR_MAA
            gList.add(num)
            i++
        }
        return gList
    }
}
