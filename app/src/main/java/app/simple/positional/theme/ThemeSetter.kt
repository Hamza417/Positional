package app.simple.positional.theme

import androidx.annotation.IntRange
import androidx.appcompat.app.AppCompatDelegate
import java.util.*

fun setAppTheme(@IntRange(from = -1, to = 4) value: Int) {
    when (value) {
        AppCompatDelegate.MODE_NIGHT_NO -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        AppCompatDelegate.MODE_NIGHT_YES -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> {
            println("Called")
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
        }
        4 -> {
            // Day/Night Auto
            val calendar = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            if (calendar < 7 || calendar > 18) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else if (calendar < 18 || calendar > 6) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }
}