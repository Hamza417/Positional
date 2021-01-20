package app.simple.positional.math

object UnitConverter {
    /**
     * Converts double value in km to miles
     */
    fun Double.toMiles(): Double {
        return this * 0.621371
    }

    /**
     * Converts double value in m/s to km/h
     */
    fun Double.toKiloMetersPerHour(): Double {
        return this * 3.6
    }

    /**
     * Converts float value of meters to kilometers
     */
    fun Float.toKilometers(): Float {
        return this / 1000f
    }

    /**
     * Converts float value in meters to miles
     */

    fun Float.toMiles(): Float {
        return this / 1609
    }

    /**
     * Converts double value in km/h to miles/hour
     */
    fun Double.toMilesPerHour(): Double {
        return this * 0.621371
    }

    /**
     * Converts double value in meters to feet
     */
    fun Double.toFeet(): Double {
        return this * 3.28084
    }
}