package app.simple.positional.menu.clock.face

import android.content.Context
import android.view.Gravity
import app.simple.positional.R
import app.simple.positional.preference.ClockPreferences
import app.simple.positional.ui.Clock
import com.github.zawadz88.materialpopupmenu.popupMenu
import kotlinx.android.synthetic.main.frag_clock.*

class Minimal {

    var value = -1

    fun minimalSkins(context: Context, clock: Clock) {

        value = ClockPreferences().getClockFaceTheme(context)

        val popupMenu = popupMenu {
            style = R.style.popupMenu
            dropdownGravity = Gravity.END
            section {
                title = "Minimal Skins"
                item {
                    label = "Minimal"
                    icon = if (value == 1) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        value = 1
                    }
                }
                item {
                    label = "Simple"
                    icon = if (value == 12) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        value = 12
                    }
                }
                item {
                    label = "Rounded Hours"
                    icon = if (value == 17) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        value = 17
                    }
                }
            }
        }

        popupMenu.show(context, clock.clock_menu)

        popupMenu.setOnDismissListener {
            ClockPreferences().setClockFaceTheme(value, context)
            clock.setDial(value)
        }
    }
}