package app.simple.positional.menu.needle

import android.content.Context
import app.simple.positional.preference.CompassPreference
import app.simple.positional.ui.Compass

fun setNeedleTheme(context: Context, compass: Compass, skins: Int) {
    CompassPreference().setNeedle(skins, context)
    compass.skins[0] = skins
    compass.setNeedle()
}