package app.simple.positional.menu.clock.face

import android.content.Context
import android.view.Gravity
import app.simple.positional.R
import app.simple.positional.preference.ClockPreferences
import app.simple.positional.ui.Clock
import com.github.zawadz88.materialpopupmenu.popupMenu
import kotlinx.android.synthetic.main.frag_clock.*

class Number {
    var value = -1

    fun numberSkins(context: Context, clock: Clock) {

        value = ClockPreferences().getClockFaceTheme(context)

        val popupMenu = popupMenu {
            style = R.style.popupMenu
            dropdownGravity = Gravity.END
            section {
                title = "Number Skins"
                item {
                    label = "Simple Numbers"
                    icon = if (value == 2) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        value = 2
                    }
                }
                item {
                    label = "Only Numbers"
                    icon = if (value == 4) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        value = 4
                    }
                }
                item {
                    label = "Plain"
                    icon = if (value == 5) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        value = 5
                    }
                }
                item {
                    label = "Birds on Branches"
                    icon = if (value == 8) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        value = 8
                    }
                }
                item {
                    label = "Birds and Hearts"
                    icon = if (value == 11) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        value = 11
                    }
                }
                item {
                    label = "Dots"
                    icon = if (value == 14) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        value = 14
                    }
                }
                item {
                    label = "Minutes"
                    icon = if (value == 15) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        value = 15
                    }
                }
                item {
                    label = "Bullets"
                    icon = if (value == 16) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        value = 16
                    }
                }
                item {
                    label = "Arabic"
                    icon = if (value == 18) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        value = 18
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