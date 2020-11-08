package app.simple.positional.util

import app.simple.positional.R

/**
 * TODO - More research required
 *
 * I am currently using both when and if else to decide what phase of the moon currently is
 * and will update this section as I improve my knowledge about moon phases..
 *
 * Moon phase. Starts at {@code -180.0} (new moon, waxing), passes {@code 0.0} (full
 * moon) and moves toward {@code 180.0} (waning, new moon).
 */
fun getMoonPhase(moonAngle: Double): String {
    if (moonAngle > -180.0 && moonAngle < -135.0) {
        return "New Moon"
    } else if (moonAngle > -135 && moonAngle < -90) {
        return "Waxing Crescent"
    } else if (moonAngle > -90 && moonAngle < -45) {
        return "First Quarter"
    } else if (moonAngle > -45 && moonAngle < 0) {
        return "Waxing Gibbous"
    } else if (moonAngle > 0 && moonAngle < 45) {
        return "Full Moon"
    } else if (moonAngle > 45 && moonAngle < 90) {
        return "Waning Gibbous"
    } else if (moonAngle > 90 && moonAngle < 135) {
        return "Third Quarter"
    } else if (moonAngle > 135 && moonAngle < 180) {
        return "Waning Crescent"
    } else {
        return "N/A"
    }
}

fun getMoonPhaseGraphics(moonAngle: Double): Int {
    when (moonAngle) {
        in -180.0..-135.0 -> {
            return R.drawable.moon_phase_01
        }
        in -135.1..-90.0 -> {
            return R.drawable.moon_phase_02
        }
        in -90.1..-45.0 -> {
            return R.drawable.moon_phase_03
        }
        in -45.1..0.0 -> {
            return R.drawable.moon_phase_04
        }
        in 0.1..45.0 -> {
            return R.drawable.moon_phase_05
        }
        in 45.1..90.0 -> {
            return R.drawable.moon_phase_06
        }
        in 90.1..135.0 -> {
            return R.drawable.moon_phase_07
        }
        in 135.1..180.0 -> {
            return R.drawable.moon_phase_08
        }
        else -> {
            return 0
        }
    }
}