package app.simple.positional.menu.clock.needle

import android.content.Context
import android.view.Gravity
import app.simple.positional.R
import app.simple.positional.preference.ClockPreferences
import app.simple.positional.ui.Clock
import com.github.zawadz88.materialpopupmenu.popupMenu
import kotlinx.android.synthetic.main.frag_clock.*

class Retro {
    var value: Int = -1

    fun setClockRetroNeedle(context: Context, clock: Clock) {

        value = ClockPreferences().getClockNeedleTheme(context)

        val popupMenu = popupMenu {
            style = R.style.popupMenu
            dropdownGravity = Gravity.END
            section {
                title = "Retro Needle Skins"
                item {
                    label = "Black/Red"
                    icon = if (value == 4) R.drawable.ic_radio_button_checked else R.drawable.ic_radio_button_unchecked
                    callback = {
                        value = 4
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