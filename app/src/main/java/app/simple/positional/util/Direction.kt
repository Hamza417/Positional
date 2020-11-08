package app.simple.positional.util

fun getDirectionFromAzimuth(azimuth: Double): String {
    var direction = "NW"
    if (azimuth >= 350f || azimuth <= 10f) {
        direction = "N"
    }
    if (azimuth in 281f..349f) {
        direction = "NW"
    }
    if (azimuth in 261f..280f) {
        direction = "W"
    }
    if (azimuth in 191f..260f) {
        direction = "SW"
    }
    if (azimuth in 171f..190f) {
        direction = "S"
    }
    if (azimuth in 101f..170f) {
        direction = "SE"
    }
    if (azimuth in 81f..100f) {
        direction = "E"
    }
    if (azimuth in 11f..80f) {
        direction = "NE"
    }

    return direction
}