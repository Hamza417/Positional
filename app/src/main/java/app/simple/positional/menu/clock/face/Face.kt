package app.simple.positional.menu.clock.face

import android.content.Context
import android.view.Gravity
import app.simple.positional.R
import app.simple.positional.preference.ClockPreferences
import app.simple.positional.ui.Clock
import com.github.zawadz88.materialpopupmenu.popupMenu
import kotlinx.android.synthetic.main.frag_clock.*

class Face {

    var value = 0

    fun faceSkinsOptions(context: Context, clock: Clock) {

        value = ClockPreferences().getClockFaceTheme(context)

        val clockPopupMenu = popupMenu {
            style = R.style.popupMenu
            dropdownGravity = Gravity.END
            section {
                title = "Face Visibility"
                item {
                    label = if (value != 0) "Hide" else "Show"
                    icon = if (value == 0) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        if (value == 0) { // means face is hidden
                            value = ClockPreferences().getLastFace(context)
                            clock.setDial(value)
                            ClockPreferences().setClockFaceTheme(value, context)
                        } else {
                            ClockPreferences().setLastFace(value, context)
                            ClockPreferences().setClockFaceTheme(0, context)
                            clock.setDial(0)
                        }
                    }
                }
            }
            section {
                title = "Dial Skins"
                item {
                    label = "Minimal"
                    icon = R.drawable.ic_minimal
                    hasNestedItems = true
                    callback = {
                        Minimal().minimalSkins(context = context, clock = clock)
                    }
                }
            }
            section {
                item {
                    label = "Opacity"
                    icon = R.drawable.ic_opacity
                    hasNestedItems = true
                    callback = {
                        Opacity().faceAlpha(context, clock)
                    }
                }
            }
        }

        clockPopupMenu.show(context, clock.clock_menu)
    }
}