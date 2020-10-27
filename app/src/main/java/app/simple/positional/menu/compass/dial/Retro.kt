package app.simple.positional.menu.compass.dial

import android.content.Context
import android.view.Gravity
import app.simple.positional.R
import app.simple.positional.ui.Compass
import com.github.zawadz88.materialpopupmenu.popupMenu
import kotlin.properties.Delegates

class Retro {
    private var skins by Delegates.notNull<Int>()

    fun retroSkins(context: Context, compass: Compass, skin: Int) {

        skins = skin

        val compassPopupMenu = popupMenu {
            style = R.style.popupMenu
            dropdownGravity = Gravity.END
            section {
                title = "Retro Skins"
                item {
                    label = "Nautica"
                    icon = if (skins == R.drawable.compass_dial_nautica) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        skins = R.drawable.compass_dial_nautica
                    }
                }
                item {
                    label = "Ship"
                    icon = if (skins == R.drawable.dial_retro_ship) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        skins = R.drawable.dial_retro_ship
                    }
                }
            }
        }

        compassPopupMenu.show(context, compass.actionView)

        compassPopupMenu.setOnDismissListener {
            setDialTheme(context, compass, skins)
        }
    }
}