package app.simple.positional.util

import android.content.Context
import app.simple.positional.R

object Direction {
    fun getDirectionCodeFromAzimuth(context: Context, azimuth: Double): String {
        return if (azimuth >= 350f || azimuth <= 10f) {
            context.getString(R.string.north_N)
        } else if (azimuth in 281f..349f) {
            context.getString(R.string.north_west_NW)
        } else if (azimuth in 261f..280f) {
            context.getString(R.string.west_W)
        } else if (azimuth in 191f..260f) {
            context.getString(R.string.south_west_SW)
        } else if (azimuth in 171f..190f) {
            context.getString(R.string.south_S)
        } else if (azimuth in 101f..170f) {
            context.getString(R.string.south_east_SE)
        } else if (azimuth in 81f..100f) {
            context.getString(R.string.east_E)
        } else if (azimuth in 11f..80f) {
            context.getString(R.string.north_east_NE)
        } else {
            context.getString(R.string.not_available)
        }
    }

    fun getDirectionNameFromAzimuth(context: Context, azimuth: Double): String {
        return if (azimuth >= 350f || azimuth <= 10f) {
            context.getString(R.string.north)
        } else if (azimuth in 281f..349f) {
            context.getString(R.string.north_west)
        } else if (azimuth in 261f..280f) {
            context.getString(R.string.west)
        } else if (azimuth in 191f..260f) {
            context.getString(R.string.south_west)
        } else if (azimuth in 171f..190f) {
            context.getString(R.string.south)
        } else if (azimuth in 101f..170f) {
            context.getString(R.string.south_east)
        } else if (azimuth in 81f..100f) {
            context.getString(R.string.east)
        } else if (azimuth in 11f..80f) {
            context.getString(R.string.north_east)
        } else {
            context.getString(R.string.not_available)
        }
    }
}