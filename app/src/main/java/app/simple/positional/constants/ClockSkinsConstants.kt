package app.simple.positional.constants

import app.simple.positional.R

val clockFaceSkins: IntArray = intArrayOf(
        // Minimal
        0, // 0 , Disable
        R.drawable.clock_face_minimal, // 1 , Minimal
        R.drawable.clock_face_numbers, // 2 , Number
        R.drawable.clock_face_roman, // 3 , Roman
        R.drawable.clock_face_numbers_two, // 4 , Number
        R.drawable.clock_face_retro_plain, // 5 , Number
        R.drawable.clock_face_retro_wrong_roman, // 6 , Roman
        R.drawable.clock_face_roman_bird_cage, // 7, Roman
        R.drawable.clock_face_number_birds_branches, // 8, Number
        R.drawable.clock_face_roman_birds_and_hearts, // 9, Roman
        R.drawable.clock_face_roman_birds_and_branches, // 10, Roman
        R.drawable.clock_face_number_birds_and_hearts, // 11, Number
        R.drawable.clock_face_minimal_simple_face, // 12, Minimal
        R.drawable.clock_face_roman_bullets, // 13, Roman
        R.drawable.clock_face_number_dots, // 14, Number
        R.drawable.clock_face_number_minutes, // 15, Number
        R.drawable.clock_face_number_bullets, // 16, Number
        R.drawable.clock_face_minimal_rounded_hours, // 17, Minimal
        R.drawable.clock_face_number_arabic, // 18, Number - Might be removed
)

/**
 * Should follow the needle sequences of [hours] [minutes] and [seconds] for the ease of access
 * This pattern will be followed throughout the app
 */
var clockNeedleSkins: Array<Array<Int>> = arrayOf(
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

