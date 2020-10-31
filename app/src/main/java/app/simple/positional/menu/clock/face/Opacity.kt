package app.simple.positional.menu.clock.face

import android.content.Context
import android.view.Gravity
import app.simple.positional.R
import app.simple.positional.preference.ClockPreferences
import app.simple.positional.ui.Clock
import com.github.zawadz88.materialpopupmenu.popupMenu
import kotlinx.android.synthetic.main.frag_clock.*

class Opacity {

    private var opacity: Float = 0f

    fun faceAlpha(context: Context, clock: Clock) {

        opacity = ClockPreferences().getFaceOpacity(context)

        val popupMenu = popupMenu {
            style = R.style.popupMenu
            dropdownGravity = Gravity.END
            section {
                title = "Dial Opacity"
                item {
                    label = "25%"
                    icon = if (opacity == 0.25f) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        opacity = 0.25f
                    }
                }
                item {
                    label = "50%"
                    icon = if (opacity == 0.50f) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        opacity = 0.50f
                    }
                }
                item {
                    label = "75% / Default"
                    icon = if (opacity == 0.75f) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        opacity = 0.75f
                    }
                }
                item {
                    label = "100%"
                    icon = if (opacity == 1.0f) {
                        R.drawable.ic_radio_button_checked
                    } else R.drawable.ic_radio_button_unchecked
                    callback = {
                        opacity = 1.0f
                    }
                }
            }
        }

        popupMenu.show(context, clock.clock_menu)

        popupMenu.setOnDismissListener {
            ClockPreferences().setFaceOpacity(opacity, context)
            clock.setFaceAlpha(opacity)
        }
    }
}