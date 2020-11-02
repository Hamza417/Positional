package app.simple.positional.menu.clock.face

import android.content.Context
import android.view.Gravity
import app.simple.positional.R
import app.simple.positional.preference.ClockPreferences
import app.simple.positional.ui.Clock
import com.github.zawadz88.materialpopupmenu.popupMenu
import kotlinx.android.synthetic.main.frag_clock.*

class Roman {
    var value = -1

    fun romanSkins(context: Context, clock: Clock) {

        value = ClockPreferences().getClockFaceTheme(context)

        val popupMenu = popupMenu {
            style = R.style.popupMenu
            dropdownGravity = Gravity.END
            section {
                title = "Roman Skins"
                item {
                    label = "Simple Roman"
                    icon = if (value == 3) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        value = 3
                    }
                }
                item {
                    label = "Wrong Roman"
                    icon = if (value == 6) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        value = 6
                    }
                }
                item {
                    label = "Caged Bird"
                    icon = if (value == 7) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        value = 7
                    }
                }
                item {
                    label = "Birds and Hearts"
                    icon = if (value == 9) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        value = 9
                    }
                }
                item {
                    label = "Birds on Branches"
                    icon = if (value == 10) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        value = 10
                    }
                }
                item {
                    label = "Bullets"
                    icon = if (value == 13) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        value = 13
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