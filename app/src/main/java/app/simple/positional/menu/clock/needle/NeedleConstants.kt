package app.simple.positional.menu.clock.needle

import app.simple.positional.R

/**
 * Should follow the needle sequences of [hours] [minutes] and [seconds] for the ease of access
 * This pattern will be followed throughout the app
 */
var needleSkins: Array<Array<Int>> = arrayOf(
        arrayOf( // 0, Minimal
                R.drawable.clock_needle_minimal_hour,
                R.drawable.clock_needle_minimal_minute,
                R.drawable.clock_needle_minimal_second
        ),
        arrayOf( // 1, Minimal
                R.drawable.clock_needle_red_rounded_hour,
                R.drawable.clock_needle_red_rounded_minute,
                R.drawable.clock_needle_red_rounded_second
        ),
        arrayOf( // 2, Minimal
                R.drawable.clock_needle_thick_rounded_hour,
                R.drawable.clock_needle_thick_rounded_minute,
                R.drawable.clock_needle_thick_rounded_second
        ),
        arrayOf( // 3, Minimal
                R.drawable.clock_needle_red_pointy_hour,
                R.drawable.clock_needle_red_pointy_minute,
                R.drawable.clock_needle_red_pointy_second
        ),
        arrayOf( // 4, Retro
                R.drawable.clock_needle_retro_black_red_hour,
                R.drawable.clock_needle_retro_black_red_minute,
                R.drawable.clock_needle_retro_black_red_second
        )
)