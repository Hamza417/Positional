package app.simple.positional.menu.compass.needle

import android.content.Context
import android.view.Gravity
import app.simple.positional.R
import app.simple.positional.ui.Compass
import com.github.zawadz88.materialpopupmenu.popupMenu
import kotlinx.android.synthetic.main.frag_compass.*
import kotlin.properties.Delegates

class Retro {
    private var skins by Delegates.notNull<Int>()

    fun retroNeedle(context: Context, compass: Compass, skin: Int) {

        skins = skin

        val compassPopupMenu = popupMenu {
            style = R.style.popupMenu
            dropdownGravity = Gravity.END
            fixedContentWidthInPx = 500
            section {
                title = "Retro Skins"
                item {
                    label = "Old Needle"
                    icon = if (skins == R.drawable.needle_degrees_needle) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        skins = R.drawable.needle_degrees_needle
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