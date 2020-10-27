package app.simple.positional.menu.compass.needle

import android.content.Context
import android.view.Gravity
import app.simple.positional.R
import app.simple.positional.preference.CompassPreference
import app.simple.positional.ui.Compass
import com.github.zawadz88.materialpopupmenu.popupMenu

class Needle {
    fun needleSkinsOptions(context: Context, compass: Compass, skins: Int) {
        val compassPopupMenu = popupMenu {
            style = R.style.popupMenu
            dropdownGravity = Gravity.END
            section {
                item {
                    label = if (skins != 0) "Disable Needle" else "Enable Needle"
                    icon = if (skins == 0) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        if (skins == 0) {
                            setNeedleTheme(context, compass, CompassPreference().getLastNeedle(context))
                        } else {
                            CompassPreference().setLastNeedle(skins, context)
                            setNeedleTheme(context, compass, 0)
                        }
                    }
                }
            }
            section {
                title = "Needle Skins"
                item {
                    label = "Minimal"
                    icon = R.drawable.ic_minimal
                    hasNestedItems = true
                    callback = {
                        Minimal().minimalNeedle(context, compass, skins)
                    }
                }
                item {
                    label = "Retro"
                    icon = R.drawable.ic_retro
                    hasNestedItems = true
                    callback = {
                        Retro().retroNeedle(context, compass, skins)
                    }
                }
            }
        }

        compassPopupMenu.show(context, compass.actionView)
    }
}