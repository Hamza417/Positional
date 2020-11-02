package app.simple.positional.menu.clock.needle

import android.content.Context
import android.view.Gravity
import app.simple.positional.R
import app.simple.positional.preference.ClockPreferences
import app.simple.positional.ui.Clock
import com.github.zawadz88.materialpopupmenu.popupMenu
import kotlinx.android.synthetic.main.frag_clock.*

class Minimal {

    var value: Int = -1

    fun setClockNeedle(context: Context, clock: Clock) {

        value = ClockPreferences().getClockNeedleTheme(context)

        val popupMenu = popupMenu {
            style = R.style.popupMenu
            dropdownGravity = Gravity.END
            section {
                title = "Minimal Needle Skins"
                item {
                    label = "Grey"
                    icon = if (value == 0) R.drawable.ic_radio_button_checked else R.drawable.ic_radio_button_unchecked
                    callback = {
                        value = 0
                    }
                }
                item {
                    label = "Thin Red"
                    icon = if (value == 1) R.drawable.ic_radio_button_checked else R.drawable.ic_radio_button_unchecked
                    callback = {
                        value = 1
                    }
                }
                item {
                    label = "Thick Rounded"
                    icon = if (value == 2) R.drawable.ic_radio_button_checked else R.drawable.ic_radio_button_unchecked
                    callback = {
                        value = 2
                    }
                }
                item {
                    label = "Red Pointy"
                    icon = if (value == 3) R.drawable.ic_radio_button_checked else R.drawable.ic_radio_button_unchecked
                    callback = {
                        value = 3
                    }
                }
            }
        }

        popupMenu.show(context = context, anchor = clock.clock_menu)

        popupMenu.setOnDismissListener {
            clock.setNeedle(value)
            ClockPreferences().setClockNeedleTheme(value, context)
        }
    }
}