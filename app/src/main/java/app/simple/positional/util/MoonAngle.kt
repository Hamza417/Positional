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

//    fun getMoonPhaseGraphics(moonAngle: Double): Int {
//        when (moonAngle) {
//            in -180.0..-135.0 -> {
//                return R.drawable.moon_phase_01
//            }
//            in -135.1..-90.0 -> {
//                return R.drawable.moon_phase_02
//            }
//            in -90.1..-45.0 -> {
//                return R.drawable.moon_phase_03
//            }
//            in -45.1..0.0 -> {
//                return R.drawable.moon_phase_04
//            }
//            in 0.1..45.0 -> {
//                return R.drawable.moon_phase_05
//            }
//            in 45.1..90.0 -> {
//                return R.drawable.moon_phase_06
//            }
//            in 90.1..135.0 -> {
//                return R.drawable.moon_phase_07
//            }
//            in 135.1..180.0 -> {
//                return R.drawable.moon_phase_08
//            }
//            else -> {
//                return 0
//            }
//        }
//    }

    fun getMoonPhaseGraphics(moonAngle: Double): Int {
        // Write the same fun but with angle difference of 12 degrees over the span of 30 days
        // and then use the angle to get the image
        return when (moonAngle) {
            in -180.0..-168.0 -> {
                R.drawable.moon00
            }
            in -168.1..-156.0 -> {
                R.drawable.moon01
            }
            in -156.1..-144.0 -> {
                R.drawable.moon02
            }
            in -144.1..-132.0 -> {
                R.drawable.moon03
            }
            in -132.1..-120.0 -> {
                R.drawable.moon04
            }
            in -120.1..-108.0 -> {
                R.drawable.moon05
            }
            in -108.1..-96.0 -> {
                R.drawable.moon06
            }
            in -96.1..-84.0 -> {
                R.drawable.moon07
            }
            in -84.1..-72.0 -> {
                R.drawable.moon08
            }
            in -72.1..-60.0 -> {
                R.drawable.moon09
            }
            in -60.1..-48.0 -> {
                R.drawable.moon10
            }
            in -48.1..-36.0 -> {
                R.drawable.moon11
            }
            in -36.1..-24.0 -> {
                R.drawable.moon12
            }
            in -24.1..-12.0 -> {
                R.drawable.moon13
            }
            in -12.1..0.0 -> {
                R.drawable.moon14
            }
            in 0.1..12.0 -> {
                R.drawable.moon15
            }
            in 12.1..24.0 -> {
                R.drawable.moon16
            }
            in 24.1..36.0 -> {
                R.drawable.moon17
            }
            in 36.1..48.0 -> {
                R.drawable.moon18
            }
            in 48.1..60.0 -> {
                R.drawable.moon19
            }
            in 60.1..72.0 -> {
                R.drawable.moon20
            }
            in 72.1..84.0 -> {
                R.drawable.moon21
            }
            in 84.1..96.0 -> {
                R.drawable.moon22
            }
            in 96.1..108.0 -> {
                R.drawable.moon23
            }
            in 108.1..120.0 -> {
                R.drawable.moon24
            }
            in 120.1..132.0 -> {
                R.drawable.moon25
            }
            in 132.1..144.0 -> {
                R.drawable.moon26
            }
            in 144.1..156.0 -> {
                R.drawable.moon27
            }
            in 156.1..168.0 -> {
                R.drawable.moon28
            }
            in 168.1..180.0 -> {
                R.drawable.moon29
            }
            else -> {
                0
            }
        }
    }
}