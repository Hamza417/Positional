package app.simple.positional.menu.compass.dial

import android.content.Context
import android.view.Gravity
import app.simple.positional.R
import app.simple.positional.preference.CompassPreference
import app.simple.positional.ui.Compass
import com.github.zawadz88.materialpopupmenu.popupMenu

class Dial {
    fun dialSkinsOptions(context: Context, compass: Compass, skins: Int) {
        val compassPopupMenu = popupMenu {
            style = R.style.popupMenu
            dropdownGravity = Gravity.END
            section {
                item {
                    label = if (skins != 0) "Disable Dial" else "Enable Dial"
                    icon = if (skins == 0) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        if (skins == 0) {
                            setDialTheme(context, compass, CompassPreference().getLastDial(context))
                        } else {
                            CompassPreference().setLastDial(skins, context)
                            setDialTheme(context, compass, 0)
                        }
                    }
                }
            }
            section {
                title = "Dial Skins"
                item {
                    label = "Minimal"
                    icon = R.drawable.ic_minimal
                    hasNestedItems = true
                    callback = {
                        Minimal().minimalSkins(context, compass, skins)
                    }
                }
                item {
                    label = "Degrees"
                    icon = R.drawable.ic_degrees
                    hasNestedItems = true
                    callback = {
                        Degrees().degreeSkins(context, compass, skins)
                    }
                }
                item {
                    label = "Retro"
                    icon = R.drawable.ic_retro
                    hasNestedItems = true
                    callback = {
                        Retro().retroSkins(context, compass, skins)
                    }
                }
            }
            section {
                item {
                    label = "Opacity"
                    hasNestedItems = true
                    icon = R.drawable.ic_opacity
                    callback = {
                        DialAlpha().dialAlpha(context, compass)
                    }
                }
            }
        }

        compassPopupMenu.show(context, compass.actionView)
    }
}