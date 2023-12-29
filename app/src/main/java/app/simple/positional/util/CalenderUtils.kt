package app.simple.positional.util

object CalenderUtils {

    /**
     * Check if it's Day or Night based on the current timezone and time
     * @return true if it's day, false if it's night
     */
    fun isDay(): Boolean {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return hour in 6..18
    }
}