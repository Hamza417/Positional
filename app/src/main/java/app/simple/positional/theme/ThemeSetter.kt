package app.simple.positional.theme

import androidx.annotation.IntRange
import androidx.appcompat.app.AppCompatDelegate

fun setTheme(@IntRange(from = 1, to = 4) value: Int) {
    when (value) {
        1 -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        2 -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        3 ->  {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        4 -> {
            // TODO - yet to be implemented
        }
    }
}