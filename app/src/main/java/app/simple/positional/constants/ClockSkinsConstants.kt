package app.simple.positional.constants

import app.simple.positional.R

object ClockSkinsConstants {
    /**
     * Should follow the needle sequences of hours minutes and seconds for the ease of access
     * This pattern will be followed throughout the app
     */
    var clockNeedleSkins: Array<IntArray> = arrayOf(
            intArrayOf( // 0
                    R.drawable.clock_needle_minimal_hour,
                    R.drawable.clock_needle_minimal_minute,
                    R.drawable.clock_needle_minimal_second
            ),
            intArrayOf( // 1
                    R.drawable.clock_needle_red_rounded_hour,
                    R.drawable.clock_needle_red_rounded_minute,
                    R.drawable.clock_needle_red_rounded_second
            ),
            intArrayOf( // 2
                    R.drawable.clock_needle_thick_rounded_hour,
                    R.drawable.clock_needle_thick_rounded_minute,
                    R.drawable.clock_needle_thick_rounded_second
            ),
            intArrayOf( // 3
                    R.drawable.clock_needle_red_pointy_hour,
                    R.drawable.clock_needle_red_pointy_minute,
                    R.drawable.clock_needle_red_pointy_second
            ),
            intArrayOf( // 4
                    R.drawable.clock_hour,
                    R.drawable.clock_minute,
                    R.drawable.clock_seconds
            ),
            intArrayOf( // 5
                    R.drawable.clock_hollow_needle_hour,
                    R.drawable.clock_hollow_needle_minute,
                    R.drawable.clock_hollow_needle_second
            )
    )
}
