package app.simple.positional.util

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