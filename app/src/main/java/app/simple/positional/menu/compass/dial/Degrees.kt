package app.simple.positional.menu.compass.dial

import android.content.Context
import android.view.Gravity
import app.simple.positional.R
import app.simple.positional.ui.Compass
import com.github.zawadz88.materialpopupmenu.popupMenu
import kotlinx.android.synthetic.main.frag_compass.*
import kotlin.properties.Delegates

class Degrees {

    private var skins by Delegates.notNull<Int>()

    fun degreeSkins(context: Context, compass: Compass, skin: Int) {

        skins = skin

        val compassPopupMenu = popupMenu {
            style = R.style.popupMenu
            dropdownGravity = Gravity.END
            section {
                title = "Degrees Skins"
                item {
                    label = "Degrees"
                    icon = if (skins == R.drawable.compass_dial_degrees) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        skins = R.drawable.compass_dial_degrees
                    }
                }
                item {
                    label = "Dark Blue"
                    icon = if (skins == R.drawable.dial_degrees_needles) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        skins = R.drawable.dial_degrees_needles
                    }
                }
            }
        }

        compassPopupMenu.show(context, compass.compass_menu)

        compassPopupMenu.setOnDismissListener {
            setDialTheme(context, compass, skins)
        }
    }
}