package app.simple.positional.menu.compass.dial

import android.content.Context
import android.view.Gravity
import app.simple.positional.R
import app.simple.positional.ui.Compass
import com.github.zawadz88.materialpopupmenu.popupMenu
import kotlin.properties.Delegates

class Minimal {
    private var skins by Delegates.notNull<Int>()

    fun minimalSkins(context: Context, compass: Compass, skin: Int) {

        skins = skin

        val compassPopupMenu = popupMenu {
            style = R.style.popupMenu
            dropdownGravity = Gravity.END
            section {
                title = "Minimal Skins"
                item {
                    label = "Gradient"
                    icon = if (skins == R.drawable.dial_minimal_gradient) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        skins = R.drawable.dial_minimal_gradient
                    }
                }
                item {
                    label = "Minimal"
                    icon = if (skins == R.drawable.compass_dial_minimal) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        skins = R.drawable.compass_dial_minimal
                    }
                }
                item {
                    label = "Plain"
                    icon = if (skins == R.drawable.compass_dial_simple) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        skins = R.drawable.compass_dial_simple
                    }
                }
                item {
                    label = "Rose"
                    icon = if (skins == R.drawable.compass_dial_rose_one) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        skins = R.drawable.compass_dial_rose_one
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