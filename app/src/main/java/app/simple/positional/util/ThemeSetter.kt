package app.simple.positional.util

import androidx.annotation.IntRange
import androidx.appcompat.app.AppCompatDelegate
import java.util.*

object ThemeSetter {
    fun setAppTheme(@IntRange(from = -1, to = 4) value: Int) {
        when (value) {
            AppCompatDelegate.MODE_NIGHT_NO -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            AppCompatDelegate.MODE_NIGHT_YES -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
            }
            4 -> {
                if (isDayOrNight()) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }
        }
    }

    /**
     * Check if it is day or night
     *
     * @return true if it is day, false if it is night
     */
    fun isDayOrNight(): Boolean {
        val cal = Calendar.getInstance()
        cal.timeInMillis = System.currentTimeMillis()
        val hour = cal[Calendar.HOUR_OF_DAY]
        return hour >= 6 && hour < 18
    }
}
