package app.simple.positional.theme

import androidx.annotation.IntRange
import androidx.appcompat.app.AppCompatDelegate

fun setTheme(@IntRange(from = -1, to = 4) value: Int) {
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
        4 -> {
            // Day/Night Auto
            // TODO - yet to be implemented
        }
    }
}