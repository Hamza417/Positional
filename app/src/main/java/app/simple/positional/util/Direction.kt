package app.simple.positional.util

fun getDirectionCodeFromAzimuth(azimuth: Double): String {
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

fun getDirectionNameFromAzimuth(azimuth: Double): String {
    var direction = "North West"
    if (azimuth >= 350f || azimuth <= 10f) {
        direction = "North"
    }
    if (azimuth in 281f..349f) {
        direction = "North West"
    }
    if (azimuth in 261f..280f) {
        direction = "West"
    }
    if (azimuth in 191f..260f) {
        direction = "South West"
    }
    if (azimuth in 171f..190f) {
        direction = "South"
    }
    if (azimuth in 101f..170f) {
        direction = "South East"
    }
    if (azimuth in 81f..100f) {
        direction = "East"
    }
    if (azimuth in 11f..80f) {
        direction = "North East"
    }

    return direction
}