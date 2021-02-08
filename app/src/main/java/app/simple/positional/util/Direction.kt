package app.simple.positional.util

import android.content.Context
import app.simple.positional.R

object Direction {
    fun getDirectionCodeFromAzimuth(context: Context, azimuth: Double): String {
        return if (azimuth >= 349.001F || azimuth <= 10F) {
            context.getString(R.string.north_N)
        } else if (azimuth in 280.001F..349F) {
            context.getString(R.string.north_west_NW)
        } else if (azimuth in 260.001F..280F) {
            context.getString(R.string.west_W)
        } else if (azimuth in 190.001F..260F) {
            context.getString(R.string.south_west_SW)
        } else if (azimuth in 170.001F..190F) {
            context.getString(R.string.south_S)
        } else if (azimuth in 100.001F..170F) {
            context.getString(R.string.south_east_SE)
        } else if (azimuth in 80.001F..100F) {
            context.getString(R.string.east_E)
        } else if (azimuth in 10.001F..80F) {
            context.getString(R.string.north_east_NE)
        } else {
            context.getString(R.string.not_available)
        }
    }

    fun getDirectionNameFromAzimuth(context: Context, azimuth: Double): String {
        return if (azimuth >= 349.001F || azimuth <= 10F) {
            context.getString(R.string.north)
        } else if (azimuth in 280.001F..349F) {
            context.getString(R.string.north_west)
        } else if (azimuth in 260.001F..280F) {
            context.getString(R.string.west)
        } else if (azimuth in 190.001F..260F) {
            context.getString(R.string.south_west)
        } else if (azimuth in 170.001F..190F) {
            context.getString(R.string.south)
        } else if (azimuth in 100.001F..170F) {
            context.getString(R.string.south_east)
        } else if (azimuth in 80.001F..100F) {
            context.getString(R.string.east)
        } else if (azimuth in 10.001F..80F) {
            context.getString(R.string.north_east)
        } else {
            context.getString(R.string.not_available)
        }
    }
}
