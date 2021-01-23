package app.simple.positional.math

object LowPassFilter {
    fun smoothAndSetReadings(readings: FloatArray, newReadings: FloatArray, readingsAlpha: Float) {
        readings[0] = readingsAlpha * newReadings[0] + (1 - readingsAlpha) * readings[0] // x
        readings[1] = readingsAlpha * newReadings[1] + (1 - readingsAlpha) * readings[1] // y
        readings[2] = readingsAlpha * newReadings[2] + (1 - readingsAlpha) * readings[2] // z
    }

}