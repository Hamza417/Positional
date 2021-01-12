package app.simple.positional.util

import android.content.Context
import app.simple.positional.R

object MoonAngle {
    /**
     * TODO - More research required
     *
     * I am currently using both when and if else to decide what phase of the moon currently is
     * and will update this section as I improve my knowledge about moon phases..
     *
     * Moon phase. Starts at {@code -180.0} (new moon, waxing), passes {@code 0.0} (full
     * moon) and moves toward {@code 180.0} (waning, new moon).
     */
    fun getMoonPhase(context: Context, moonAngle: Double): String {
        return if (moonAngle > -180.0 && moonAngle < -135.0) {
            context.getString(R.string.new_moon)
        } else if (moonAngle > -135 && moonAngle < -90) {
            context.getString(R.string.waxing_crescent)
        } else if (moonAngle > -90 && moonAngle < -45) {
            context.getString(R.string.first_quarter)
        } else if (moonAngle > -45 && moonAngle < 0) {
            context.getString(R.string.waxing_gibbous)
        } else if (moonAngle > 0 && moonAngle < 45) {
            context.getString(R.string.full_moon)
        } else if (moonAngle > 45 && moonAngle < 90) {
            context.getString(R.string.waning_gibbous)
        } else if (moonAngle > 90 && moonAngle < 135) {
            context.getString(R.string.third_quarter)
        } else if (moonAngle > 135 && moonAngle < 180) {
            context.getString(R.string.waning_crescent)
        } else {
            context.getString(R.string.not_available)
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
}