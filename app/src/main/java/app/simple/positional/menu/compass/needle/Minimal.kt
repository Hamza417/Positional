package app.simple.positional.menu.compass.needle

import android.content.Context
import android.view.Gravity
import app.simple.positional.R
import app.simple.positional.ui.Compass
import com.github.zawadz88.materialpopupmenu.popupMenu
import kotlinx.android.synthetic.main.frag_compass.*
import kotlin.properties.Delegates

class Minimal {
    private var skins by Delegates.notNull<Int>()

    fun minimalNeedle(context: Context, compass: Compass, skin: Int) {

        skins = skin

        val compassPopupMenu = popupMenu {
            style = R.style.popupMenu
            dropdownGravity = Gravity.END
            section {
                title = "Minimal Skins"
                item {
                    label = "Gradient (Rounded)"
                    icon = if (skins == R.drawable.needle_minimal_gradient) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        skins = R.drawable.needle_minimal_gradient
                    }
                }
                item {
                    label = "Gradient (Sharp)"
                    icon = if (skins == R.drawable.needle_minimal_gradient_sharp) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        skins = R.drawable.needle_minimal_gradient_sharp
                    }
                }
                item {
                    label = "Classic"
                    icon = if (skins == R.drawable.compass_needle_classic) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        skins = R.drawable.compass_needle_classic
                    }
                }
                item {
                    label = "Simple"
                    icon = if (skins == R.drawable.compass_needle_simple) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        skins = R.drawable.compass_needle_simple
                    }
                }
                item {
                    label = "Desaturated"
                    icon = if (skins == R.drawable.compass_needle_desaturated) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        skins = R.drawable.compass_needle_desaturated
                    }
                }
                item {
                    label = "Sharp"
                    icon = if (skins == R.drawable.compass_needle_sharp) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        skins = R.drawable.compass_needle_sharp
                    }
                }
            }
        }

        compassPopupMenu.show(context, compass.compass_menu)

        compassPopupMenu.setOnDismissListener {
            setNeedleTheme(context, compass, skins)
        }
    }
}