package app.simple.positional.constants

import android.graphics.Color
import app.simple.positional.R

object CompassBloom {
    var compassBloomRes = arrayOf(
            R.drawable.compass_bloom_01,
            R.drawable.compass_bloom_02,
            R.drawable.compass_bloom_03,
            R.drawable.compass_bloom_04
    )

    var compassBloomTextColor = arrayOf(
            Color.parseColor("#f88806"), // Orange
            Color.parseColor("#6F2374"), // Red
            Color.parseColor("#FFEE074D"), // Purple
            Color.parseColor("#FF1BA0D1"), // Moon Dark
    )
}