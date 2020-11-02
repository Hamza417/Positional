package app.simple.positional.menu.clock.needle

import android.content.Context
import android.view.Gravity
import app.simple.positional.R
import app.simple.positional.ui.Clock
import com.github.zawadz88.materialpopupmenu.popupMenu
import kotlinx.android.synthetic.main.frag_clock.*

class Needle {
    fun openNeedleMenu(context: Context, clock: Clock) {
        val popupMenu = popupMenu {
            style = R.style.popupMenu
            dropdownGravity = Gravity.END
            section {
                title = "Needle Skins"
                item {
                    label = "Minimal"
                    hasNestedItems = true
                    icon = R.drawable.ic_minimal
                    callback = {
                        Minimal().setClockNeedle(context = context, clock = clock)
                    }
                }
                item {
                    label = "Retro"
                    hasNestedItems = true
                    icon = R.drawable.ic_minimal
                    callback = {
                        Retro().setClockRetroNeedle(context = context, clock = clock)
                    }
                }
            }
        }

        popupMenu.show(context, clock.clock_menu)
    }
}