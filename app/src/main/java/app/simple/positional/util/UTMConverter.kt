package app.simple.positional.util

import gov.nasa.worldwind.geom.Angle
import gov.nasa.worldwind.geom.coords.UTMCoord

object UTMConverter {
    fun getUTMCoordinates(latitude: Double, longitude: Double): String {
        return "${getUtmZone(latitude, longitude)} ${getUtmEasting(latitude, longitude)} ${getUtmNorthing(latitude, longitude)}"
    }

    private fun getUtmEasting(lat: Double, lon: Double): String {
        return "${
            String.format(
                    LocaleHelper.getAppLocale(),
                    "%7.0f",
                    getUtmCoord(lat, lon).easting
            )
        }m E"
    }

    private fun getUtmCoord(lat: Double, lon: Double): UTMCoord {
        return UTMCoord.fromLatLon(Angle.fromDegreesLatitude(lat), Angle.fromDegreesLongitude(lon))
    }

    private fun getUtmZone(lat: Double, lon: Double): String {
        return "${getUtmCoord(lat, lon).zone}${getUtmLatBand(lat)}"
    }

    private fun getUtmLatBand(lat: Double): String {
        return when {
            -80.0 <= lat && lat < -72.0 -> "C"
            -72.0 <= lat && lat < -64.0 -> "D"
            -72.0 <= lat && lat < -56.0 -> "E"
            -72.0 <= lat && lat < -48.0 -> "F"
            -72.0 <= lat && lat < -40.0 -> "G"
            -72.0 <= lat && lat < -32.0 -> "H"
            -72.0 <= lat && lat < -24.0 -> "J"
            -72.0 <= lat && lat < -16.0 -> "K"
            -72.0 <= lat && lat < -8.0 -> "L"
            -72.0 <= lat && lat < 0.0 -> "M"
            00.0 <= lat && lat < 08.0 -> "N"
            08.0 <= lat && lat < 16.0 -> "P"
            16.0 <= lat && lat < 24.0 -> "Q"
            24.0 <= lat && lat < 32.0 -> "R"
            32.0 <= lat && lat < 40.0 -> "S"
            40.0 <= lat && lat < 48.0 -> "T"
            48.0 <= lat && lat < 56.0 -> "U"
            56.0 <= lat && lat < 64.0 -> "V"
            64.0 <= lat && lat < 72.0 -> "W"
            72.0 <= lat && lat < 84.0 -> "X"
            else -> ""
        }
    }

    private fun getUtmNorthing(lat: Double, lon: Double): String {
        val utmCoordinate = getUtmCoord(lat, lon)
        return "${
            String.format(
                    LocaleHelper.getAppLocale(),
                    "%7.0f",
                    utmCoordinate.northing
            )
        }m N"
    }
}
