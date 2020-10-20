package app.simple.positional.menu.dial

import android.content.Context
import app.simple.positional.preference.CompassPreference
import app.simple.positional.ui.Compass

fun setDialTheme(context: Context, compass: Compass, skins: Int) {
    CompassPreference().setDial(skins, context)
    compass.skins[1] = skins
    compass.setDial()
}